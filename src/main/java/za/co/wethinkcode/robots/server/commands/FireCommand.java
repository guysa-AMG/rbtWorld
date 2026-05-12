package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class FireCommand extends Command{

    FireCommand(String[] arguStrings, String rbtNameString) {
        super("fire", arguStrings,rbtNameString   );
        //TODO Auto-generated constructor stub
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        BaseRobot victim = world.getFireable(robot);

        robot.decrementBullets();
        if (victim != null){
            int distance = robot.getPosition().getStraightDistance(victim.getPosition());
            int damagePower = Iworld.MAG_MAX-distance;
            if (victim.inflictDamage(damagePower))
            {
            
            ServerResponseState victimState = ServerResponseState.builder()
                                                            .position(victim.getPosition())
                                                            .direction(victim.getDirection())
                                                            .shields(victim.getShields())
                                                            .shots(victim.getShoots())
                                                            .status(victim.getOperationState())
                                                            .build();

            ServerResponseData data =  ServerResponseData.builder()
                                                        .message("Hit")
                                                        .distance(distance)
                                                        .robot(victim.getName())
                                                        .state(victimState)
                                                        .build();

          ServerResponseState state = ServerResponseState.builder()
                                                        .shields(robot.getShield())
                                                        .shots(robot.getShoots())
                                                        .build();

            ServerResponse hitRes = ServerResponse.builder()
                                               .result(StatusCode.OK)
                                               .data(data)
                                               .state(state)            
                                               .build();    
            return hitRes;}
        }
         ServerResponse missRes = ServerResponse.builder()
                                               .result(StatusCode.OK)
                                               .data(ServerResponseData.builder()
                                                                       .message("Miss")
                                                                       .build())
                                               .state(ServerResponseState.builder()
                                                                         .shields(robot.getShield())
                                                                         .shots(robot.getShoots())
                                                                         .build() )            
                                               .build();    
            return missRes;

      }
    
}