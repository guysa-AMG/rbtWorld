// # Abstract class or Interface
package za.co.wethinkcode.robots.server.commands;


import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.server.commands.MovementCommand.BackCommand;
import za.co.wethinkcode.robots.server.commands.MovementCommand.ForwardCommand;
import za.co.wethinkcode.robots.server.commands.MovementCommand.TurnCommand;
import za.co.wethinkcode.robots.server.commands.serverCommands.DumpCommand;
import za.co.wethinkcode.robots.server.commands.serverCommands.RobotsCommand;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;


public abstract class Command {
    protected String robotName;
    protected String CommandName;
    protected String[] argument;
    protected String attribute;
    protected boolean restricted =true;
    public String getCommandName(){
        return this.CommandName;
    }
    public void setRobotName(String name){
        this.robotName=name;
    }
    public String getAttribute(){
        return this.attribute;
    }
    public String[] getArgument(){
        return this.argument;
    }
    public void setAttribute(String data){
        this.attribute = data;
    }

    public String getRobotName(){
        return this.robotName;
    }
    public abstract ServerResponse execute(Iworld world,BaseRobot robot);

    protected Command(String name,String[] argument,String rbtName){
        this.CommandName=name;
        this.robotName=rbtName;
        this.argument=argument;
    }
    Command(String[] argument,String rbtNameString){
        this(null,argument,rbtNameString);
    }
   
    protected Command(String name,String rbtNameString){
        this(name,null,rbtNameString);
    }
   public static Command generate(ServerRequest req){
  

    return switch(req.getCommand()){

        case "launch" -> new LaunchCommand(req.getArguments(),req.getRobot());
        case "state"  -> new StateCommand(req.getRobot());
        case "robots" -> new RobotsCommand( req.getRobot());
        case "turn"   -> new TurnCommand( req.getArguments(), req.getRobot());
        case "look"   -> new LookCommand(req.getRobot());
        case "dump"   -> new DumpCommand(req.getRobot());
        case "fire"   -> new FireCommand( req.getArguments(), req.getRobot());
        case "forward"-> new ForwardCommand( req.getArguments(), req.getRobot());
        case "back"   -> new BackCommand( req.getArguments(), req.getRobot());
        case "help"   -> new HelpCommand(req.getRobot());
        

        default -> new HelpCommand(req.getRobot());
    };

    
    }
    public void setAsServerCommand(){
        this.restricted=false;
    }

    public  boolean restricted(){
        return this.restricted;
     }
    public ServerResponse restrictedServerResponse(){
        return ServerResponse.builder().result(StatusCode.ERROR).data(ServerResponseData.builder().message("command reserved for serverside").build()).build();
    }

  
}
