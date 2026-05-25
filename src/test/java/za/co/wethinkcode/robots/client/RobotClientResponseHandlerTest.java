package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RobotClientResponseHandlerTest {

    @Test
    public void testDeathMessages() {
        assertTrue(RobotClientResponseHandler.isDeathMessage("KILLED_BY alice"));
        assertTrue(RobotClientResponseHandler.isDeathMessage("FELL_IN_PIT"));
        assertTrue(RobotClientResponseHandler.isDeathMessage("robot x has not been launched"));
        assertFalse(RobotClientResponseHandler.isDeathMessage("random"));
    }

    @Test
    public void testHitMessage() {
        assertTrue(RobotClientResponseHandler.isHitMessage("HIT_BY bob"));
        assertFalse(RobotClientResponseHandler.isHitMessage("HIT"));
    }

    @Test
    public void testLookToggle() {
        assertTrue(RobotClientResponseHandler.shouldExpandLook("look"));
        assertFalse(RobotClientResponseHandler.shouldExpandLook("forward"));
    }

    @Test
    public void testIsFireResult() {
        assertTrue(RobotClientResponseHandler.isFireResult("fire"));
        assertFalse(RobotClientResponseHandler.isFireResult("look"));
    }
}
