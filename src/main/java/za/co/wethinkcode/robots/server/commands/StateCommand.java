package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class StateCommand extends Command {

  
    StateCommand(String name, String rbtNameString) {
        super(name, rbtNameString);
     
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        ServerResponseState state = ServerResponseState.builder()
                                                        .position(robot.getPosition())
                                                        .shields(robot.getShield())
                                                        .shots(robot.getShoots())
                                                        .status(robot.getOperationState())
                                                        .build();
                                                        
        ServerResponse res =  ServerResponse.builder()
                                            .state(state)
                                            .build();

        return res;
    }

  
}
