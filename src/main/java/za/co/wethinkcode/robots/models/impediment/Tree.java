package za.co.wethinkcode.robots.models.impediment;

import za.co.wethinkcode.robots.models.Position;

public class Tree implements Impediments {
     private Position pos;
     private String type;

     @Override
     public Position getPosition() {
        return this.pos;
     }

     @Override
     public void setPosition(Position pos) {
       this.pos=pos;
     }

     @Override
     public String getType() {
        return this.type;
     }
}
