package za.co.wethinkcode.robots.server.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import za.co.wethinkcode.robots.client.ConsoleInteraction;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.services.ITCService;

public class HelpCommand extends Command {

    HelpCommand( String rbtNameString) {
        super("help", rbtNameString);
      
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
      ConsoleInteraction ci = new ConsoleInteraction();
      PrintStream origPrintStream = System.out;
      ByteArrayOutputStream capturedByteStream = new ByteArrayOutputStream();
      PrintStream captureStream = new PrintStream(capturedByteStream);

      System.setOut(captureStream);
      ci.displayHelp();
      System.setOut(origPrintStream);

      ServerResponseData data = ServerResponseData.builder()
                                                  .message(capturedByteStream.toString())
                                                  .build();
      return ServerResponse.builder()
                           .result(StatusCode.OK)
                           .data(data)
                           .build();
    }

 

   

    
}
