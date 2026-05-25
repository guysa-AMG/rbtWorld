package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;

public class LaunchCommandTest {

    @Test
    public void testLaunchBalanced() {
        RobotWorld world = WorldGenerator.build();
        LaunchCommand c = new LaunchCommand(new String[]{"balanced"}, "alice");
        ServerResponse res = c.execute(world, null);
        assertEquals(StatusCode.OK, res.getResult());
        assertTrue(world.getAllRobots().containsKey("alice"));
        assertNotNull(res.getData().getPosition());
    }

    @Test
    public void testLaunchWithNumbers() {
        RobotWorld world = WorldGenerator.build();
        LaunchCommand c = new LaunchCommand(new String[]{"5","2"}, "bob");
        ServerResponse res = c.execute(world, null);
        assertEquals(StatusCode.OK, res.getResult());
        assertTrue(world.getAllRobots().containsKey("bob"));
    }
}
