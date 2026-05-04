package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class BackCommand extends Command{



    BackCommand( String[] argument, String rbtName) {
        super("forward", argument, rbtName);
      
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        Position pos = robot.getPosition();
        
        switch(robot.getDirection()){
            case Directions.NORTH -> pos.decrementY();

            case Directions.SOUTH -> pos.incrementY();

            case Directions.EAST -> pos.incrementX();

            case Directions.WEST -> pos.decrementX();

            
        }
        
        if (world.isPositionAvailable(pos)){
            world.moveRobot(robot.getName(), pos);
        }

        ServerResponseData data = ServerResponseData.builder()
                                                    .message("DONE")
                                                    .build();

        ServerResponseState state = ServerResponseState.builder()
                                                       .position(robot.getPosition())
                                                       .direction(robot.getDirection())
                                                       .status(robot.getOperationState())
                                                       .shields(robot.getShield())
                                                       .build();

        ServerResponse res = ServerResponse.builder()
                                           .result(StatusCode.OK)
                                           .data(data)
                                           .state(state)
                                           .build();
        return res;
        
        

       
    }
}