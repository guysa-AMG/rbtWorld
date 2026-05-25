package za.co.wethinkcode.robots.models.impediment;

import java.awt.Color;
import java.awt.Graphics;

import lombok.NonNull;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class Mountain extends Impediments {

    public Mountain( Position position) {
        super(position, "MOUNTAIN");
        int scale = WorldGenerator.MAP_SCALE;
        width = scale;
        height = scale;
       
    }

    @Override
    public void draw(Graphics g) {
       g.setColor(new Color(60, 37, 21));
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX();
       int ycord = scale * position.getY();
       int[] xpoints = {xcord,xcord+(width/2),xcord+width };
       int[] ypoints = {ycord,ycord-(width/2),ycord};
       g.fillPolygon(xpoints,ypoints , 3);

         // Optional: Draw a snow cap
        int[] snowX = {250, 300, 350};
        int[] snowY = {175, 100, 175};
     
     
    }



  



    
}
