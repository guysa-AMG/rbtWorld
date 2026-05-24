package za.co.wethinkcode.robots.models.impediment;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class Tree extends Impediments {

   public Tree(Position position) {
     super(position, "TREE","cuterbt.gif");
        int scale = WorldGenerator.MAP_SCALE;
        width = scale;
        height = scale;
   }

     @Override
    public void draw(Graphics g) {
       g.setColor(Color.green);
      
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX();
       int ycord = scale * position.getY();
       g.drawOval(xcord, ycord, width, height);
       
    }

  
 
}
