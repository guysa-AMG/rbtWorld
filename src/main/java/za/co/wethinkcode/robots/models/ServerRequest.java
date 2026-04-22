package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Value;
import za.co.wethinkcode.robots.server.commands.Command;



@Data
public class ServerRequest {

    public ServerRequest(String robot,String command,String[] args){
    this.robot = robot;
    this.command = command;
    this.arguments = args;
}

@JsonProperty(required = true)
private String robot;

@JsonProperty(required = true)
private String command;

@JsonProperty(required = true)
private String[] arguments;

public Command getCommandInstance(){
    return null;
}



}
