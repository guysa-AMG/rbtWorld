package za.co.wethinkcode.robots.server.robot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import za.co.wethinkcode.robots.server.world.WorldGenerator;

public class DefensiveRobot extends BaseRobot {

    
    
    public DefensiveRobot(String name, int x, int y,int id) {
        super(name, x, y, BaseRobot.MAX_VALUE*3, BaseRobot.MAX_VALUE, id);

    }

    @Override
    public void draw(Graphics g) {
       String label =this.getName();
       String facing = this.getDirection().toString();
       String values = this.getShields()+" / "+this.getShoots();
       g.setColor(Color.BLUE);
       int scale = WorldGenerator.MAP_SCALE;
       int xcord = scale * position.getX() +scale/4;
       int ycord = scale * position.getY()+scale/4;
       g.fillOval(xcord,ycord,width/2,height/2);
       g.setColor(Color.WHITE);
       g.setFont(new Font("Monospaced", Font.CENTER_BASELINE, 12));
       g.drawString(label, xcord, ycord-30);
       g.drawString(facing, xcord, ycord-20);
       g.drawString(values, xcord, ycord-10);

    }


    
}
