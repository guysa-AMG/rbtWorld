package za.co.wethinkcode.robots.services;

import java.util.logging.Logger;
import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;

import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.shared.Protocol;

public class ITCService {
   private static ITCService instance = new ITCService();
   private volatile Logger log;
   private volatile Iworld world;

    private ITCService(){
        this.log =  Logger.getLogger("Robot Service");
    }
    
    public void setWorld(Iworld given_world){
        this.world = given_world;
    }

    public static  ITCService getInstance(){
        return instance;
    }
   

    public synchronized  String doThisCommand(String data){ 
        this.log.info("Command recieved: "+data);
        Protocol protocol =new Protocol();
        ServerRequest req =  protocol.decodeRequest(data);
        Command com = req.getCommandInstance();
        ServerResponse response = this.world.perform(com);
        System.out.println(response);
        return protocol.encodeResponse(response);

       
    }
    public synchronized void  getAllPlayers(){}
    //must implement
    public Boolean isValid(){

        return true;
    }
   

}
