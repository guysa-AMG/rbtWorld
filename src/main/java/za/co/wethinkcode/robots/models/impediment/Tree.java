package za.co.wethinkcode.robots.models.impediment;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType.CanGoThrough;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType.CannotGoThrough;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

@CannotGoThrough
public class Tree extends Impediments {

   public Tree(Position position) {
     super(position, "TREE","cuterbt.gif");
        int scale = WorldGenerator.MAP_SCALE;
        width = scale;
        height = scale;
   }

     @Override
    public void draw(Graphics g) {
       g.setColor(new Color(34,139,34));
      
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX();
       int ycord = scale * position.getY();
       g.fillOval(xcord, ycord, width, height);
       
    }

  
 
}
