package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.commands.MovementCommand.BackCommand;
import za.co.wethinkcode.robots.server.commands.MovementCommand.ForwardCommand;
import za.co.wethinkcode.robots.server.commands.MovementCommand.TurnCommand;
import za.co.wethinkcode.robots.server.commands.serverCommands.DumpCommand;
import za.co.wethinkcode.robots.server.commands.serverCommands.RobotsCommand;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class CommandsTest {

    private RobotWorld world;

    @BeforeEach
    void freshWorld() {
        world = new RobotWorld(11, 11, 5);
    }

    private static ServerRequest req(String cmd, String robot, String... args) {
        return new ServerRequest(robot, cmd, args);
    }

    @Nested
    @DisplayName("Command.generate factory")
    class Factory {
        @Test void buildsLaunch()    { assertEquals("launch",  Command.generate(req("launch","HAL","balanced")).getCommandName()); }
        @Test void buildsState()     { assertEquals("state",   Command.generate(req("state","HAL")).getCommandName()); }
        @Test void buildsRobots()    { assertEquals("robots",  Command.generate(req("robots","HAL")).getCommandName()); }
        @Test void buildsTurn()      { assertEquals("turn",    Command.generate(req("turn","HAL","left")).getCommandName()); }
        @Test void buildsLook()      { assertEquals("look",    Command.generate(req("look","HAL")).getCommandName()); }
        @Test void buildsDump()      { assertEquals("dump",    Command.generate(req("dump","HAL")).getCommandName()); }
        @Test void buildsReload()    { assertEquals("reload",  Command.generate(req("reload","HAL")).getCommandName()); }
        @Test void buildsRepair()    { assertEquals("repair",  Command.generate(req("repair","HAL")).getCommandName()); }
        @Test void buildsFire()      { assertEquals("fire",    Command.generate(req("fire","HAL")).getCommandName()); }
        @Test void buildsForward()   { assertEquals("forward", Command.generate(req("forward","HAL","1")).getCommandName()); }
        @Test void buildsBack()      { assertEquals("back",    Command.generate(req("back","HAL","1")).getCommandName()); }
        @Test void buildsHelp()      { assertEquals("help",    Command.generate(req("help","HAL")).getCommandName()); }
        @Test void unknownFallsBackToHelp() {
            assertEquals("help", Command.generate(req("wibble","HAL")).getCommandName());
        }
    }

    @Nested
    @DisplayName("Command base getters/setters")
    class Base {
        @Test void setAndGetRobotName() {
            Command c = Command.generate(req("look","HAL"));
            c.setRobotName("NEW");
            assertEquals("NEW", c.getRobotName());
        }

        @Test void setAndGetAttribute() {
            Command c = Command.generate(req("look","HAL"));
            c.setAttribute("meta");
            assertEquals("meta", c.getAttribute());
        }

        @Test void getArgumentReturnsConstructorArgs() {
            Command c = Command.generate(req("forward","HAL","2"));
            assertEquals("2", c.getArgument()[0]);
        }

        @Test void restrictedDefaultsToTrue() {
            Command c = Command.generate(req("dump","HAL"));
            assertTrue(c.restricted());
        }

        @Test void setAsServerCommandLiftsRestriction() {
            Command c = Command.generate(req("dump","HAL"));
            c.setAsServerCommand();
            assertEquals(false, c.restricted());
        }

        @Test void restrictedServerResponseReturnsError() {
            Command c = Command.generate(req("dump","HAL"));
            ServerResponse res = c.restrictedServerResponse();
            assertEquals(StatusCode.ERROR, res.getResult());
            assertTrue(res.getData().getMessage().contains("reserved"));
        }
    }

    @Nested
    @DisplayName("LaunchCommand variants")
    class Launch {
        @Test void launchBalancedSpawnsRobot() {
            Command c = Command.generate(req("launch","HAL","balanced"));
            ServerResponse res = world.perform(c);
            assertEquals(StatusCode.OK, res.getResult());
            assertNotNull(world.getAllRobots().get("HAL"));
        }

        @Test void launchOffensiveSpawnsRobot() {
            world.perform(Command.generate(req("launch","HAL","offensive")));
            assertNotNull(world.getAllRobots().get("HAL"));
        }

        @Test void launchDefensiveSpawnsRobot() {
            world.perform(Command.generate(req("launch","HAL","defensive")));
            assertNotNull(world.getAllRobots().get("HAL"));
        }

        @Test void launchExplicitShieldShots() {
            world.perform(Command.generate(req("launch","HAL","4","2")));
            assertNotNull(world.getAllRobots().get("HAL"));
        }

        @Test void launchWithNoArgsReturnsError() {
            ServerResponse res = world.perform(Command.generate(req("launch","HAL")));
            assertEquals(StatusCode.ERROR, res.getResult());
            assertNotNull(res.getData().getMessage());
        }

        @Test void launchUnknownKindStillSpawns() {
            world.perform(Command.generate(req("launch","HAL","weird")));
            assertNotNull(world.getAllRobots().get("HAL"));
        }
    }

    @Nested
    @DisplayName("StateCommand")
    class State {
        @Test void stateReturnsResponseWithState() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("state","HAL")));
            assertNotNull(res.getState());
            assertNotNull(res.getState().getPosition());
        }
    }

    @Nested
    @DisplayName("HelpCommand")
    class Help {
        @Test void helpReturnsOkResponse() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("help","HAL")));
            assertEquals(StatusCode.OK, res.getResult());
            assertNotNull(res.getData());
        }
    }

    @Nested
    @DisplayName("QuitCommand")
    class Quit {
        @Test void quitRemovesRobot() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            // QuitCommand isn't routed through Command.generate; build directly
            QuitCommand quit = new QuitCommand("HAL");
            BaseRobot bot = world.getAllRobots().get("HAL");
            quit.execute(world, bot);
            assertNull(world.getAllRobots().get("HAL"));
        }

        @Test void quitConstructorWithName() {
            QuitCommand q = new QuitCommand("HAL");
            assertEquals("quit", q.getCommandName());
        }
    }

    @Nested
    @DisplayName("ErrorCommand")
    class Error {
        @Test void errorCommandReportsErrorStatus() {
            ErrorCommand ec = new ErrorCommand("boom", "HAL");
            ServerResponse res = ec.execute(world, null);
            assertEquals(StatusCode.ERROR, res.getResult());
            assertEquals("boom", res.getData().getMessage());
        }
    }

    @Nested
    @DisplayName("TurnCommand")
    class Turn {
        @Test void turnLeftSucceeds() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("turn","HAL","left")));
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test void turnRightSucceeds() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("turn","HAL","right")));
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test void turnUnknownDirectionIsError() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("turn","HAL","sideways")));
            assertEquals(StatusCode.ERROR, res.getResult());
        }

        @Test void turnWithNoArgsIsError() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            BaseRobot bot = world.getAllRobots().get("HAL");
            TurnCommand t = new TurnCommand(new String[]{}, "HAL");
            ServerResponse res = t.execute(world, bot);
            assertEquals(StatusCode.ERROR, res.getResult());
        }
    }

    @Nested
    @DisplayName("ForwardCommand / BackCommand parseSteps + execute")
    class Movement {
        @Test void forwardExecuteReturnsOkResponse() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("forward","HAL","1")));
            assertEquals(StatusCode.OK, res.getResult());
            assertNotNull(res.getState());
        }

        @Test void backExecuteReturnsOkResponse() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("back","HAL","1")));
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test void forwardWithNoArgsDefaultsToOne() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ForwardCommand f = new ForwardCommand(new String[]{}, "HAL");
            assertEquals(StatusCode.OK, f.execute(world, world.getAllRobots().get("HAL")).getResult());
        }

        @Test void backWithNoArgsDefaultsToOne() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            BackCommand b = new BackCommand(new String[]{}, "HAL");
            assertEquals(StatusCode.OK, b.execute(world, world.getAllRobots().get("HAL")).getResult());
        }

        @Test void forwardWithNonNumericArgsDefaultsToOne() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ForwardCommand f = new ForwardCommand(new String[]{"abc"}, "HAL");
            assertEquals(StatusCode.OK, f.execute(world, world.getAllRobots().get("HAL")).getResult());
        }
    }

    @Nested
    @DisplayName("Server-side commands (restricted by default)")
    class ServerOnly {
        @Test void dumpReturnsRestrictedErrorByDefault() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("dump","HAL")));
            assertEquals(StatusCode.ERROR, res.getResult());
        }

        @Test void dumpUnrestrictedReturnsDumpText() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            world.addObstacle(new Obstacle(1, 1, 1, 1, "MOUNTAIN"));
            DumpCommand d = new DumpCommand("HAL");
            d.setAsServerCommand();
            ServerResponse res = d.execute(world, world.getAllRobots().get("HAL"));
            assertEquals(StatusCode.OK, res.getResult());
            assertTrue(res.getData().getMessage().contains("Robot Dump"));
            assertTrue(res.getData().getMessage().contains("World Dump"));
        }

        @Test void robotsCommandRestrictedByDefault() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            ServerResponse res = world.perform(Command.generate(req("robots","HAL")));
            assertEquals(StatusCode.ERROR, res.getResult());
        }

        @Test void robotsCommandUnrestrictedListsRobots() {
            world.perform(Command.generate(req("launch","HAL","balanced")));
            RobotsCommand r = new RobotsCommand("HAL");
            r.setAsServerCommand();
            ServerResponse res = r.execute(world, world.getAllRobots().get("HAL"));
            assertEquals(StatusCode.OK, res.getResult());
            assertTrue(res.getData().getMessage().contains("HAL"));
        }
    }

    @Nested
    @DisplayName("Performing a command for an unlaunched robot")
    class Unlaunched {
        @Test void unknownRobotGetsErrorBack() {
            ServerResponse res = world.perform(Command.generate(req("look","Ghost")));
            assertEquals(StatusCode.ERROR, res.getResult());
            assertTrue(res.getData().getMessage().toLowerCase().contains("not been launched"));
        }
    }

    @Nested
    @DisplayName("Forward/Back moveRobot public API")
    class MoveRobotApi {

        private void launchAtOrigin() {
            world.perform(Command.generate(req("launch", "HAL", "balanced")));
            BaseRobot bot = world.getAllRobots().get("HAL");
            bot.updatePosition(new za.co.wethinkcode.robots.models.Position(0, 0));
            bot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test void forwardMoveRobot_facingNorth_movesUp() {
            launchAtOrigin();
            new ForwardCommand(new String[]{"1"}, "HAL").moveRobot("HAL", 1, world);
            assertEquals(1, world.getAllRobots().get("HAL").getPosition().getY());
        }

        @Test void forwardMoveRobot_missingRobotReturnsFalse() {
            assertEquals(false, new ForwardCommand(new String[]{"1"}, "Ghost").moveRobot("Ghost", 1, world));
        }

        @Test void forwardMoveRobot_zeroStepsReturnsTrue() {
            launchAtOrigin();
            assertTrue(new ForwardCommand(new String[]{"0"}, "HAL").moveRobot("HAL", 0, world));
        }

        @Test void backMoveRobot_facingNorth_movesDown() {
            launchAtOrigin();
            new za.co.wethinkcode.robots.server.commands.MovementCommand.BackCommand(
                    new String[]{"1"}, "HAL").moveRobot("HAL", 1, world);
            assertEquals(-1, world.getAllRobots().get("HAL").getPosition().getY());
        }

        @Test void backMoveRobot_missingRobotReturnsFalse() {
            assertEquals(false, new za.co.wethinkcode.robots.server.commands.MovementCommand.BackCommand(
                    new String[]{"1"}, "Ghost").moveRobot("Ghost", 1, world));
        }

        @Test void backMoveRobot_zeroStepsReturnsTrue() {
            launchAtOrigin();
            assertTrue(new za.co.wethinkcode.robots.server.commands.MovementCommand.BackCommand(
                    new String[]{"0"}, "HAL").moveRobot("HAL", 0, world));
        }
    }
}
