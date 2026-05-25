package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;

public class FireCommandTest {

    @Test
    public void testOutOfAmmo() {
        RobotWorld world = WorldGenerator.build();
        world.addRobot("s");
        BaseRobot shooter = world.getAllRobots().get("s");
        // consume all bullets
        while (shooter.decrementBullets()) {}
        FireCommand c = new FireCommand(new String[]{}, "s");
        ServerResponse res = c.execute(world, shooter);
        assertEquals(StatusCode.ERROR, res.getResult());
        assertEquals("OUT_OF_AMMO", res.getData().getMessage());
    }
}
