package za.co.wethinkcode.robots.server.world;

import java.util.ArrayList;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.services.ITCService;

public final class BattleArenaWorld {

    private BattleArenaWorld() {}

     public static RobotWorld buildFromMap() {

        RobotWorld world = new RobotWorld();
        ArrayList<ArrayList<Impediments>>obj = ITCService.getInstance().parseStringMap("world.properties");
        for (ArrayList<Impediments> rows:obj){
            rows.get(0).getPosition().getX();
        }

        return world;
    }
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
}
