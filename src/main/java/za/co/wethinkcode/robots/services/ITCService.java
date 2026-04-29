package za.co.wethinkcode.robots.services;


import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void setWorld(Iworld given_world){
        this.logger.info(".... Robot World initialized");
        this.world = given_world;
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
