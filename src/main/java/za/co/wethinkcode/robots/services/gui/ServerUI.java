package za.co.wethinkcode.robots.services.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import za.co.wethinkcode.robots.client.gui.WorldPanel;
import za.co.wethinkcode.robots.models.impediment.EmptySpot;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.services.ITCService;

public class ServerUI implements Runnable {

    private JFrame frame;
    private WorldRender world;

    public ServerUI(){
     world = new WorldRender();
    frame= new JFrame("Our World");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
   
    // 2. Add the world to the center position to stretch across all boundaries
    frame.add(world);
    frame.pack();
    frame.setLocationRelativeTo(null);
 
    frame.setVisible(true);
    }
   
    @Override
    public void run() {
        ActionListener listener =new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) { 
                frame.repaint();  }
            
        };
        Timer timer = new Timer(500, listener);
        timer.start();
    }
    
}


class WorldRender extends JPanel{
    int worldHeight;
    int scale;
    int worldWidth;
    WorldRender(){
        worldHeight = ITCService.getInstance().getWorld().getHeight();
        worldWidth = ITCService.getInstance().getWorld().getWidth();
        scale=WorldGenerator.MAP_SCALE;
        setBackground(new Color(20,20,20));
        setPreferredSize(new Dimension(worldWidth*scale,worldHeight*scale));
    }


  
    @Override
    protected void paintComponent(Graphics g) {
       
        super.paintComponent(g);
        paintAllObjects(g);
        drawGrid(g);
    }
   void  paintAllObjects(Graphics g){
       List<Impediments> objs= ITCService.getInstance().getWorld().getMap();
       for(Impediments obj:objs){
        if (!( obj instanceof EmptySpot)){  obj.draw(g);}
       }
    }
     private void drawGrid(Graphics g2) {
        g2.setColor(new Color(40, 40, 50));
        
        for (int gx = 0; gx <= worldWidth; gx++) {
            int px = gx * scale;
            g2.drawLine(px, 0, px, worldHeight * scale);
        }
        for (int gy = 0; gy <= worldHeight; gy++) {
            int py = gy * scale;
            g2.drawLine(0, py, worldWidth * scale, py);
        }

    }
}