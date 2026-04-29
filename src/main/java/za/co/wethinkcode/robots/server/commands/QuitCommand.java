package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.robot.Robot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class QuitCommand extends Command {

    public QuitCommand() {
        super("quit");
    }

    @Override
    public boolean execute(Robot target, Iworld world) {

        world.removeRobot(target.getName());

        System.out.println("Robot " + target.getName() + " has quit.");
        return true;
    }


}