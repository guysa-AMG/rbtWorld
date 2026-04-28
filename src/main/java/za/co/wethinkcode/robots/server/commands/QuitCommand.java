package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class QuitCommand extends Command {

    

    QuitCommand(String name, String rbtNameString) {
        super(name, rbtNameString);
        //TODO Auto-generated constructor stub
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

   
}
