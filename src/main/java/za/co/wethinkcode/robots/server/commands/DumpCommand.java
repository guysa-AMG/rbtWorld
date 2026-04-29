package za.co.wethinkcode.robots.server.commands;

import java.util.Arrays;
import za.co.wethinkcode.robots.server.robot.Robot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class DumpCommand extends Command {

    public DumpCommand() {
        super("dump");
    }

    @Override
    public boolean execute(Robot target, Iworld world) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("=== Robot Dump ===\n");
        stringBuilder.append("Name: ").append(target.getName()).append("\n");
        stringBuilder.append("Position: ").append(target.getPosition()).append("\n");
        stringBuilder.append("Direction: ").append(target.getDirection()).append("\n");
        stringBuilder.append("Shields: ").append(target.getShields()).append("\n");
        stringBuilder.append("Shots: ").append(target.getFireRate()).append("\n");

        stringBuilder.append("\n=== World Dump ===\n");
        stringBuilder.append("World size: ").append(world.getSize()).append("\n");
        stringBuilder.append("Obstacles: ").append(world.getObstacles()).append("\n");
        stringBuilder.append("Robots: ").append(world.getRobotNames()).append("\n");
        stringBuilder.append("\n=== Command Metadata ===\n");
        stringBuilder.append("Command: ").append(getCommandName()).append("\n");
        //stringBuilder.append("Arguments: ").append(Arrays.toString(getArguments())).append("\n");
        stringBuilder.append("==================");

        target.sendMessage(stringBuilder.toString());
        return true;
    }




}