package za.co.wethinkcode.robots.server.commands.MovementCommand;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class BackCommand extends Command{



    public BackCommand( String[] argument, String rbtName) {
        super("back", argument, rbtName);
      
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        int steps = ForwardCommand.parseSteps(this.argument);
        Position startPos = robot.getPosition().copy();
        int livesBefore = robot.getLives();

        boolean moved = world.moveRobot(robot.getName(), -steps);
        boolean stillAlive = world.getAllRobots().containsKey(robot.getName());
        boolean diedThisMove = robot.getLives() < livesBefore;
        if (!stillAlive) {
            return ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("FELL_IN_PIT").position(robot.getPosition()).build())
                    .build();
        }
        if (diedThisMove) {
            return ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("RESPAWNED").position(robot.getPosition()).build())
                    .state(ServerResponseState.builder()
                            .position(robot.getPosition())
                            .direction(robot.getDirection())
                            .status(robot.getOperationState())
                            .shields(robot.getShield())
                            .shots(robot.getShoots())
                        
                            .build())
                    .build();
        }

        Position endPos = robot.getPosition();
        boolean travelled = !endPos.equals(startPos);
        if (travelled) robot.markMoved(); else robot.incrementBlocked();
        String message = (moved && travelled) ? "DONE" : "BLOCKED";

        ServerResponseData data = ServerResponseData.builder()
                                                    .message(message)
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