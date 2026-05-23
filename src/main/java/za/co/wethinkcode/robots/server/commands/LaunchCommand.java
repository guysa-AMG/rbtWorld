package za.co.wethinkcode.robots.server.commands;

import javax.swing.plaf.nimbus.State;

import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class LaunchCommand extends Command {

   
   
    LaunchCommand(String[] argument, String rbtNameString) {
        super("launch", argument, rbtNameString);
      
        
    }

    @Override
    public ServerResponse execute(Iworld world,BaseRobot robot) {
      world.addRobot(robotName);
      robot = world.getAllRobots().get(robotName);
        
      ServerResponseData data  = ServerResponseData.builder()
                                                   .position(robot.getPosition())
                                                   .visibility(Iworld.visibleDistance)
                                                   .reload(Iworld.reloadTime)
                                                   .repair(Iworld.repairTime)
                                                   .shields(robot.getShield())
                                                   .build();

                                        
      ServerResponseState state = ServerResponseState.builder()
                                                     .position(robot.getPosition())
                                                     .direction(robot.getDirection())
                                                     .shields(robot.getShield())
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
