package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class StateCommand extends Command {

  
    StateCommand( String rbtNameString) {
        super("state", rbtNameString);
     
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        ServerResponseState state = ServerResponseState.builder()
                                                        .position(robot.getPosition())
                                                        .shields(robot.getShields())
                                                        .shots(robot.getShoots())
                                                        .status(robot.getOperationState())
                                                        .build();
                                                        
        ServerResponse res =  ServerResponse.builder()
                                            .state(state)
                                            .build();

        return res;
    }

  
}
