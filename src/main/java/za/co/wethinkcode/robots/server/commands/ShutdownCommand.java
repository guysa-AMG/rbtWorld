package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.robot.Robot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        super("shutdown");
    }

    @Override
    public boolean execute(Robot target, Iworld world) {
        System.out.println("Shutting down the server...");
        System.exit(0);
        return true;
    }


}


