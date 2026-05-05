package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class LookCommand extends  Command{

  

    LookCommand(String name, String rbtNameString) {
        super(name, rbtNameString);
        //TODO Auto-generated constructor stub
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        //TODO Implement the whole look logic 





        ServerResponseData data  = ServerResponseData.builder()
                                                 
                                                   .position(robot.getPosition())
                                                   .visibility(Iworld.visibleDistance)
                                                   .reload(Iworld.reloadTime)
                                                   .repair(Iworld.repairTime)
                                                   .shields(robot.getShield())
                                                   .build();

                                        
      ServerResponseState state = ServerResponseState.builder()
                                                     .position(robot.getPosition())
                                                     .direction(robot.getDirection())
                                                     .shields(robot.getShield())
                                                     .shots(robot.getShoots())
                                                     .status(OperationalMode.NORMAL)
                                                     .build();
                                                

      ServerResponse res = ServerResponse.builder()
                                         .result(StatusCode.OK)
                                         .data(data)
                                         .state(state)
                                         .build();
      return res;         
                                        
 
    }

    
    
    
}


