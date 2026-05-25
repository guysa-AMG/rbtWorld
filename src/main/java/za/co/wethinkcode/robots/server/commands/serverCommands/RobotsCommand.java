package za.co.wethinkcode.robots.server.commands.serverCommands;

import java.util.Map;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class RobotsCommand extends Command {

   
   
    public RobotsCommand( String rbtNameString) {
        super("robots", rbtNameString);
      
        
    }

    @Override
    public ServerResponse execute(Iworld world,BaseRobot robot) {

      if (this.restricted){
        return this.restrictedServerResponse();
       }

      Map<String,BaseRobot> robots = world.getAllRobots();
      StringBuilder listOfBots =  new StringBuilder();
      listOfBots.append("Robots Connected are :   ");

        robots.forEach((String name,BaseRobot bots)->{
            
           listOfBots.append(name+"  ");
        });
     
      ServerResponseData data  = ServerResponseData.builder()
      
                                                   .message(listOfBots.toString())
                                                   .build();

                                                

      ServerResponse res = ServerResponse.builder()
                                         .result(StatusCode.OK)
                                         .data(data)
                                    
                                         .build();
      return res;         
    }

   
}
