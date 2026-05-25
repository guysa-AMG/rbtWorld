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
    private static final int CELL = 16;
    int worldHeight;
    int worldWidth;
    WorldRender(){
        worldHeight = ITCService.getInstance().getWorld().getHeight();
        worldWidth = ITCService.getInstance().getWorld().getWidth();
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(worldWidth * CELL + 2, worldHeight * CELL + 2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintAllObjects(g);
        drawGrid(g);
    }

    void paintAllObjects(Graphics g){
        List<Impediments> objs = ITCService.getInstance().getWorld().getMap();
        for (Impediments obj : objs){
            if (obj instanceof EmptySpot) continue;
            int px = obj.getPosition().getX() * CELL;
            int py = obj.getPosition().getY() * CELL;
            g.setColor(colorFor(obj.type));
            g.fillRect(px + 1, py + 1, CELL - 2, CELL - 2);
        }
    }

    private void drawGrid(Graphics g2) {
        g2.setColor(new Color(40, 40, 50));
        for (int gx = 0; gx <= worldWidth; gx++) {
            int px = gx * CELL;
            g2.drawLine(px, 0, px, worldHeight * CELL);
        }
        for (int gy = 0; gy <= worldHeight; gy++) {
            int py = gy * CELL;
            g2.drawLine(0, py, worldWidth * CELL, py);
        }
    }

    // Same palette as the client GUI so server and client show identical-looking maps.
    private static Color colorFor(String type) {
        if (type == null) return Color.MAGENTA;
        return switch (type.toUpperCase()) {
            case "MOUNTAIN"      -> new Color(150, 100, 60);
            case "WATER", "LAKE" -> new Color(60, 130, 220);
            case "TREE"          -> new Color(40, 160, 70);
            case "WALL"          -> new Color(200, 200, 200);
            case "ROCK"          -> new Color(130, 130, 130);
            case "PIT", "HOLE"   -> new Color(220, 40, 40);
            case "BOUNDARY"      -> new Color(80, 80, 80);
            default              -> Color.MAGENTA;
        };
    }
}