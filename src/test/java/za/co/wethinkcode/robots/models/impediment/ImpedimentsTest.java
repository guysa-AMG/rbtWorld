package za.co.wethinkcode.robots.models.impediment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType.CanGoThrough;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType.CannotGoThrough;

public class ImpedimentsTest {

    private static java.awt.Graphics graphics() {
        return new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).getGraphics();
    }

    @Nested
    @DisplayName("Type strings and position storage")
    class TypesAndPositions {

        @Test void boundaryReportsWall() {
            assertEquals("WALL", new Boundary(new Position(1, 1)).getType());
        }

        @Test void mountainReportsMountain() {
            assertEquals("MOUNTAIN", new Mountain(new Position(2, 2)).getType());
        }

        @Test void pitReportsHole() {
            assertEquals("HOLE", new Pit(new Position(3, 3)).getType());
        }

        @Test void waterReportsWater() {
            assertEquals("WATER", new Water(new Position(4, 4)).getType());
        }

        @Test void rocksReportsRock() {
            assertEquals("ROCK", new Rocks(new Position(5, 5)).getType());
        }

        @Test void treeReportsTree() {
            assertEquals("TREE", new Tree(new Position(6, 6)).getType());
        }

        @Test void emptySpotReportsEmptySpot() {
            assertEquals("EMPTYSPOT", new EmptySpot(new Position(0, 0)).getType());
        }

        @Test void allStorePositions() {
            Position p = new Position(7, 7);
            assertEquals(p, new Boundary(p).getPosition());
            assertEquals(p, new Mountain(p).getPosition());
            assertEquals(p, new Pit(p).getPosition());
            assertEquals(p, new Water(p).getPosition());
            assertEquals(p, new Rocks(p).getPosition());
            assertEquals(p, new Tree(p).getPosition());
            assertEquals(p, new EmptySpot(p).getPosition());
        }
    }

    @Nested
    @DisplayName("CanGoThrough / CannotGoThrough annotation markers")
    class Annotations {
        @Test void emptySpotIsPassable() {
            assertTrue(EmptySpot.class.isAnnotationPresent(CanGoThrough.class));
        }
        @Test void pitIsPassable() {
            assertTrue(Pit.class.isAnnotationPresent(CanGoThrough.class));
        }
        @Test void waterIsPassable() {
            assertTrue(Water.class.isAnnotationPresent(CanGoThrough.class));
        }
        @Test void boundaryBlocks() {
            assertTrue(Boundary.class.isAnnotationPresent(CannotGoThrough.class));
            assertFalse(Boundary.class.isAnnotationPresent(CanGoThrough.class));
        }
        @Test void treeBlocks() {
            assertTrue(Tree.class.isAnnotationPresent(CannotGoThrough.class));
        }
        @Test void rocksBlocks() {
            assertTrue(Rocks.class.isAnnotationPresent(CannotGoThrough.class));
        }
    }

    @Nested
    @DisplayName("draw() implementations are wired up")
    class Drawing {
        @Test void boundaryDraws()  { new Boundary(new Position(0, 0)).draw(graphics()); }
        @Test void mountainDraws()  { new Mountain(new Position(0, 0)).draw(graphics()); }
        @Test void pitDraws()       { new Pit(new Position(0, 0)).draw(graphics()); }
        @Test void waterDraws()     { new Water(new Position(0, 0)).draw(graphics()); }
        @Test void rocksDraws()     { new Rocks(new Position(0, 0)).draw(graphics()); }
        @Test void treeDraws()      { new Tree(new Position(0, 0)).draw(graphics()); }
        @Test void emptyDraws()     { new EmptySpot(new Position(0, 0)).draw(graphics()); }
    }

    @Nested
    @DisplayName("Obstacle setPosition")
    class ObstacleSetPosition {
        @Test void setPositionUpdatesCoords() {
            Obstacle o = new Obstacle(0, 0, 0, 0, "WALL");
            o.setPosition(new Position(5, 9));
            assertEquals(5, o.getPosition().getX());
            assertEquals(9, o.getPosition().getY());
        }

        @Test void getPosReturnsPosition() {
            Obstacle o = new Obstacle(3, 4, 3, 4, "TREE");
            assertNotNull(o.getPos());
            assertEquals(3, o.getPos().getX());
            assertEquals(4, o.getPos().getY());
        }
    }
}
