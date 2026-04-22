package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.wethinkcode.robots.server.commands.Command;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerRequest {

    @JsonProperty(required = true)
private String robot;

@JsonProperty(required = true)
private String command;

@JsonProperty(required = true)
private String[] arguments;



}
