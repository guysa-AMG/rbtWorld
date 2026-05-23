package za.co.wethinkcode.robots.server.commands;

import javax.swing.plaf.nimbus.State;

import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class ErrorCommand extends Command {

   
   
    ErrorCommand(String[] argument, String rbtNameString) {
        super("error", argument, rbtNameString);
      
        
    }
public ErrorCommand(String errMessage,String rbtName){
    super("error",rbtName);
    this.attribute = errMessage;

}
    @Override
    public ServerResponse execute(Iworld world,BaseRobot robot) {
    
      ServerResponseData data =  ServerResponseData.builder()
                                                    .message(this.attribute)
                                                    .build();
      ServerResponse res = ServerResponse.builder()
                                         .result(StatusCode.ERROR)
                                         .data(data)
                                         .build();
      return res;         
    }

   
}
