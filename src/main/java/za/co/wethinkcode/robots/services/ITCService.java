package za.co.wethinkcode.robots.services;

import java.util.logging.Logger;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.shared.Protocol;

public class ITCService {
   private static ITCService instance = new ITCService();
   private Logger log;
   private Iworld world;

    private ITCService(){
        this.log =  Logger.getLogger("Robot Service");
    }

    public static  ITCService getInstance(){
        return instance;
    }
    public void addRobot(){
        
    }

    public void deserialize(String data){
        ServerRequest req = new Protocol().decodeRequest(null);
        
        req.getCommand();

    }

     public String doThisCommand(String data){
        Protocol protocol =new Protocol();
        ServerRequest req =  protocol.decodeRequest(data);
        Command com = req.getCommand();
        ServerResponse response = this.world.perform(com);
        System.out.println(response);
        return protocol.encodeResponse(response);

       
    }
    public void  getAllPlayers(){}
    //must implement
    public Boolean isValid(){

        return true;
    }
    public void execute(){

    }

}
