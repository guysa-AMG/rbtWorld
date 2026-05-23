package za.co.wethinkcode.robots.server.commands;

import java.util.List;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseObject;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class LookCommand extends Command {

    LookCommand(String name, String rbtNameString) {
        super(name, rbtNameString);
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        List<ServerResponseObject> objects;
        if (world instanceof RobotWorld rw) {
            objects = rw.lookAround(robot.getName());
        } else {
            objects = List.of();
        }

        ServerResponseData data = ServerResponseData.builder()
                .message(objects.isEmpty() ? "Nothing in sight" : "OK")
                .objects(objects)
                .visibility(Iworld.lookRange)
                .position(robot.getPosition())
                .build();

        ServerResponseState state = ServerResponseState.builder()
                .position(robot.getPosition())
                .direction(robot.getDirection())
                .shields(robot.getShield())
                .shots(robot.getShoots())
                .status(robot.getOperationState() == null ? OperationalMode.NORMAL : robot.getOperationState())
                .build();

        return ServerResponse.builder()
                .result(StatusCode.OK)
                .data(data)
                .state(state)
                .build();
    }
}
