package za.co.wethinkcode.robots.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonDeserialize(using = PositionDeserializer.class)
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
    public int getStraightDistance(Position other){
       Position distance= getDistance(other);

        if (distance.X==0){
            return distance.Y;
        }
        if (distance.Y==0){
            return distance.X;
        }return -1;
     
    
    }

    public Position getDistance(Position other){
        return new Position(Math.abs(this.X-other.X),Math.abs(this.Y-other.Y));
    }
    
     public Position copy(){
          return new Position(this.X,this.Y);
     }
     public boolean equals(Position pos){
        return this.X==pos.X&&this.Y==pos.Y;
     }
}
