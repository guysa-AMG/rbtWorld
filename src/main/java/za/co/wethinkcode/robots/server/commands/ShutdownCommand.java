package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

import za.co.wethinkcode.robots.server.world.Iworld;

public class ShutdownCommand extends Command {

    public ShutdownCommand(String rbtName) {
        super("shutdown",rbtName);
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }


   

}


