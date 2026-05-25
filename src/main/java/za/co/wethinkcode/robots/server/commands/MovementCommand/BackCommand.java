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

    static int parseSteps(String[] args) {
        if (args == null || args.length == 0) return 1;
        try {
            return Math.max(0, Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            return 1;
        }
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

            if (dir == Directions.NORTH) stepY -= multiplier;
            else if (dir == Directions.SOUTH) stepY += multiplier;
            else if (dir == Directions.EAST) stepX -= multiplier;
            else if (dir == Directions.WEST) stepX += multiplier;

            if (stepX > xLimit || stepX < -xLimit || stepY > yLimit || stepY < -yLimit) {
                fullyMoved = false;
                break;
            }

            if (world.isPositionBlocked(stepX, stepY)) {
                fullyMoved = false;
                break;
            }

          

            nextX = stepX;
            nextY = stepY;
        }
        robot.updatePosition(new Position(nextX, nextY));
      //  world.consumePickupAt(nextX, nextY, robot);
        world.updateRobot(name, robot);
        return fullyMoved;
    }

    public boolean move(BaseRobot robot,int steps,RobotWorld world){
      Position pos=  robot.getPosition().copy();
     Position old=  robot.getPosition().copy();
      Directions dir = robot.getDirection();
     
       for (int i = 1; i <= Math.abs(steps); i++) {
        if (dir == Directions.NORTH) pos.incrementY();
            else if (dir == Directions.SOUTH) pos.decrementY();
            else if (dir == Directions.EAST) pos.decrementX();
            else if (dir == Directions.WEST) pos.incrementX();
            if(world.isPositionAvailable(pos)){
                world.swapePosition(pos,robot.getPosition().copy());
            }
            else{ return false; }
       }
       return !robot.getPosition().equals(old);
    }
    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        int steps = parseSteps(this.argument);

        boolean moved = move(robot, steps,(RobotWorld)world);
        
        String message = moved ? "DONE" : "BLOCKED";

        ServerResponseData data = ServerResponseData.builder()
                                                    .message(message)
                                                    .build();

        ServerResponseState state = ServerResponseState.builder()
                                                       .position(robot.getPosition())
                                                       .direction(robot.getDirection())
                                                       .status(robot.getOperationState())
                                                       .shields(robot.getShield())
                                                       .shots(robot.getShoots())
                                                       .build();

        ServerResponse res = ServerResponse.builder()
                                           .result(StatusCode.OK)
                                           .data(data)
                                           .state(state)
                                           .build();
        return res;
        
        

       
    }
}