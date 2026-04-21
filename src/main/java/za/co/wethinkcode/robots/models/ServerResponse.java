package za.co.wethinkcode.robots.models;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.wethinkcode.robots.server.commands.OperationalMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {
    
    @JsonProperty(required = true)
    private StatusCode result;

    @JsonProperty(required = true)
    private Map<String,String> data;

    @JsonProperty
    private State state;

}

class State{

@JsonProperty
private ArrayList<Integer> position;

@JsonProperty
private Directions Direction;

@JsonProperty
private int shields;

@JsonProperty
private int shots;

@JsonProperty
private OperationalMode status;
}