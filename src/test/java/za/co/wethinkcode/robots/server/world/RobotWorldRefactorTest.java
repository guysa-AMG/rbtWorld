package za.co.wethinkcode.robots.server.world;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RobotWorldRefactorTest {

    @Test
    public void testGetRobotStateHandlesMissingRobot() {
        RobotWorld w = new RobotWorld();
        String s = w.getRobotState("noone");
        assertEquals("Robot not found", s);
    }

    @Test
    public void testGetObjectsAtPositionNull() {
        RobotWorld w = new RobotWorld();
        assertNull(w.getObjectsAtPosition(null));
    }
}
