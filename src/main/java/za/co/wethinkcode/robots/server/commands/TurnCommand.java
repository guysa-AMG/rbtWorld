package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class TurnCommand extends Command{



    TurnCommand( String[] argument, String rbtName) {
        super("turn", argument, rbtName);
      
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        Position pos = robot.getPosition();
        Position intendedPosition = pos.copy();
       
    
        String direction = this.argument[0].toLowerCase();
        
        switch(robot.getDirection()){
            case Directions.NORTH -> {
            if (direction.equals("left")){
                robot.updateDirection(Directions.EAST);
            }
            if(direction.equals("right")){
                 robot.updateDirection(Directions.WEST);
            }
            
            }

            case Directions.SOUTH -> {
                if (direction.equals("left")){
                robot.updateDirection(Directions.WEST);
            }
            if(direction.equals("right")){
                 robot.updateDirection(Directions.EAST);
            }
       
            }

            case Directions.EAST -> {
               if (direction.equals("left")){
                robot.updateDirection(Directions.SOUTH);
            }
            if(direction.equals("right")){
                 robot.updateDirection(Directions.NORTH);
            }
           
            }

            case Directions.WEST -> {
                   if (direction.equals("left")){
                robot.updateDirection(Directions.NORTH);
            }
            if(direction.equals("right")){
                 robot.updateDirection(Directions.SOUTH);
            }
         
            }
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