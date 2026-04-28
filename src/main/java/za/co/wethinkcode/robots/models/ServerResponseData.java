package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerResponseData {

    @JsonProperty
    String message;
    
    @JsonProperty
    Position position;

    @JsonProperty
    int visibility;

    @JsonProperty
    float reload;

    @JsonProperty
    float repair;

    @JsonProperty
    float shields;


}
