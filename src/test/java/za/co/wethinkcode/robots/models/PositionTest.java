package za.co.wethinkcode.robots.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PositionTest {

    @Nested
    @DisplayName("construction & accessors")
    class Construction {

        @Test
        void allArgsConstructor_setsXAndY() {
            Position p = new Position(3, 7);
            assertEquals(3, p.getX());
            assertEquals(7, p.getY());
        }

        @Test
        void noArgConstructor_leavesXAndYAtZero() {
            Position p = new Position();
            assertEquals(0, p.getX());
            assertEquals(0, p.getY());
        }

        @Test
        void setX_updatesXOnly() {
            Position p = new Position(0, 5);
            p.setX(9);
            assertEquals(9, p.getX());
            assertEquals(5, p.getY());
        }

        @Test
        void setY_updatesYOnly() {
            Position p = new Position(2, 0);
            p.setY(11);
            assertEquals(2, p.getX());
            assertEquals(11, p.getY());
        }

        @Test
        void allArgsConstructor_acceptsNegativeCoordinates() {
            Position p = new Position(-4, -9);
            assertEquals(-4, p.getX());
            assertEquals(-9, p.getY());
        }
    }

    @Nested
    @DisplayName("increment & decrement")
    class IncrementDecrement {

        @Test
        void incrementX_addsOneToX() {
            Position p = new Position(2, 5);
            p.incrementX();
            assertEquals(3, p.getX());
            assertEquals(5, p.getY());
        }

        @Test
        void incrementY_addsOneToY() {
            Position p = new Position(2, 5);
            p.incrementY();
            assertEquals(2, p.getX());
            assertEquals(6, p.getY());
        }

        @Test
        void decrementX_subtractsOneFromX() {
            Position p = new Position(2, 5);
            p.decrementX();
            assertEquals(1, p.getX());
            assertEquals(5, p.getY());
        }

        @Test
        void decrementY_subtractsOneFromY() {
            Position p = new Position(2, 5);
            p.decrementY();
            assertEquals(2, p.getX());
            assertEquals(4, p.getY());
        }

        @Test
        void incrementThenDecrement_returnsToOriginal() {
            Position p = new Position(0, 0);
            p.incrementX();
            p.incrementY();
            p.decrementX();
            p.decrementY();
            assertEquals(0, p.getX());
            assertEquals(0, p.getY());
        }
    }

    @Nested
    @DisplayName("copy & equality")
    class CopyAndEquality {

        @Test
        void copy_returnsInstanceWithSameValues() {
            Position original = new Position(4, 8);
            Position duplicate = original.copy();
            assertEquals(4, duplicate.getX());
            assertEquals(8, duplicate.getY());
        }

        @Test
        void copy_returnsIndependentInstance() {
            Position original = new Position(4, 8);
            Position duplicate = original.copy();
            duplicate.incrementX();
            assertEquals(4, original.getX(), "original X must not change when copy is mutated");
            assertEquals(5, duplicate.getX());
        }

        @Test
        void copy_isNotTheSameReference() {
            Position original = new Position(1, 2);
            assertNotSame(original, original.copy());
        }

        @Test
        void equalsPosition_trueForSameCoordinates() {
            Position a = new Position(7, 3);
            Position b = new Position(7, 3);
            assertTrue(a.equals(b));
        }

        @Test
        void equalsPosition_falseForDifferentX() {
            Position a = new Position(7, 3);
            Position b = new Position(8, 3);
            assertFalse(a.equals(b));
        }

        @Test
        void equalsPosition_falseForDifferentY() {
            Position a = new Position(7, 3);
            Position b = new Position(7, 4);
            assertFalse(a.equals(b));
        }
    }

    @Nested
    @DisplayName("isIn & distance helpers")
    class DistanceAndBounds {

        @Test
        void isIn_trueWhenInsideBox() {
            assertTrue(new Position(2, 3).isIn(0, 0, 5, 5));
        }

        @Test
        void isIn_falseWhenOutsideBox() {
            assertFalse(new Position(7, 3).isIn(0, 0, 5, 5));
        }

        @Test
        void getDistance_returnsAbsoluteDelta() {
            Position d = new Position(0, 0).getDistance(new Position(-3, 4));
            assertEquals(3, d.getX());
            assertEquals(4, d.getY());
        }

        @Test
        void getStraightDistance_sameRow() {
            assertEquals(3, new Position(0, 5).getStraightDistance(new Position(3, 5)));
        }

        @Test
        void getStraightDistance_sameColumn() {
            assertEquals(7, new Position(2, 0).getStraightDistance(new Position(2, 7)));
        }

        @Test
        void getStraightDistance_diagonalReturnsMinusOne() {
            assertEquals(-1, new Position(0, 0).getStraightDistance(new Position(2, 2)));
        }
    }
}
