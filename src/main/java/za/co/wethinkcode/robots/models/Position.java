package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Position {
    
    @JsonProperty("x")
    private int X;
    @JsonProperty("y")
    private int Y;
    
    public void incrementX(){
        this.X+=1;
    }
    public void incrementY(){
        this.Y+=1;
    }
     public void decrementX(){
        this.X-=1;
    }
    public void decrementY(){
        this.Y-=1;
    }
    
    
     public Position copy(){
          return new Position(this.X,this.Y);
     }
     public boolean equals(Position pos){
        return this.X==pos.X&&this.Y==pos.Y;
     }
}
