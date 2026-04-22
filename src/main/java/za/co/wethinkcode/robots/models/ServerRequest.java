package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import za.co.wethinkcode.robots.server.commands.Command;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ServerRequest {

    public ServerRequest(String robot, String command, String[] args) {
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


 
    public String getCommandInstance() {
        return null;
    }

   


}
