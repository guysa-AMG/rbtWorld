package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.Position;

public class RobotClientResponseProcessorTest {

    @Test
    public void testFormatLogLineNpcBroadcast() {
        ServerResponse r = ServerResponse.builder().result(StatusCode.OK)
                .data(ServerResponseData.builder().message("[Guyser_Thekiller] I am here").build()).build();
        String s = RobotClientResponseProcessor.formatLogLine(r);
        assertTrue(s.contains("Guyser_Thekiller") && s.startsWith(">>>"));
    }

    @Test
    public void testFormatLogLineNormal() {
        ServerResponse r = ServerResponse.builder().result(StatusCode.OK)
                .data(ServerResponseData.builder().message("Hello").build()).build();
        String s = RobotClientResponseProcessor.formatLogLine(r);
        assertEquals("<< [OK] Hello", s);
    }

    @Test
    public void testShouldFlashAndComputeDistance() {
        ServerResponseState oldState = ServerResponseState.builder().position(new Position(0,0)).direction(null).status(OperationalMode.NORMAL).shields(1).shots(1).build();
        ServerResponseData data = ServerResponseData.builder().distance(3).build();
        assertTrue(RobotClientResponseProcessor.shouldFlashBullet("fire", "me", oldState, data));
        assertEquals(3, RobotClientResponseProcessor.computeFlashDistance(data));
    }

    @Test
    public void testMessagePredicates() {
        assertTrue(RobotClientResponseProcessor.isHitMessage("HIT_BY someone"));
        assertTrue(RobotClientResponseProcessor.isKilledMessage("KILLED_BY someone"));
        assertTrue(RobotClientResponseProcessor.isFellInPit("FELL_IN_PIT"));
    }
}
