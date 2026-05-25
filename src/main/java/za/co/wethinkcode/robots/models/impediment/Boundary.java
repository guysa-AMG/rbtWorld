package za.co.wethinkcode.robots.models.impediment;
import java.awt.Color;
import java.awt.Graphics;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class Boundary extends Impediments {
  
    public Boundary(Position position) {
        super(position, "WALL"," ");
        int scale = WorldGenerator.MAP_SCALE;
        width =scale-(scale/4);
        height =scale-(scale/4);
     
    }
   
     @Override
    public void draw(Graphics g) {
       g.setColor(Color.white);
      
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX()+(scale/8) ;
       int ycord = scale * position.getY()+(scale/8);
       
       g.fillRect(xcord, ycord, width, height);
       g.setColor(Color.BLACK);
        g.drawRect(xcord, ycord, width, height);
      
       
    }
    

    
}
