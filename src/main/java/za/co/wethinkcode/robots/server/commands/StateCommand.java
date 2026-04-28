package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.server.robot.Robot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class StateCommand extends Command {

    public StateCommand() {
        super("state");
    }

    @Override
    public boolean execute(Robot target, Iworld world) {

        System.out.println("Robot " + target.getName() + " is at position "
                + target.getPosition()
                + " facing " + target.getDirection());


        return true;
    }
}
