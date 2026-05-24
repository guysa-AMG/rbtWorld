package za.co.wethinkcode.robots.models.transitmodels;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
public class ServerRequest {

    public ServerRequest(String robot, String command, String[] args) {
        this.robot = robot;
        this.command = command;
        this.arguments = args;
    }
     public ServerRequest(String robot, String command) {
        this.robot = robot;
        this.command = command;
        this.arguments = new String[]{};
    }

    @JsonProperty(required = true)
    private String robot;

    @JsonProperty(required = true)
    private String command;

    @JsonProperty(required = true)
    private String[] arguments;



}
