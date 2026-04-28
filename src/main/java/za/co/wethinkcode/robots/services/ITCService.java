package za.co.wethinkcode.robots.services;


import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;
import org.slf4j.*;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.shared.Protocol;

public class ITCService {
   private volatile static ITCService instance = new ITCService();
   private volatile Logger logger = LoggerFactory.getLogger(ITCService.class);
   private volatile Iworld world;

    private ITCService(){
   
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
       Command com = Command.generate(req);
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
