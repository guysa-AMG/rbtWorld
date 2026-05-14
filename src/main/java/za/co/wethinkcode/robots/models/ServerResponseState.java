package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.wethinkcode.robots.server.commands.OperationalMode;

@Builder
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
    int lives;

    @JsonProperty
    int kills;

    @JsonProperty
    OperationalMode status;
    public boolean equals(ServerResponseState req){
    return this.direction.equals(req.direction)&&this.position.equals(req.position)&&this.status==req.status&&this.shields==req.shields&&this.shots==req.shots;
}

}
