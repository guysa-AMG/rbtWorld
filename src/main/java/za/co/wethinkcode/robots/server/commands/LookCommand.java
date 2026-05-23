package za.co.wethinkcode.robots.server.commands;

import java.util.ArrayList;
import java.util.List;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.ImpedimentType;
import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseObject;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class LookCommand extends Command {

    public LookCommand( String rbtNameString) {
        super("look", rbtNameString);
    }

   public List<ServerResponseObject> lookAround(RobotWorld world){
        List<ServerResponseObject> out = new ArrayList<>();
        String name =this.robotName;
        BaseRobot robot = world.getAllRobots().get(name);
        if (robot == null || robot.getPosition() == null) return out;
        Position pos = robot.getPosition();

        int xLimit = (world.getWidth() - 1) / 2;
        int yLimit = (world.getHeight() - 1) / 2;

        for (Directions dir : Directions.values()) {
            int dx = (dir == Directions.EAST) ? 1 : (dir == Directions.WEST) ? -1 : 0;
            int dy = (dir == Directions.NORTH) ? 1 : (dir == Directions.SOUTH) ? -1 : 0;
            for (int dist = 1; dist <= Iworld.lookRange; dist++) {
                int sx = pos.getX() + dx * dist;
                int sy = pos.getY() + dy * dist;

                if (sx < -xLimit || sx > xLimit || sy < -yLimit || sy > yLimit) {
                    out.add(ServerResponseObject.builder()
                            .direction(dir).distance(dist)
                            .type(ImpedimentType.EDGE)
                            .subtype("EDGE")
                            .position(new Position(sx, sy))
                            .build());
                    break;
                }

                BaseRobot other = world.robotAtCell(sx, sy, robot);
                if (other != null) {
                    out.add(ServerResponseObject.builder()
                            .direction(dir).distance(dist)
                            .type(ImpedimentType.ROBOT)
                            .subtype("ROBOT")
                            .name(other.getName())
                            .position(new Position(sx, sy))
                            .build());
                    break;
                }

                String hitType = world.obstacleTypeAt(sx, sy);
                if (hitType != null) {
                    out.add(ServerResponseObject.builder()
                            .direction(dir).distance(dist)
                            .type(ImpedimentType.OBSTACLE)
                            .subtype(hitType)
                            .position(new Position(sx, sy))
                            .build());
                    if ("MOUNTAIN".equals(hitType) || "WALL".equals(hitType)) break;
                }
            }
        }
        return out;
    }


    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        List<ServerResponseObject> objects;
        if (world instanceof RobotWorld rw) {
            objects = lookAround((RobotWorld)world);
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
