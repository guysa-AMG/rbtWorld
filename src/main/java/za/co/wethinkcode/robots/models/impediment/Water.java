package za.co.wethinkcode.robots.models.impediment;
import java.awt.Color;
import java.awt.Graphics;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class Water extends Impediments {

    public Water(Position position) {
        super(position, "WATER"," ");
        int scale = WorldGenerator.MAP_SCALE;
        width = scale;
        height = scale;
    }

     @Override
    public void draw(Graphics g) {
       g.setColor(new Color(54, 134, 214));
      
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX();
       int ycord = scale * position.getY();
       g.fillRect(xcord, ycord, width, height);
       
    }
   

    
}
