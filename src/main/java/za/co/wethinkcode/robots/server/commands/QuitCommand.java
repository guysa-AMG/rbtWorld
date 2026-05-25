package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class QuitCommand extends Command {

    public QuitCommand(String[] args, String rbtName) {
        super("quit", args, rbtName);
    }

    public QuitCommand(String rbtName) {
        super("quit", rbtName);
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        world.removeRobot(robot.getName());
        return null;
    }
}
