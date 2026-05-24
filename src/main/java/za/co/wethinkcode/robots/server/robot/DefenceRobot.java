package za.co.wethinkcode.robots.server.robot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class DefenceRobot extends BaseRobot {

    
    
    public DefenceRobot(String name, int x, int y) {
        super(name, x, y, shield, FRate);

    }

    @Override
    public void draw(Graphics g) {
       String label =this.getName();
       String facing = this.getDirection().toString();
       g.setColor(Color.WHITE);
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX() +scale/4;
       int ycord = scale * position.getY()+scale/4;
       g.setFont(new Font("Monospaced", Font.CENTER_BASELINE, 11));
      g.drawString(label, xcord, ycord-30);
      g.drawString(facing, xcord, ycord-20);
       g.fillOval(xcord,ycord,width/2,height/2);
    }


    
}
