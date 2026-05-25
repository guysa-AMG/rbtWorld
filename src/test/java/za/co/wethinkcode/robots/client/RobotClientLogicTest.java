package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RobotClientLogicTest {

    @Test
    public void testCommandWhenNotLaunchedReturnsNull() {
        assertNull(RobotClientLogic.normaliseCommand("look", null));
    }

    @Test
    public void testCommandWhenLaunchedPrependsName() {
        assertEquals("HAL look", RobotClientLogic.normaliseCommand("look", "HAL"));
    }

    @Test
    public void testNameCommandWithLockedDifferentNameRewrites() {
        assertEquals("HAL launch", RobotClientLogic.normaliseCommand("Bob launch", "HAL"));
    }

    @Test
    public void testValidFullLinePassedThrough() {
        assertEquals("HAL forward 3", RobotClientLogic.normaliseCommand("HAL forward 3", null));
    }

    @Test
    public void testUnknownCommandRejected() {
        assertNull(RobotClientLogic.normaliseCommand("HAL foo", null));
    }
}
