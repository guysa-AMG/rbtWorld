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
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class BackCommand extends Command{



    public BackCommand( String[] argument, String rbtName) {
        super("back", argument, rbtName);
      
    }
       public boolean moveRobot(String name, int steps,RobotWorld world) {
        BaseRobot robot = world.getAllRobots().get(name);
        if (robot == null) return false;
        Position currentPos = robot.getPosition();
        Directions dir = robot.getDirection();

        if (currentPos == null || dir == null) return false;
        if (steps == 0) return true;

        int multiplier = (steps > 0) ? 1 : -1;
        int nextX = currentPos.getX();
        int nextY = currentPos.getY();
        boolean fullyMoved = true;

        int xLimit = (world.getWidth() - 1) / 2;
        int yLimit = (world.getHeight() - 1) / 2;

        for (int i = 1; i <= Math.abs(steps); i++) {
            int stepX = nextX;
            int stepY = nextY;

            if (dir == Directions.NORTH) stepY += multiplier;
            else if (dir == Directions.SOUTH) stepY -= multiplier;
            else if (dir == Directions.EAST) stepX += multiplier;
            else if (dir == Directions.WEST) stepX -= multiplier;

            if (stepX > xLimit || stepX < -xLimit || stepY > yLimit || stepY < -yLimit) {
                fullyMoved = false;
                break;
            }

            if (world.isPositionBlocked(stepX, stepY)) {
                fullyMoved = false;
                break;
            }

            if (world.isPositionInPit(stepX, stepY)) {
                robot.updatePosition(new Position(stepX, stepY));
                int remaining = robot.decrementLives();
                if (remaining > 0) {
                    Position spawn = world.newSpawnPoint();
                    robot.respawnAt(spawn);
                    world.updateRobot(name, robot);
                } else {
                    world.removeRobot(name);
                }
                return true;
            }

            nextX = stepX;
            nextY = stepY;
        }
        robot.updatePosition(new Position(nextX, nextY));
       // world.consumePickupAt(nextX, nextY, robot);
        world.updateRobot(name, robot);
        return fullyMoved;
    }



    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        int steps = ForwardCommand.parseSteps(this.argument);
        Position startPos = robot.getPosition().copy();
        int livesBefore = robot.getLives();

        boolean moved = moveRobot(robot.getName(), -steps,(RobotWorld)world);
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