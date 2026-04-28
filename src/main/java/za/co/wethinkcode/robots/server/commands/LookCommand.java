package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.robot.Robot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class LookCommand extends Command {

    public LookCommand() {
        super("look");
    }

    @Override
    public boolean execute(Robot target, Iworld world) {

        System.out.println("Robot " + target.getName() + " is looking around.");

        return true;
    }
}


