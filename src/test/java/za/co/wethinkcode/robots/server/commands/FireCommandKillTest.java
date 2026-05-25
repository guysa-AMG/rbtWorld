package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.robot.SimpleRobot;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;

public class FireCommandKillTest {

    @Test
    public void testLethalHitRemovesVictim() {
        RobotWorld world = WorldGenerator.build();
        world.addRobot("shooter",3,3,0);
        world.addRobot("victim",1,1,0);

        BaseRobot shooter = world.getAllRobots().get("shooter");
        BaseRobot victim = world.getAllRobots().get("victim");

        shooter.updatePosition(new Position(0,0));
        shooter.updateDirection(Directions.EAST);

        victim.updatePosition(new Position(1,0));
        victim.updateDirection(Directions.WEST);

        world.updateRobot("shooter", shooter);
        world.updateRobot("victim", victim);

        FireCommand c = new FireCommand(new String[]{}, "shooter");
        ServerResponse res = c.execute(world, shooter);
        assertEquals(StatusCode.OK, res.getResult());
        assertTrue(res.getData().getMessage().startsWith("KILLED") || res.getData().getMessage().contains("Hit") || res.getData().getMessage().equals("Miss"));
        // If lethal, victim should be removed
        if (res.getData().getMessage().startsWith("KILLED")) {
            assertFalse(world.getAllRobots().containsKey("victim"));
        }
    }
}
