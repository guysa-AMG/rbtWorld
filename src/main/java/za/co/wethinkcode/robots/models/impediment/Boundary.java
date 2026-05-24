package za.co.wethinkcode.robots.models.impediment;
import java.awt.Color;
import java.awt.Graphics;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class Boundary extends Impediments {
    boolean vertical;
    public Boundary(Position position,boolean vertical) {
        super(position, "WALL"," ");
        this.vertical=vertical;
        int scale = WorldGenerator.MAP_SCALE;
        width =this.vertical?scale/2:scale;
        height = this.vertical?scale:scale/2;
     
    }
     public Boundary(Position position) {
        super(position, "WALL"," ");
        this.vertical=false;
    }

     @Override
    public void draw(Graphics g) {
       g.setColor(Color.white);
      
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX() +width/2;
       int ycord = scale * position.getY();
       
       g.fillRect(xcord, ycord, width, height);
       g.setColor(Color.BLACK);
        g.drawRect(xcord, ycord, width, height);
      
       
    }
    

    
}
