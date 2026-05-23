package za.co.wethinkcode.robots.models.transitmodels;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.ImpedimentType;
import za.co.wethinkcode.robots.models.Position;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class ServerResponseObject {

    @JsonProperty
    Directions direction;

    @JsonProperty
    ImpedimentType type;

    @JsonProperty
    int distance;

    @JsonProperty
    String subtype;

    @JsonProperty
    Position position;

    @JsonProperty
    String name;

}
