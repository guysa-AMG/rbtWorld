package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class BackCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setupWorldWithEmptyMap() {
        world = new RobotWorld(11, 11, 5);
        ArrayList<ArrayList<Impediments>> emptyMap = new ArrayList<>();
        for (int y = 0; y < 11; y++) {
            ArrayList<Impediments> row = new ArrayList<>();
            for (int x = 0; x < 11; x++) row.add(null);
            emptyMap.add(row);
        }
        world.loadMap(emptyMap);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
        robot.updatePosition(new Position(5, 5));
    }

    private Command backCommand(String steps) {
        return Command.generate(new ServerRequest("HAL", "back", new String[]{steps}));
    }

    @Nested
    @DisplayName("response shape")
    class ResponseShape {

        @Test
        void execute_returnsOkResult() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test
        void execute_responseHasState() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertNotNull(res.getState());
        }

        @Test
        void execute_responseHasDataMessage() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertNotNull(res.getData());
            assertNotNull(res.getData().getMessage());
        }
    }

    @Nested
    @DisplayName("invalid arguments")
    class InvalidArguments {

        @Test
        void execute_throwsOnNonNumericSteps() {
            assertThrows(NumberFormatException.class,
                    () -> backCommand("two").execute(world, robot));
        }
    }
}
