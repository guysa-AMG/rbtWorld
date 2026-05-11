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
import za.co.wethinkcode.robots.models.impediment.Hole;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Mountain;
import za.co.wethinkcode.robots.models.impediment.Rocks;
import za.co.wethinkcode.robots.models.impediment.Tree;
import za.co.wethinkcode.robots.server.commands.Command;

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
   private volatile static ITCService instance = new ITCService();
   private volatile Logger logger = LoggerFactory.getLogger(ITCService.class);
   private volatile Iworld world;

    private ITCService(){
        this.threads=new HashMap<>();
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
       
        return protocol.encodeResponse(response);
    }
  
}
