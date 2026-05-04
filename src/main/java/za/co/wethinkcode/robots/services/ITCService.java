package za.co.wethinkcode.robots.services;


import za.co.wethinkcode.robots.models.Position;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;

import org.slf4j.*;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.shared.Protocol;

public class ITCService {
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
       if (( raw_map =prop.get("robotWorld"))!=null){
            String[] rows = raw_map.toString().split(",");
          
            for (String row: rows){
                char[] column = row.toLowerCase().toCharArray();
                ArrayList<Impediments> temp = new ArrayList<>();
                for (char ch:column){
                    Impediments obj=null;
                    if (ch=='h'){
                    obj= new Hole();
                    }
                     if (ch=='r'){
                    obj= new Rocks();
                    }
                     if (ch=='t'){
                    obj= new Tree();
                    }
                     if (ch=='m'){
                    obj= new Mountain();
                    }
                  
                    temp.add(obj);
               
                }
           
                new_map.add(temp);

            }
       }
    }
        return new_map;
    }
    public void setWorld(Iworld given_world){
        this.logger.info(".... Robot World initialized");
        this.world = given_world;
         ArrayList<ArrayList<Impediments>> map = parseMap("world.properties");
        this.world.loadMap(map);
    }

    public static  ITCService getInstance(){
        return instance;
    }
   

    public synchronized  String doThisCommand(String data){ 
        
        this.logger.info("Command recieved: "+data);
        Protocol protocol =new Protocol();
        ServerRequest req =  protocol.decodeRequest(data);
        this.logger.error(req.getCommand());
        if ((req.getCommand().equals("off"))||(req.getCommand().equals("shutdown"))||(req.getCommand().equals("quit"))){
            return "off";
        }
        Command com = Command.generate(req);
       
        ServerResponse response = this.world.perform(com);
       
        return protocol.encodeResponse(response);

       
    }
    public synchronized void  getAllPlayers(){}
    //must implement
    public Boolean isValid(){

        return true;
    }
   

}
