package za.co.wethinkcode.robots.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class ServerResponseData {
    
    @JsonProperty
    List<ServerResponseObject> objects;

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

    public boolean equals(ServerResponseData req){
    return this.message.equals(req.message)&&this.position.equals(req.position)&&this.reload==req.reload&&this.repair==req.repair&&this.shields==req.shields&&this.visibility==req.visibility;
}

}
