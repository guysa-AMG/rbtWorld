// # Abstract class or Interface
package za.co.wethinkcode.robots.server.commands;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.world.Iworld;

public abstract class Command {
    private String robotName;
    private String CommandName;
    private String[] argument;

    public String getCommandName(){
        return this.CommandName;
    }
    public String getRobotName(){
        return this.robotName;
    }
    public abstract ServerResponse execute(Iworld world);

    Command(String name,String[] arguemnt){
        this.CommandName=name;
        this.argument=argument;
    }
    Command(String name){
        this.CommandName = name;
        this.argument = argument;
    }
    
}