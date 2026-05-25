package za.co.wethinkcode.robots.models.impediment;

import java.awt.Color;
import java.awt.Graphics;

import lombok.NonNull;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType.CanGoThrough;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType.CannotGoThrough;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

@CannotGoThrough
public class Rocks extends Impediments {

   public Rocks(Position position) {
      super(position, "ROCK","cuterbt.gif");
      //TODO Auto-generated constructor stub
        int scale = WorldGenerator.MAP_SCALE;
        width = scale/2;
        height = scale/2;
   }

   @Override
    public void draw(Graphics g) {
       g.setColor(Color.gray);
      
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX();
       int ycord = scale * position.getY();
       g.fillRoundRect(xcord, ycord, width, height,5,5);
       
    }
  

    
}
