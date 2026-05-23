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

public class TurnCommand extends Command{



    public TurnCommand( String[] argument, String rbtName) {
        super("turn", argument, rbtName);
      
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        if (this.argument == null || this.argument.length == 0) {
            return ServerResponse.builder()
                    .result(StatusCode.ERROR)
                    .data(ServerResponseData.builder().message("turn requires an argument: left or right").build())
                    .build();
        }

        String direction = this.argument[0].toLowerCase();
        if (direction.equals("left")) {
            robot.turnLeft();
        } else if (direction.equals("right")) {
            robot.turnRight();
        } else {
            return ServerResponse.builder()
                    .result(StatusCode.ERROR)
                    .data(ServerResponseData.builder().message("turn argument must be 'left' or 'right', got: " + direction).build())
                    .build();
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