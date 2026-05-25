package za.co.wethinkcode.robots.server.commands;

import javax.swing.plaf.nimbus.State;

import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;


public class LaunchCommand extends Command {

   
   
    LaunchCommand(String[] argument, String rbtNameString) {
        super("launch", argument, rbtNameString);
      
        
    }

    @Override
    public ServerResponse execute(Iworld world,BaseRobot robot) {
        if (argument.length==0){
            ServerResponseData data = ServerResponseData.builder().message("please provide robot kind [offensive, defensive, balanced] or shield and shots").build();
            ServerResponse res = ServerResponse.builder().result(StatusCode.ERROR).data(data).build();
            return res;
        }
       if( argument.length >1)
       {
        int shield = Integer.parseInt(argument[0]);
        int shoots = Integer.parseInt(argument[1]);
        world.addRobot(robotName,shield,shoots,this.id);
       }
       if (argument.length==1){
      String kind =  argument[0].toLowerCase();
      switch (kind.toLowerCase()) {
        case "balanced" ->  world.addRobot(robotName,6,6,this.id);
        case "offensive" -> world.addRobot(robotName,3,9,this.id);
        case "defensive" -> world.addRobot(robotName,9,3,this.id);
        default -> world.addRobot(robotName,6,6,this.id);
      }
       }
      robot = world.getAllRobots().get(robotName);
        
      ServerResponseData data  = ServerResponseData.builder()
                                                   .position(robot.getPosition())
                                                   .visibility(Iworld.visibleDistance)
                                                   .reload(Iworld.RELOAD_TIME)
                                                   .repair(Iworld.REPAIR_TIME)
                                                   .shields(robot.getShields())
                                                   .build();

                                        
      ServerResponseState state = ServerResponseState.builder()
                                                     .position(robot.getPosition())
                                                     .direction(robot.getDirection())
                                                     .shields(robot.getShields())
                                                     .shots(robot.getShoots())
                                                     .status(OperationalMode.NORMAL)
                                                     .build();
                                                

      ServerResponse res = ServerResponse.builder()
                                         .result(StatusCode.OK)
                                         .data(data)
                                         .state(state)
                                         .build();
      return res;         
    }

   
}
