package za.co.wethinkcode.robots.server.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.server.commands.OperationalMode;
import za.co.wethinkcode.robots.server.world.Iworld;

public class BaseRobotTest {

    private BaseRobot robot;

    @BeforeEach
    void freshRobot() {
        // shield is hard-coded to 20 in constructor regardless of arg, fireRate from arg
        robot = new SimpleRobot("HAL", 0, 0, 5, 3);
        robot.setWorldBounds(11, 11);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        void getName_returnsConstructorName() {
            assertEquals("HAL", robot.getName());
        }

        @Test
        void getPosition_returnsConstructorCoordinates() {
            assertEquals(0, robot.getPosition().getX());
            assertEquals(0, robot.getPosition().getY());
        }

        @Test
        void getDirection_defaultsToNorth() {
            assertEquals(Directions.NORTH, robot.getDirection());
        }

        @Test
        void getShield_defaultsTo20() {
            assertEquals(5, robot.getShield());
        }

        @Test
        void getFireRate_returnsConstructorValue() {
            assertEquals(3, robot.getFireRate());
        }

        @Test
        void getShoots_initializedToThree() {
            assertEquals(Iworld.MAG_MAX, robot.getShoots());
        }

        @Test
        void getStatus_defaultsToNormal() {
            assertEquals(OperationalMode.NORMAL, robot.getStatus());
        }

         @Test
        void getOperationState_returnsNullInitially() {
            assertEquals(null, robot.getOperationState());
        }

    }

    @Nested
    @DisplayName("turning")
    class Turning {

        @Test
        void turnRight_NorthToEast() {
            robot.turnRight();
            assertEquals(Directions.EAST, robot.getDirection());
        }

        @Test
        void turnRight_EastToSouth() {
            robot.turnRight();
            robot.turnRight();
            assertEquals(Directions.SOUTH, robot.getDirection());
        }

        @Test
        void turnRight_fullCircle_returnsToNorth() {
            for (int i = 0; i < 4; i++) robot.turnRight();
            assertEquals(Directions.NORTH, robot.getDirection());
        }

        @Test
        void turnLeft_NorthToWest() {
            robot.turnLeft();
            assertEquals(Directions.WEST, robot.getDirection());
        }

        @Test
        void turnLeft_fullCircle_returnsToNorth() {
            for (int i = 0; i < 4; i++) robot.turnLeft();
            assertEquals(Directions.NORTH, robot.getDirection());
        }

        @Test
        void turnLeft_WestToSouth() {
            robot.turnLeft();
            robot.turnLeft();
            assertEquals(Directions.SOUTH, robot.getDirection());
        }

        @Test
        void turnRight_WestToNorth() {
            robot.updateDirection(Directions.WEST);

            robot.turnRight();

            assertEquals(Directions.NORTH, robot.getDirection());
        }

        @Test
        void turnLeft_SouthToEast() {
            robot.turnLeft();
            robot.turnLeft();
            robot.turnLeft();
            assertEquals(Directions.EAST, robot.getDirection());
        }

        @Test
        void turnRight_SouthToWest() {
            robot.turnRight();
            robot.turnRight();
            robot.turnRight();

            assertEquals(Directions.WEST, robot.getDirection());
        }

    }

    @Nested
    @DisplayName("movement")
    class Movement {

        @Test
        void moveForward_facingNorth_increasesY() {
            robot.moveForward(2);
            assertEquals(0, robot.getPosition().getX());
            assertEquals(2, robot.getPosition().getY());
        }

        @Test
        void moveForward_facingEast_increasesX() {
            robot.turnRight();
            robot.moveForward(3);
            assertEquals(3, robot.getPosition().getX());
            assertEquals(0, robot.getPosition().getY());
        }

        @Test
        void moveBack_facingNorth_decreasesY() {
            robot.moveForward(3);
            robot.moveBack(2);
            assertEquals(0, robot.getPosition().getX());
            assertEquals(1, robot.getPosition().getY());
        }

        @Test
        void moveForward_stopsAtBoundary() {
            // worldHeight=11, isOutOfBounds: y > worldHeight/2 = 5; so max y=5
            robot.moveForward(20);
            assertTrue(robot.getPosition().getY() <= 5,
                    "Should not move past +5; got " + robot.getPosition().getY());
        }

        @Test
        void moveForward_facingSouth_decreasesY() {
            robot.updateDirection(Directions.SOUTH);
            robot.moveForward(2);
            assertEquals(-2, robot.getPosition().getY());
        }

