package za.co.wethinkcode.robots.models.impediment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ObstacleTest {

    @Nested
    @DisplayName("single-cell obstacle")
    class SingleCell {

        @Test
        void isAt_matchesItsOwnCoordinates() {
            Obstacle obs = new Obstacle(3, 4, 3, 4, "ROCK");
            assertTrue(obs.isAt(3, 4));
        }

        @Test
        void isAt_rejectsAdjacentCellEast() {
            Obstacle obs = new Obstacle(3, 4, 3, 4, "ROCK");
            assertFalse(obs.isAt(4, 4));
        }

        @Test
        void isAt_rejectsAdjacentCellNorth() {
            Obstacle obs = new Obstacle(3, 4, 3, 4, "ROCK");
            assertFalse(obs.isAt(3, 5));
        }

        @Test
        void isAt_rejectsFarCell() {
            Obstacle obs = new Obstacle(0, 0, 0, 0, "ROCK");
            assertFalse(obs.isAt(100, 100));
        }
    }

    @Nested
    @DisplayName("rectangular obstacle")
    class Rectangle {

        @Test
        void isAt_includesAllCornerCells() {
            // x1=2, y1=5 (top), x2=4, y2=3 (bottom)
            Obstacle obs = new Obstacle(2, 5, 4, 3, "MOUNTAIN");
            assertTrue(obs.isAt(2, 5));
            assertTrue(obs.isAt(4, 5));
            assertTrue(obs.isAt(2, 3));
            assertTrue(obs.isAt(4, 3));
        }

        @Test
        void isAt_includesInteriorCell() {
            Obstacle obs = new Obstacle(2, 5, 4, 3, "MOUNTAIN");
            assertTrue(obs.isAt(3, 4));
        }

        @Test
        void isAt_rejectsCellJustOutsideEastEdge() {
            Obstacle obs = new Obstacle(2, 5, 4, 3, "MOUNTAIN");
            assertFalse(obs.isAt(5, 4));
        }

        @Test
        void isAt_rejectsCellJustOutsideNorthEdge() {
            Obstacle obs = new Obstacle(2, 5, 4, 3, "MOUNTAIN");
            assertFalse(obs.isAt(3, 6));
        }

        @Test
        void isAt_includesNegativeCoordinateInterior() {
            Obstacle obs = new Obstacle(-5, 2, -3, 0, "LAKE");
            assertTrue(obs.isAt(-4, 1));
        }
    }

    @Test
    void getType_returnsTypeFromConstructor() {
        Obstacle obs = new Obstacle(0, 0, 0, 0, "PIT");
        assertEquals("PIT", obs.getType());
    }
}
