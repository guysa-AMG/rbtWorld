package za.co.wethinkcode.robots.server.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import za.co.wethinkcode.robots.client.ConsoleInteraction;
import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.services.ITCService;

public class ReloadCommand extends Command {

    ReloadCommand( String rbtNameString) {
        super("reload", rbtNameString);
      
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
     
        try {
            Thread.sleep((long)world.RELOAD_TIME*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.reload();

       ServerResponseState state = ServerResponseState.builder()
                                                      .status(OperationalMode.RELOAD)
                                                      .position(robot.getPosition())
                                                      .shields(robot.getShields())
                                                      .direction(robot.getDirection())
                                                      .shots(robot.getShoots())
                                                      .build();
      ServerResponseData data = ServerResponseData.builder()
                                                  .message("DONE")
                                                  .visibility(Iworld.visibleDistance)
                                                  .reload(Iworld.RELOAD_TIME)
                                                  .repair(Iworld.REPAIR_TIME)
                                                  .state(state)
                                                  .build();
      return ServerResponse.builder()
                           .result(StatusCode.OK)
                           .data(data)
                           .build();
    }

 

   

    
}
