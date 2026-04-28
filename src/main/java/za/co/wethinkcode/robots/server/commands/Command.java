// # Abstract class or Interface
package za.co.wethinkcode.robots.server.commands;


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
    public abstract void execute();

    Command(String name,String[] argument){
        this.CommandName=name;
        this.argument=argument;
    }
    Command(String name){
        this.CommandName = name;
        this.argument = null;
    }
    
}