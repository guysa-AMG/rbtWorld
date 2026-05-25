package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;


public class LaunchCommand extends Command {

   
   
    LaunchCommand(String[] argument, String rbtNameString) {
        super("launch", argument, rbtNameString);
      
        
    }

    @Override
    public ServerResponse execute(Iworld world,BaseRobot robot) {
        if (argument.length==0){
            ServerResponseData data = ServerResponseData.builder()
                    .message("Launch needs arguments. Use one of:\n"
                            + "  <name> launch <kind>                e.g. HAL launch balanced\n"
                            + "  <name> launch <shield> <shots>      e.g. HAL launch 6 6\n"
                            + "  <name> launch <kind> <shield> <shots>\n"
                            + "Kinds: balanced (6,6), offensive (3,9), defensive (9,3)")
                    .build();
            ServerResponse res = ServerResponse.builder().result(StatusCode.ERROR).data(data).build();
            return res;
        }
       // Accept any of:  <kind>  |  <shield> <shots>  |  <kind> <shield> <shots>
       // Always take the LAST two args as shield/shots if they parse as ints, else fall back to kind.
       boolean launched = false;
       if (argument.length >= 2) {
           try {
               int shield = Integer.parseInt(argument[argument.length - 2]);
               int shoots = Integer.parseInt(argument[argument.length - 1]);
               world.addRobot(robotName, shield, shoots);
               launched = true;
           } catch (NumberFormatException ignored) {
               // last two args weren't numeric — try treating arg[0] as a kind name below
           }
       }
       if (!launched) {
           String kind = argument[0].toLowerCase();
           switch (kind) {
               case "balanced"  -> world.addRobot(robotName, 6, 6);
               case "offensive" -> world.addRobot(robotName, 3, 9);
               case "defensive" -> world.addRobot(robotName, 9, 3);
               default          -> world.addRobot(robotName, 6, 6);
           }
       }
      robot = world.getAllRobots().get(robotName);
        
      ServerResponseData data  = ServerResponseData.builder()
                                                   .position(robot.getPosition())
                                                   .visibility(Iworld.visibleDistance)
                                                   .reload(Iworld.RELOAD_TIME)
                                                   .repair(Iworld.REPAIR_TIME)
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
