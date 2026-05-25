package za.co.wethinkcode.robots.server.world;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.Boundary;
import za.co.wethinkcode.robots.models.impediment.EmptySpot;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Mountain;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.models.impediment.Pit;
import za.co.wethinkcode.robots.models.impediment.Rocks;
import za.co.wethinkcode.robots.models.impediment.Tree;
import za.co.wethinkcode.robots.models.impediment.Water;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

public abstract class WorldGenerator implements Iworld  {
    public static int MAP_SCALE=100;
    protected  int width;
    protected  int height;
    protected  int visibility;
    protected  List<Obstacle> obstacles;
    protected List<Impediments> map;
    protected ArrayList<Position> emptySpots;
    protected  Map<String, BaseRobot> robots = new HashMap<>();
    protected  java.util.Set<Position> ammoPickups = ConcurrentHashMap.newKeySet();
    protected List<Command> historyOfCommands;

   private static Logger log = LoggerFactory.getLogger(WorldGenerator.class);

   


       public static RobotWorld build() {
        RobotWorld world = new RobotWorld(51, 31, 7);

        // Outer mountain wall — north (4 segments, 3 passes)
        world.addObstacle(new Obstacle(-25, 15, -17, 14, "MOUNTAIN"));
        world.addObstacle(new Obstacle(-14, 15,  -6, 14, "MOUNTAIN"));
        world.addObstacle(new Obstacle(  6, 15,  14, 14, "MOUNTAIN"));
        world.addObstacle(new Obstacle( 17, 15,  25, 14, "MOUNTAIN"));

        // South wall (mirror)
        world.addObstacle(new Obstacle(-25, -14, -17, -15, "MOUNTAIN"));
        world.addObstacle(new Obstacle(-14, -14,  -6, -15, "MOUNTAIN"));
        world.addObstacle(new Obstacle(  6, -14,  14, -15, "MOUNTAIN"));
        world.addObstacle(new Obstacle( 17, -14,  25, -15, "MOUNTAIN"));

        // West and East walls (small mountain spurs at corners)
        world.addObstacle(new Obstacle(-25, 13, -25,  8, "MOUNTAIN"));
        world.addObstacle(new Obstacle(-25, -8, -25, -13, "MOUNTAIN"));
        world.addObstacle(new Obstacle( 25, 13,  25,  8, "MOUNTAIN"));
        world.addObstacle(new Obstacle( 25, -8,  25, -13, "MOUNTAIN"));

        // Central river — divides the map east/west, three bridge gaps
        world.addObstacle(new Obstacle(-1, 13, 0,  9, "LAKE"));
        world.addObstacle(new Obstacle(-1,  7, 0,  1, "LAKE"));
        world.addObstacle(new Obstacle(-1, -1, 0, -7, "LAKE"));
        world.addObstacle(new Obstacle(-1, -9, 0, -13, "LAKE"));

        // West forest
        int[][] westTrees = {
            {-22,  6}, {-21,  7}, {-20,  6}, {-19,  8}, {-21,  5},
            {-20,  4}, {-22,  3}, {-19,  5}, {-18,  6}, {-23,  8},
            {-22, -3}, {-21, -4}, {-20, -3}, {-19, -5}, {-21, -6},
            {-18, -4}, {-23, -2}, {-17, -5}
        };
        for (int[] t : westTrees) {
            world.addObstacle(new Obstacle(t[0], t[1], t[0], t[1], "TREE"));
        }

        // East forest (mirror)
        int[][] eastTrees = {
            { 22,  6}, { 21,  7}, { 20,  6}, { 19,  8}, { 21,  5},
            { 20,  4}, { 22,  3}, { 19,  5}, { 18,  6}, { 23,  8},
            { 22, -3}, { 21, -4}, { 20, -3}, { 19, -5}, { 21, -6},
            { 18, -4}, { 23, -2}, { 17, -5}
        };
        for (int[] t : eastTrees) {
            world.addObstacle(new Obstacle(t[0], t[1], t[0], t[1], "TREE"));
        }

        // North-west fortress
        world.addObstacle(new Obstacle(-12, 12,  -8, 12, "WALL"));
        world.addObstacle(new Obstacle(-12, 11, -12,  9, "WALL"));
        world.addObstacle(new Obstacle( -8, 11,  -8,  9, "WALL"));
        world.addObstacle(new Obstacle(-12,  9, -10,  9, "WALL"));

        // North-east fortress (mirror)
        world.addObstacle(new Obstacle( 8, 12,  12, 12, "WALL"));
        world.addObstacle(new Obstacle( 8, 11,   8,  9, "WALL"));
        world.addObstacle(new Obstacle(12, 11,  12,  9, "WALL"));
        world.addObstacle(new Obstacle(10,  9,  12,  9, "WALL"));

        // Central ruins — broken walls south of the map
        world.addObstacle(new Obstacle(-7, -10, -5, -10, "WALL"));
        world.addObstacle(new Obstacle(-7, -11, -7, -12, "WALL"));
        world.addObstacle(new Obstacle( 5, -10,  7, -10, "WALL"));
        world.addObstacle(new Obstacle( 7, -11,  7, -12, "WALL"));

        // Rock formations
        int[][] rocks = {
            {-15, -10}, {-14, -10}, {-13, -10}, {-14, -11},
            { 13, -10}, { 14, -10}, { 15, -10}, { 14, -11},
            { -7,  -7}, {  7,  -7}, { -4,  -3}, {  4,  -3},
            {-15,   2}, {-14,   2}, { 14,   2}, { 15,   2}
        };
        for (int[] r : rocks) {
            world.addObstacle(new Obstacle(r[0], r[1], r[0], r[1], "ROCK"));
        }

        // Pit traps near choke points and corners
        int[][] pits = {
            { -3,   0}, { 3,   0},
            { -3,  -8}, { 3,  -8},
            { -3,   8}, { 3,   8},
            {  0,  12}, {  0, -12},
            {-12, -12}, { 12, -12},
            {-15,   8}, { 15,   8}
        };
        for (int[] p : pits) {
            world.addObstacle(new Obstacle(p[0], p[1], p[0], p[1], "PIT"));
        }

        // Ammo pickups — scattered across both sides
        int[][] ammo = {
            { -20,  0}, { 20,  0},
            { -10,  5}, { 10,  5},
            { -10, -5}, { 10, -5},
            {  -5, 10}, {  5, 10},
            {  -5,-10}, {  5,-10}
        };
        for (int[] a : ammo) {
            world.addAmmoPickup(new Position(a[0], a[1]));
        }

        return world;
    }


