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

public class RepairCommand extends Command {

    RepairCommand( String rbtNameString) {
        super("repair", rbtNameString);
      
    }
   @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
     
        try {
            Thread.currentThread().sleep((long)world.REPAIR_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.repair();

       ServerResponseState state = ServerResponseState.builder()
                                                      .status(OperationalMode.REPAIR)
                                                      .position(robot.getPosition())
                                                      .shields(robot.getShields())
                                                      .direction(robot.getDirection())
                                                      .shots(robot.getShoots())
                                                      .build();
      ServerResponseData data = ServerResponseData.builder()
                                                  .message("DONE")
                                                  .state(state)
                                                  .build();
      return ServerResponse.builder()
                           .result(StatusCode.OK)
                           .data(data)
                           .build();
    }

}