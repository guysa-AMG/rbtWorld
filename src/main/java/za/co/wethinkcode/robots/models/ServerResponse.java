package za.co.wethinkcode.robots.models;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value=Include.NON_NULL)
public class ServerResponse {
    
    @JsonProperty(required = true)
    private StatusCode result;

    @JsonProperty(required = true)
    private ServerResponseData data;

    @JsonProperty
    private ServerResponseState state;
    

    public boolean equals(ServerResponse req){
    return this.result.equals(req.result)&&this.data.equals(req.data)&&this.state.equals(req.state);
}
}