     public static RobotWorld generateFromMapString(String map){
    RobotWorld world = new RobotWorld();
    int row =0 ;
    List<Impediments> objs=new ArrayList<>();

    for (String data : map.split("\n")) {
        String[] straightLineContents = data.toLowerCase().trim().split("\\s+");
        for (int index=0;index<straightLineContents.length;index++){
                int objX = MAP_SCALE * index;
                int objY = MAP_SCALE * row;
                String chr = straightLineContents[index];
                Impediments obj = mapToImpediments(chr,objX,objY);
                objs.add(obj);
                
        }
        row++;
    }
    world.loadMap(objs);
    return world;
    }
   
    /**
     * generateFromMap
     * this method would generate a customized world by reading the map file and
     * populating a new RobotWorld Instance.
     * Create your file according to
     * this mapping
     *       "T"  ->   Tree
             "M"  ->  Mountain
             "P"  ->  Pit
             "W"  ->  Water
             "|"  ->  Boundary
             "-"  ->  Boundary
             "R"  ->  Rocks
             "."  ->  EmptySpot
     * 
     * @param  mapfile needs to be in the ~/src/main/resources/maps/mapfile 
     * @return RobotWorld
     */
    public static RobotWorld generateFromMapfile(String mapfile){
   
     try( InputStream map =  WorldGenerator.class.getClassLoader().getResourceAsStream("maps/"+mapfile)){
        if (map!=null){
            List<Impediments> objs=new ArrayList<>();
            BufferedReader buffRd = new BufferedReader(new InputStreamReader(map));
            int row =0 ;
            int col=0;
            String data;

            while (( data = buffRd.readLine())!=null) {
                String[] straightLineContents = data.toLowerCase().trim().split("\\s+");
                for (int index=0;index<straightLineContents.length;index++){
                        int objX = index;
                        int objY =  row;
                        String chr = straightLineContents[index];
                        Impediments obj = mapToImpediments(chr,objX,objY);
                        objs.add(obj);
                        
                }

                row++;
                int lineSize= straightLineContents.length;
                if(col<lineSize){ col=lineSize; }
            }
        
         RobotWorld world = new RobotWorld(col, row, WorldGenerator.lookRange); 
         world.loadMap(objs);
         return world;
         
        }
     } catch (IOException e) {
        
        log.error("failed to get resource stream of ["+mapfile+"] "+e.getMessage());
    }
    

      return null;
    }
    /**
     * mapToImpediments
     * creates the correct object of subtype Impediments at the given coordinates (x,y)
     * 
     * @param chr
     * @param x
     * @param y
     * @return Impediments
     */
    private static Impediments mapToImpediments(String chr, int x, int y){
        Position pos = new Position(x, y);
     return switch (chr.toUpperCase()) {
            case "T"  ->  new Tree(pos);
            case "M"  -> new Mountain(pos);
            case "P"  -> new Pit(pos);
            case "W"  -> new Water(pos);
            case "|"  -> new Boundary(pos);
            case "-"  -> new Boundary(pos);
            case "R"  ->new  Rocks(pos);
            case "."  ->new  EmptySpot(pos);

            default->null;
        };
        
    }

    @Override
   public int getHeight(){ return this.height;}
      @Override
   public int getWidth(){ return this.width;}
   

  

}
