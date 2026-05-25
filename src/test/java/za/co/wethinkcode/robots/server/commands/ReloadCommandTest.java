package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.robot.SimpleRobot;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.commands.ReloadCommand;
import za.co.wethinkcode.robots.models.StatusCode;

public class ReloadCommandTest {

    @Test
    public void testReload() {
        SimpleRobot r = new SimpleRobot("r", 0, 0,0);
        ReloadCommand c = new ReloadCommand("r");
        ServerResponse res = c.execute(null, r);
        assertEquals(StatusCode.OK, res.getResult());
        assertEquals("DONE", res.getData().getMessage());
    }
}
