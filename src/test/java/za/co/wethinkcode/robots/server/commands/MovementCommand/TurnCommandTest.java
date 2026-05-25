package za.co.wethinkcode.robots.server.commands.MovementCommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.robot.SimpleRobot;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;

public class TurnCommandTest {

    @Test
    public void testTurnLeft() {
        SimpleRobot r = new SimpleRobot("r", 0, 0,0);
        TurnCommand c = new TurnCommand(new String[]{"left"}, "r");
        ServerResponse res = c.execute(null, r);
        assertEquals(StatusCode.OK, res.getResult());
        assertEquals("DONE", res.getData().getMessage());
        // turned left from NORTH -> WEST
        assertEquals(za.co.wethinkcode.robots.models.Directions.WEST, r.getDirection());
    }

    @Test
    public void testTurnRight() {
        SimpleRobot r = new SimpleRobot("r", 0, 0,0);
        TurnCommand c = new TurnCommand(new String[]{"right"}, "r");
        ServerResponse res = c.execute(null, r);
        assertEquals(StatusCode.OK, res.getResult());
        assertEquals("DONE", res.getData().getMessage());
        assertEquals(za.co.wethinkcode.robots.models.Directions.EAST, r.getDirection());
    }

    @Test
    public void testInvalidArg() {
        SimpleRobot r = new SimpleRobot("r", 0, 0,0);
        TurnCommand c = new TurnCommand(new String[]{"up"}, "r");
        ServerResponse res = c.execute(null, r);
        assertEquals(StatusCode.ERROR, res.getResult());
        assertTrue(res.getData().getMessage().contains("turn argument must be"));
    }

    @Test
    public void testNoArg() {
        SimpleRobot r = new SimpleRobot("r", 0, 0,0);
        TurnCommand c = new TurnCommand(new String[]{}, "r");
        ServerResponse res = c.execute(null, r);
        assertEquals(StatusCode.ERROR, res.getResult());
        assertTrue(res.getData().getMessage().contains("turn requires an argument"));
    }
}
