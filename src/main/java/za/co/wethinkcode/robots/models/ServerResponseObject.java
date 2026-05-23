package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
