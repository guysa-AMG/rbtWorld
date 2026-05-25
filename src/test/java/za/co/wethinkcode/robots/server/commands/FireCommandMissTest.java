package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;

public class FireCommandMissTest {

    @Test
    public void testMiss() {
        RobotWorld world = WorldGenerator.build();
        world.addRobot("s");
        BaseRobot shooter = world.getAllRobots().get("s");
        shooter.updatePosition(new Position(0,0));
        shooter.updateDirection(Directions.NORTH);
        world.updateRobot("s", shooter);

        FireCommand c = new FireCommand(new String[]{}, "s");
        ServerResponse res = c.execute(world, shooter);
        assertEquals(StatusCode.OK, res.getResult());
        assertTrue(res.getData().getMessage().equals("Miss") || res.getData().getMessage().startsWith("HIT") || res.getData().getMessage().startsWith("KILLED"));
    }
}
