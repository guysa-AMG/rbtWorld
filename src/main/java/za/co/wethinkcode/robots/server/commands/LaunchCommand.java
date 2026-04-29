package za.co.wethinkcode.robots.server.commands;

import javax.swing.plaf.nimbus.State;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
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
      return new ServerResponse(
        StatusCode.OK,
         new ServerResponseData(null,robot.getPosition(), Iworld.visibleDistance, Iworld.reloadTime, Iworld.repairTime, robot.getShield()),
        new ServerResponseState(robot.getPosition(), robot.getDirection(), robot.getShield(), robot.getShoots(), OperationalMode.NORMAL) );
    }

   
}
