package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import za.co.wethinkcode.robots.server.commands.OperationalMode;

@Data
@AllArgsConstructor
public class ServerResponseState {
    
    @JsonProperty
    Position position;

    @JsonProperty
    Directions direction;

    @JsonProperty
    int shields;

    @JsonProperty
    int shots;

    @JsonProperty
    OperationalMode status;


}
