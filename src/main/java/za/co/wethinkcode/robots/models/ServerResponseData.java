package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
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
    int shields;


}
