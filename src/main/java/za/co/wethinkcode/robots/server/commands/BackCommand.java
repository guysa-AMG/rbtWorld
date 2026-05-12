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
        super("back", argument, rbtName);
      
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        Position pos = robot.getPosition();
        Position intendedPosition = pos.copy();
       
    
        int steps = Integer.parseInt(this.argument[0]);
      
        switch(robot.getDirection()){
            case Directions.NORTH -> {
                intendedPosition.setY(intendedPosition.getY()+steps);
                Position testPosition = pos.copy();
                while(!intendedPosition.equals(pos))
                {testPosition.incrementY();
                if (world.isPositionAvailable(testPosition)){
                    pos.incrementY();
                }
                else{
                    break;
                }
            }
         
            
            }

            case Directions.SOUTH -> {
                intendedPosition.setY(intendedPosition.getY()-steps);
                Position testPosition = pos.copy();
                while(!intendedPosition.equals(pos))
                {testPosition.decrementY();
                if (world.isPositionAvailable(testPosition)){
                    pos.decrementY();
                }
                else{
                    break;
                }
            }
       
            }

            case Directions.WEST -> {
                intendedPosition.setX(intendedPosition.getX()+steps);
                Position testPosition = pos.copy();
                while(!intendedPosition.equals(pos))
                {testPosition.incrementX();
                if (world.isPositionAvailable(testPosition)){
                    pos.incrementX();
                }
                else{
                    break;
                }
            }
           
            }

            case Directions.EAST -> {
                intendedPosition.setX(intendedPosition.getX()-steps);
                Position testPosition = pos.copy();
                while(!intendedPosition.equals(pos))
                {testPosition.decrementX();
                if (world.isPositionAvailable(testPosition)){
                    pos.decrementX();
                }
                else{
                    break;
                }
            }
         
            }
        }
        
     

        ServerResponseData data = ServerResponseData.builder()
                                                    .message(intendedPosition.equals(robot.getPosition())?"DONE":"BLOCKED")
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