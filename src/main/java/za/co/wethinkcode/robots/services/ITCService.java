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
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.*;
import za.co.wethinkcode.robots.models.impediment.Pit;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Mountain;
import za.co.wethinkcode.robots.models.impediment.Rocks;
import za.co.wethinkcode.robots.models.impediment.Tree;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.npc.KillerNPCController;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.*;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.shared.Protocol;

public class ITCService {

    //initialize all singleton instance fields using the volatile to keep values synced across all threads
    
   private volatile Map<Socket,Thread> threads;
   private final Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();
   private final Set<String> subscribers = ConcurrentHashMap.newKeySet();
   private volatile static ITCService instance = new ITCService();
   private volatile Logger logger = LoggerFactory.getLogger(ITCService.class);
   private volatile WorldGenerator world;
   private volatile KillerNPCController killerController;

    private ITCService(){
        this.threads=new HashMap<>();
    }

    public void registerClient(String robotName, PrintWriter writer) {
        if (robotName == null || writer == null) return;
        this.clientWriters.put(robotName, writer);
    }

    public void unregisterClient(String robotName) {
        if (robotName != null) {
            this.clientWriters.remove(robotName);
            this.subscribers.remove(robotName);
        }
    }

    public void subscribe(String robotName) {
        if (robotName != null) this.subscribers.add(robotName);
    }

    public void unsubscribe(String robotName) {
        if (robotName != null) this.subscribers.remove(robotName);
    }

    /**
     * Push a fresh world-state snapshot to every subscribed client.
     * Each subscriber gets their own response with `state` filled in for their robot.
     */
    public void broadcastWorldState() {
        if (this.subscribers.isEmpty() || this.world == null) return;
        Protocol protocol = new Protocol();
        for (String name : this.subscribers) {
            PrintWriter w = this.clientWriters.get(name);
            if (w == null) continue;
            ServerResponse snapshot = ServerResponse.builder()
                    .type("world_state")
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().build())
                    .build();
            decorateWithWorldState(snapshot, name);
            try {
                String json = protocol.encodeResponse(snapshot);
                synchronized (w) {
                    w.println(json);
                    w.flush();
                }
            } catch (Exception e) {
                this.logger.warn("broadcastWorldState failed for " + name + ": " + e.getMessage());
            }
        }
    }

    /** Push a one-way event to every registered client. Used for global announcements (e.g. NPC kills). */
    public void broadcastEvent(ServerResponse event) {
        if (event == null) return;
        try {
            String json = new Protocol().encodeResponse(event);
            for (PrintWriter w : this.clientWriters.values()) {
                synchronized (w) {
                    w.println(json);
                    w.flush();
                }
            }
        } catch (Exception e) {
            this.logger.warn("broadcastEvent failed: " + e.getMessage());
        }
    }

    public WorldGenerator getWorld() {
        return this.world;
    }

    public void setKillerController(KillerNPCController c) {
        this.killerController = c;
    }

    public KillerNPCController getKillerController() {
        return this.killerController;
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
        this.world.removeRobot(thread.getName());
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

            e.printStackTrace();
            this.logger.error(e.getMessage());
            System.err.println(e);
        }
        return null;
    }

    
    /**
     * parseStringMap
     * takes in a single string like "T  R  R,      ,T  R  H" splits it into a 2d string by spliting by ","
     * and interprets letter as Objects like T Builds a Tree Objects located at the list coordinates 
     * @param raw_map
     * @return ArrayList<ArrayList<Impediments>> which is a 2D list of objects(Impediments)
     */


    /**
     * SetWorld
     * sets The Current world in which robots will be launched
     * @param given_world the instance of WorldGenerator 
     * 
     */
    public void setWorld(WorldGenerator given_world){
        this.logger.info(".... Robot World initialized");
        this.world = given_world;
       
    }
 
    public static  ITCService getInstance(){
        return instance;
    }
    public void informClients(){
        ServerResponse res =  ServerResponse.builder().result(StatusCode.OK).data(ServerResponseData.builder().message("Server Shutting down").build()).build();
       String data = new Protocol().encodeResponse(res)+"\n";
        for (Socket client : threads.keySet()){
            try{
             client.getOutputStream().write(data.getBytes());
          
            }
            catch(IOException e){
                this.logger.info("failed to send client a good bye");
            }
        }
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

        // Service-level commands that don't touch the world.
        if ("subscribe".equalsIgnoreCase(req.getCommand())) {
            subscribe(req.getRobot());
            ServerResponse ack = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("Subscribed to world state").build())
                    .build();
            // Send the new subscriber an immediate snapshot so the GUI doesn't render an empty world.
            broadcastWorldState();
            return protocol.encodeResponse(ack);
        }
        if ("unsubscribe".equalsIgnoreCase(req.getCommand())) {
            unsubscribe(req.getRobot());
            ServerResponse ack = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("Unsubscribed from world state").build())
                    .build();
            return protocol.encodeResponse(ack);
        }

        Command com = Command.generate(req);
        ServerResponse response = this.world.perform(com);

        // Anything that went through the world may have changed it — let subscribers refresh.
        broadcastWorldState();

        return protocol.encodeResponse(response);
    }
    
    
    public synchronized  String doThisCommandUnRestricted(String data){ 
      
        this.logger.info("Server is requesting: "+data);
        Protocol protocol =new Protocol();
        ServerRequest req =  protocol.decodeRequest(data);
       
        if ((req.getCommand().equals("off"))||(req.getCommand().equals("shutdown"))||(req.getCommand().equals("quit"))){return "off";}
        Command com = Command.generate(req);
        com.setAsServerCommand();
        ServerResponse response = this.world.perform(com);
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
            
            if (state.getPosition() == null) state.setPosition(robot.getPosition());
            if (state.getDirection() == null) state.setDirection(robot.getDirection());
            if (state.getShields() == 0) state.setShields(robot.getShield());
            if (state.getShots() == 0) state.setShots(robot.getShoots());
        }
    }
  
}
