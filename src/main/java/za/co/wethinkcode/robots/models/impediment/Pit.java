package za.co.wethinkcode.robots.models.impediment;
import java.awt.Color;
import java.awt.Graphics;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class Pit extends Impediments {

    public Pit( Position position) {
        super(position, "HOLE");
    }

     @Override
    public void draw(Graphics g) {
       g.setColor(new Color(150, 150, 150));
      
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX();
       int ycord = scale * position.getY();
       g.fillOval(xcord, ycord, scale, scale/2);
       g.setColor(Color.BLACK);
       g.drawRect(xcord, ycord+(scale/2), scale, scale/2);
       
    }



    
}
