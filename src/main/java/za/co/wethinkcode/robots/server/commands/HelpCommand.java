package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.services.ITCService;

public class HelpCommand extends Command {

    

    HelpCommand(String name, String rbtNameString) {
        super("help", rbtNameString);
        //TODO Auto-generated constructor stub
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {

      return null;
    }

 

   

    
}
