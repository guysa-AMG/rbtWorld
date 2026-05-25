package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.commands.MovementCommand.ForwardCommand;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.Directions;

public class ForwardCommandMoveTest {

    @Test
    public void testMoveMethod() {
        RobotWorld world = new RobotWorld(5,5,3);
        BaseRobot r = BaseRobot.Builder("bob", 0,0,3,3,0);
        world.getAllRobots().put("bob", r);
        // ensure map has robot at 0,0
        world.loadMap(java.util.Arrays.asList(r, new za.co.wethinkcode.robots.models.impediment.EmptySpot(new Position(1,0))));

        r.updateDirection(Directions.EAST);
        boolean moved = new ForwardCommand(new String[]{"1"}, "bob").move(r, 1, world);
        assertTrue(moved);
        assertNotEquals(new Position(0,0), r.getPosition());
    }
}
