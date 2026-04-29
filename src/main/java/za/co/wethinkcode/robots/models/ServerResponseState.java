package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.wethinkcode.robots.server.commands.OperationalMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
    
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
