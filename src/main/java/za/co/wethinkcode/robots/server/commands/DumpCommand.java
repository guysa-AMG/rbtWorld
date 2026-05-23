package za.co.wethinkcode.robots.server.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

import za.co.wethinkcode.robots.server.world.Iworld;

public class DumpCommand extends Command {

    public DumpCommand(String rbtName) {
        super("dump",rbtName);
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot target) {
        
        if (this.restricted){
        return this.restrictedServerResponse();
       }
       
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append("=== Robot Dump ===\n");
        String obstacles="\n\t";
        for (Obstacle obs: world.getObstacles()){
             obstacles+="kind: "+obs.getType()+"\n\t";
             obstacles+="position: "+obs.getPos()+"\n\t";
           }
        for(String botname: world.getAllRobots().keySet())
         {  
        BaseRobot robot = world.getAllRobots().get(botname);
        stringBuilder.append("Name: ").append(robot.getName()).append("\n");
        stringBuilder.append("Position: ").append(robot.getPosition()).append("\n");
        stringBuilder.append("Direction: ").append(robot.getDirection()).append("\n");
        stringBuilder.append("Shields: ").append(robot.getShields()).append("\n");
        stringBuilder.append("Shots: ").append(robot.getShoots()).append("\n");
         stringBuilder.append("------------------").append("\n");
        
        }
       stringBuilder.append("\n=== Command History ===\n");
        List<Command> commands = world.getHistoryOfCommands();
        for(Command command: commands)
       { 
        stringBuilder.append("Command: ").append(command.getCommandName()).append("\n");
        stringBuilder.append("caller: ").append(command.getRobotName()).append("\n");
        stringBuilder.append("arg: ").append(command.getArgument()).append("\n");
         stringBuilder.append("--------------------").append("\n");
        
    }
        stringBuilder.append("==================");
        stringBuilder.append("\n=== World Dump ===\n");
        stringBuilder.append("World size: ").append(world.getHeight()+"x").append(world.getWidth()).append("\n");
        stringBuilder.append("Obstacles: ").append(obstacles).append("\n");
      

        ServerResponseData data =  ServerResponseData.builder()
                                                          .message(stringBuilder.toString())
                                                          .build();
        
        ServerResponse res = ServerResponse.builder()
                                           .result(StatusCode.OK)
                                           .data(data)
                                           .build();
        return res;
    }

  



}