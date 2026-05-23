package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class LaunchCommandTest {

    private RobotWorld world;

    @BeforeEach
    void freshWorld() {
        world = new RobotWorld(11, 11, 5);
    }

    @Test
    void execute_addsRobotToWorld() {
        Command launch = Command.generate(new ServerRequest("HAL", "launch", new String[0]));
        launch.execute(world, null);
        assertNotNull(world.getAllRobots().get("HAL"));
    }

    @Test
    void execute_returnsOkResult() {
        Command launch = Command.generate(new ServerRequest("HAL", "launch", new String[0]));
        ServerResponse res = launch.execute(world, null);
        assertEquals(StatusCode.OK, res.getResult());
    }

    @Test
    void execute_responseStateContainsRobotPosition() {
        Command launch = Command.generate(new ServerRequest("HAL", "launch", new String[0]));
        ServerResponse res = launch.execute(world, null);

        // addRobot picks a safe random spawn — we just assert a position was set, not its exact value.
        assertNotNull(res.getState());
        assertNotNull(res.getState().getPosition());
        int xLimit = (world.getWidth() - 1) / 2;
        int yLimit = (world.getHeight() - 1) / 2;
        int x = res.getState().getPosition().getX();
        int y = res.getState().getPosition().getY();
        assertTrue(Math.abs(x) <= xLimit && Math.abs(y) <= yLimit,
                "spawn position " + x + "," + y + " should be inside the world");
    }

    @Test
    void execute_responseStateDirectionIsNorth() {
        Command launch = Command.generate(new ServerRequest("HAL", "launch", new String[0]));
        ServerResponse res = launch.execute(world, null);

        assertEquals(Directions.NORTH, res.getState().getDirection());
    }

    @Test
    void execute_responseDataIncludesVisibilityAndShields() {
        Command launch = Command.generate(new ServerRequest("HAL", "launch", new String[0]));
        ServerResponse res = launch.execute(world, null);

        assertNotNull(res.getData());
        assertTrue(res.getData().getVisibility() > 0);
        assertTrue(res.getData().getShields() > 0);
    }
}