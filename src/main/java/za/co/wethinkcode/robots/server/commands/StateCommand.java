package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class StateCommand extends Command {

  
    StateCommand(String name, String rbtNameString) {
        super(name, rbtNameString);
     
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

  
}
