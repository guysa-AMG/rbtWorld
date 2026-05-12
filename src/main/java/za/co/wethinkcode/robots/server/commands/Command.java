// # Abstract class or Interface
package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

import za.co.wethinkcode.robots.server.world.Iworld;

public abstract class Command {
    protected String robotName;
    protected String CommandName;
    protected String[] argument;
    protected String attribute;
    public String getCommandName(){
        return this.CommandName;
    }
    public void setRobotName(String name){
        this.robotName=name;
    }
    public String getAttribute(){
        return this.attribute;
    }
    public void setAttribute(String data){
        this.attribute = data;
    }

    public String getRobotName(){
        return this.robotName;
    }
    public abstract ServerResponse execute(Iworld world,BaseRobot robot);

    Command(String name,String[] argument,String rbtName){
        this.CommandName=name;
        this.robotName=rbtName;
        this.argument=argument;
    }
    Command(String[] argument,String rbtNameString){
        this(null,argument,rbtNameString);
    }
   
    Command(String name,String rbtNameString){
        this(name,null,rbtNameString);
    }
   public static Command generate(ServerRequest req){
    System.out.println(req.getCommand());
    return switch(req.getCommand()){

        case "launch" -> new LaunchCommand(req.getArguments(),req.getRobot());

        case "state" -> new StateCommand("state",req.getRobot());
        case "robots" -> new RobotsCommand( req.getRobot());
        case "turn" -> new TurnCommand( req.getArguments(), req.getRobot());

        case "fire" -> new FireCommand( req.getArguments(), req.getRobot());

        case "forward" -> new ForwardCommand( req.getArguments(), req.getRobot());

        case "back" -> new BackCommand( req.getArguments(), req.getRobot());

        default -> new HelpCommand("help",req.getRobot());
    };

    
        
    }

  
}
