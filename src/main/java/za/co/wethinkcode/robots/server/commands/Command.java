// # Abstract class or Interface
package za.co.wethinkcode.robots.server.commands;


import za.co.wethinkcode.robots.server.robot.Robot;
import za.co.wethinkcode.robots.server.world.Iworld;

public abstract class Command {
    private String robotName;
    private String CommandName;
    private String[] argument;

    public String getCommandName() {
        return this.CommandName;
    }

    public String getRobotName() {
        return this.robotName;
    }

    Command(String name, String[] argument) {
        this.CommandName = name;
        this.argument = argument;
    }

    Command(String name) {
        this.CommandName = name;
        this.argument = null;
    }

    public abstract boolean execute(Robot target, Iworld world);
}

