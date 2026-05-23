package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class QuitCommand extends Command {

    public QuitCommand(String robotName) {
        super("quit",robotName);
    }

  
    @Override
    public ServerResponse execute(Iworld world, BaseRobot target){

        world.removeRobot(target.getName());

        System.out.println("Robot " + target.getName() + " has quit.");
        return null;
    }

}
