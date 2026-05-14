package za.co.wethinkcode.robots.services;
/**
 * the ITCService (Inter-Thread Communication Service)
 * 
 * is the middle point the service in which all socket client handler deals with 
 * everything is stored here like storage of client threads the world in which all robots will be launched
 * and because its a singleton the instance is persisted at runtime. race condition on command execution is
 * address by placing a lock. and stale data is avoid by making all field volatile
 * 
*/

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseRobot;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.impediment.Hole;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Mountain;
import za.co.wethinkcode.robots.models.impediment.Rocks;
import za.co.wethinkcode.robots.models.impediment.Tree;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.*;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.shared.Protocol;

public class ITCService {

    //initialize all singleton instance fields using the volatile to keep values synced across all threads
    
   private volatile Map<Socket,Thread> threads;
   private final Map<String, java.io.PrintWriter> clientWriters = new java.util.concurrent.ConcurrentHashMap<>();
   private volatile static ITCService instance = new ITCService();
   private volatile Logger logger = LoggerFactory.getLogger(ITCService.class);
   private volatile Iworld world;

    private ITCService(){
        this.threads=new HashMap<>();
    }

    public void registerClient(String robotName, java.io.PrintWriter writer) {
        if (robotName == null || writer == null) return;
        this.clientWriters.put(robotName, writer);
    }

    public void unregisterClient(String robotName) {
        if (robotName != null) this.clientWriters.remove(robotName);
    }

    /** Push a one-way event to the named client (the victim). Safe to call from any thread. */
    public void pushEvent(String robotName, ServerResponse event) {
        if (robotName == null || event == null) return;
        java.io.PrintWriter w = this.clientWriters.get(robotName);
        if (w == null) return;
        try {
            String json = new Protocol().encodeResponse(event);
            synchronized (w) {
                w.println(json);
                w.flush();
            }
        } catch (Exception e) {
            this.logger.warn("pushEvent failed for " + robotName + ": " + e.getMessage());
        }
    }

    public void addThreadControllers(Socket client,Thread thread){
        this.threads.put(client, thread);
    }
    /**
     * Terminates Socket
     * given a socket its goes throught stored map finds the thread then terminates the thread
     * @param Socket of the the client you want to terminate
     * @return boolean
     */
    public boolean terminateServerThread(Socket client){
        
        Thread thread = this.threads.get(client);
        this.logger.info(client.getLocalAddress().getHostName()+" is requesting to kill its dedicated thread.");
        if(thread!=null){
            thread.interrupt();
            return true;
        }
        return false;
    }

    protected Properties loadProperty(String prop){
        Properties propLoader = new Properties(null);
       
       InputStream propFd = ITCService.class.getClassLoader().getResourceAsStream(prop);
        try {
            propLoader.load(propFd);
           return propLoader;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            this.logger.error(e.getMessage());
            System.err.println(e);
        }
        return null;
    }
    public ArrayList<ArrayList<Impediments>> parseMap(String res){
        ArrayList<ArrayList<Impediments>> new_map=new ArrayList<>();

       Properties prop =loadProperty(res);
       if (prop != null){

       Object raw_map;
       if (( raw_map =prop.get("robotWorld"))!=null){  new_map = parseStringMap((String)raw_map); return new_map;}
       
    }return null;}
    
    /**
     * parseStringMap
     * takes in a single string like "T  R  R,      ,T  R  H" splits it into a 2d string by spliting by ","
     * and interprets letter as Objects like T Builds a Tree Objects located at the list coordinates 
     * @param raw_map
     * @return ArrayList<ArrayList<Impediments>> which is a 2D list of objects(Impediments)
     */
    public ArrayList<ArrayList<Impediments>> parseStringMap(String raw_map){
        ArrayList<ArrayList<Impediments>> new_map=new ArrayList<>();
        String[] rows = raw_map.toString().split(",");
          
            for (String row: rows){
                
                char[] column = row.toLowerCase().toCharArray();
                ArrayList<Impediments> temp = new ArrayList<>();

                for (char ch:column){
                    Impediments obj=null;
                    if (ch=='h'){ obj= new Hole(); }
                    if (ch=='r'){ obj= new Rocks(); }
                    if (ch=='t'){ obj= new Tree();}
                    if (ch=='m'){ obj= new Mountain(); }
                    if(ch!='"') { temp.add(obj); }
                }
                new_map.add(temp);
            }
        return new_map;
    }



    /**
     * SetWorld
     * sets The Current world in which robots will be launched
     * @param given_world the instance of IWorld 
     * 
     */
    public void setWorld(Iworld given_world){
        this.logger.info(".... Robot World initialized");
        this.world = given_world;
       
    }
    public void loadWorld(Iworld given_world){
        setWorld(given_world);
         ArrayList<ArrayList<Impediments>> map = parseMap("world.properties");
        this.world.loadMap(map);
    }

    public static  ITCService getInstance(){
        return instance;
    }
   
    /**
     * doThisCommand method
     * takes in a Json command in string format de-serializes it using Protocol.
     * in case the command name is related to shutdown,off or quit it returns back to the executor(ClientThread Handler) to terminate
     * generates Command using the factory method and sends the command to the world to execute
     * 
     * @param data
     * @return ServerResponse as Json string 
     */
    public synchronized  String doThisCommand(String data){ 
        
        this.logger.info("Command recieved: "+data);
        Protocol protocol =new Protocol();
        ServerRequest req =  protocol.decodeRequest(data);
       
        if ((req.getCommand().equals("off"))||(req.getCommand().equals("shutdown"))||(req.getCommand().equals("quit"))){return "off";}
        Command com = Command.generate(req);
        ServerResponse response = this.world.perform(com);

        decorateWithWorldState(response, req.getRobot());

        return protocol.encodeResponse(response);
    }

    /**
     * Attach live world info (ammo pickups, robot lives) to every response,
     * so the GUI never has to ask separately.
     */
    private void decorateWithWorldState(ServerResponse response, String robotName) {
        if (response == null) return;
        if (this.world instanceof RobotWorld rw) {
            ServerResponseData data = response.getData();
            if (data == null) {
                data = ServerResponseData.builder().build();
                response.setData(data);
            }
            if (data.getPickups() == null || data.getPickups().isEmpty()) {
                data.setPickups(rw.getAmmoPickups());
            }
            // Snapshot of all robots (positions, health, kills) — so every client renders the whole arena.
            java.util.List<ServerResponseRobot> snapshot = new java.util.ArrayList<>();
            for (BaseRobot r : rw.getAllRobots().values()) {
                snapshot.add(ServerResponseRobot.builder()
                        .name(r.getName())
                        .position(r.getPosition())
                        .direction(r.getDirection())
                        .lives(r.getLives())
                        .shields(r.getShield())
                        .shots(r.getShoots())
                        .kills(r.getKills())
                        .status(r.getOperationState())
                        .build());
            }
            data.setRobots(snapshot);
        }
        BaseRobot robot = this.world.getAllRobots().get(robotName);
        if (robot != null) {
            ServerResponseState state = response.getState();
            if (state == null) {
                state = ServerResponseState.builder().build();
                response.setState(state);
            }
            state.setLives(robot.getLives());
            state.setKills(robot.getKills());
            if (state.getPosition() == null) state.setPosition(robot.getPosition());
            if (state.getDirection() == null) state.setDirection(robot.getDirection());
            if (state.getShields() == 0) state.setShields(robot.getShield());
            if (state.getShots() == 0) state.setShots(robot.getShoots());
        }
    }
  
}
