package za.co.wethinkcode.robots.models.transitmodels;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.Position;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class ServerResponseRobot {

    @JsonProperty
    String name;

    @JsonProperty
    Position position;

    @JsonProperty
    Directions direction;

    @JsonProperty
    int lives;

    @JsonProperty
    int shields;

    @JsonProperty
    int shots;

    @JsonProperty
    int kills;

    @JsonProperty
    OperationalMode status;
}