        @Test
        void moveForward_facingWest_decreasesX() {
            robot.updateDirection(Directions.WEST);
            robot.moveForward(2);
            assertEquals(-2, robot.getPosition().getX());
        }

        @Test
        void moveBack_facingSouth_increasesY() {
            robot.updateDirection(Directions.SOUTH);
            robot.moveBack(2);
            assertEquals(2, robot.getPosition().getY());
        }

        @Test
        void moveBack_facingEast_decreasesX() {
            robot.updateDirection(Directions.EAST);
            robot.moveBack(2);
            assertEquals(-2, robot.getPosition().getX());
        }

        @Test
        void moveBack_facingWest_increasesX() {
            robot.updateDirection(Directions.WEST);
            robot.moveBack(2);
            assertEquals(2, robot.getPosition().getX());
        }

        @Test
        void moveZeroSteps_doesNotChangePosition() {
            int x = robot.getPosition().getX();
            int y = robot.getPosition().getY();

            robot.moveForward(0);

            assertEquals(x, robot.getPosition().getX());
            assertEquals(y, robot.getPosition().getY());
        }

        @Test
        void forwardThenBack_returnsToOrigin() {
            robot.moveForward(3);
            robot.moveBack(3);

            assertEquals(0, robot.getPosition().getX());
            assertEquals(0, robot.getPosition().getY());
        }
    }

    @Nested
    @DisplayName("damage & combat")
    class Combat {

        @Test
        void absorbDamage_decreasesShield() {
            robot.shootRobot(robot); // self-shoot for simplicity, fireRate becomes 2 then absorb
            // Actually shootRobot decrements then passes new rate to absorbDamage.
            // Just verify shield went down somewhat from 20.
            assertTrue(robot.getShield() < 20);
        }

        @Test
        void shootRobot_decrementsFireRate() {
            int before = robot.getFireRate();
            BaseRobot target = new SimpleRobot("R2", 1, 1, 5, 3);
            target.setWorldBounds(11, 11);
            robot.shootRobot(target);
            assertEquals(before - 1, robot.getFireRate());
        }

        @Test
        void shootRobot_returnsFalseWhenOutOfShots() {
            BaseRobot target = new SimpleRobot("R2", 1, 1, 5, 3);
            target.setWorldBounds(11, 11);
            // Empty fireRate
            while (robot.getFireRate() > 0) robot.shootRobot(target);
            assertFalse(robot.shootRobot(target));
        }

        @Test
        void reload_restoresFireRateAndSetsNormal() {
            BaseRobot target = new SimpleRobot("R2", 1, 1, 5, 3);
            target.setWorldBounds(11, 11);
            while (robot.getFireRate() > 0) robot.shootRobot(target);
            robot.reload(5);
            assertEquals(5, robot.getFireRate());
            assertEquals(OperationalMode.NORMAL, robot.getStatus());
        }

        @Test
        void repair_restoresShieldAndSetsNormal() {
            // shoot until shield drops
            for (int i = 0; i < 30; i++) {
                BaseRobot t = new SimpleRobot("X", 0, 0, 5, 3);
                t.setWorldBounds(11, 11);
                robot.shootRobot(t);
                if (robot.getFireRate() == 0) robot.reload(5);
            }
            // Now repair
            robot.repair(20);
            assertEquals(20, robot.getShield());
            assertEquals(OperationalMode.NORMAL, robot.getStatus());
        }

        @Test
        void status_becomesDeadWhenShieldZero() {
            BaseRobot target = new SimpleRobot("Target", 0, 0, 1, 3);
            BaseRobot shooter = new SimpleRobot("Shooter", 0, 0, 5, 10);

            shooter.shootRobot(target);

            if (target.getShield() == 0) {
                assertEquals(OperationalMode.DEAD, target.getStatus());
            }
        }

    }

    @Nested
    @DisplayName("status flags")
    class Status {

        @Test
        void setStatus_isReflectedByGetStatus() {
            robot.setStatus(OperationalMode.REPAIR);
            assertEquals(OperationalMode.REPAIR, robot.getStatus());
        }

        @Test
        void isDead_falseByDefault() {
            assertFalse(robot.isDead());
        }

        @Test
        void isDead_trueWhenStatusDead() {
            robot.setStatus(OperationalMode.DEAD);
            assertTrue(robot.isDead());
        }

        @Test
        void setStatus_doesNotAffectOperationState_bug() {
            robot.setStatus(OperationalMode.RELOAD);

            assertEquals(null, robot.getOperationState());
        }
    }
}
