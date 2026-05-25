package za.co.wethinkcode.robots.server.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class RobotKindsTest {

    private static java.awt.Graphics graphics() {
        return new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB).getGraphics();
    }

    @Nested
    @DisplayName("Builder factory chooses subtype from shield/shoots ratio")
    class Builder {
        @Test void equalShieldAndShootsYieldsSimple() {
            BaseRobot r = BaseRobot.Builder("HAL", 0, 0, 5, 5, 1);
            assertTrue(r instanceof SimpleRobot);
        }

        @Test void higherShieldYieldsDefensive() {
            BaseRobot r = BaseRobot.Builder("HAL", 0, 0, 9, 3, 2);
            assertTrue(r instanceof DefensiveRobot);
        }

        @Test void higherShootsYieldsOffensive() {
            BaseRobot r = BaseRobot.Builder("HAL", 0, 0, 3, 9, 3);
            assertTrue(r instanceof OffensiveRobot);
        }
    }

    @Nested
    @DisplayName("Subclasses each draw without exception")
    class Drawing {
        @Test void simpleDraws()    { new SimpleRobot("S", 0, 0, 1).draw(graphics()); }
        @Test void offensiveDraws() { new OffensiveRobot("O", 0, 0, 2).draw(graphics()); }
        @Test void defensiveDraws() { new DefensiveRobot("D", 0, 0, 3).draw(graphics()); }
    }

    @Nested
    @DisplayName("Subclasses inherit shield/shots correctly")
    class Stats {
        @Test void offensiveHasMoreShotsThanShield() {
            OffensiveRobot r = new OffensiveRobot("O", 0, 0, 1);
            assertTrue(r.getShoots() > r.getShields());
        }

        @Test void defensiveHasMoreShieldThanShots() {
            DefensiveRobot r = new DefensiveRobot("D", 0, 0, 2);
            assertTrue(r.getShields() > r.getShoots());
        }

        @Test void simpleHasEqualShieldShots() {
            SimpleRobot r = new SimpleRobot("S", 0, 0, 3);
            assertEquals(r.getShields(), r.getShoots());
        }
    }

    @Nested
    @DisplayName("Names and positions persist through constructor")
    class Identity {
        @Test void offensiveStoresNameAndPos() {
            OffensiveRobot r = new OffensiveRobot("HAL", 3, 4, 1);
            assertEquals("HAL", r.getName());
            assertNotNull(r.getPosition());
            assertEquals(3, r.getPosition().getX());
            assertEquals(4, r.getPosition().getY());
        }

        @Test void defensiveStoresNameAndPos() {
            DefensiveRobot r = new DefensiveRobot("R2", -1, -2, 2);
            assertEquals("R2", r.getName());
            assertEquals(-1, r.getPosition().getX());
            assertEquals(-2, r.getPosition().getY());
        }

        @Test void differentInstancesAreDistinct() {
            assertNotEquals(new SimpleRobot("A", 0, 0, 1).getName(),
                            new SimpleRobot("B", 0, 0, 2).getName());
        }
    }
}
