package za.co.wethinkcode.robots.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.nimbus.State;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseObject;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class RobotsCommand extends Command {

   
   
    RobotsCommand( String rbtNameString) {
        super("robots", rbtNameString);
      
        
    }

    @Override
    public ServerResponse execute(Iworld world,BaseRobot robot) {

      if (this.restricted){
        return this.restrictedServerResponse();
       }
      world.addRobot(robotName);
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
