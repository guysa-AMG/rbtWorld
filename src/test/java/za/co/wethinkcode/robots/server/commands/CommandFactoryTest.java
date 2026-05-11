package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.ServerRequest;

public class CommandFactoryTest {

    private static ServerRequest req(String robot, String command, String... args) {
        return new ServerRequest(robot, command, args);
    }

    @Nested
    @DisplayName("Command.generate dispatches to the correct subtype")
    class Dispatch {

        @Test
        void generate_launch_returnsLaunchCommand() {
            assertTrue(Command.generate(req("HAL", "launch", "speed", "5", "5")) instanceof LaunchCommand);
        }

        @Test
        void generate_state_returnsStateCommand() {
            assertTrue(Command.generate(req("HAL", "state")) instanceof StateCommand);
        }

        @Test
        void generate_robots_returnsRobotsCommand() {
            assertTrue(Command.generate(req("HAL", "robots")) instanceof RobotsCommand);
        }

        @Test
        void generate_turn_returnsTurnCommand() {
            assertTrue(Command.generate(req("HAL", "turn", "right")) instanceof TurnCommand);
        }

        @Test
        void generate_forward_returnsForwardCommand() {
            assertTrue(Command.generate(req("HAL", "forward", "5")) instanceof ForwardCommand);
        }

        @Test
        void generate_back_returnsBackCommand() {
            assertTrue(Command.generate(req("HAL", "back", "5")) instanceof BackCommand);
        }

        @Test
        void generate_unknownCommand_fallsBackToHelpCommand() {
            assertTrue(Command.generate(req("HAL", "fly")) instanceof HelpCommand);
        }
    }

    @Nested
    @DisplayName("metadata is propagated from request to command")
    class Metadata {

        @Test
        void generate_preservesRobotName() {
            Command cmd = Command.generate(req("R2D2", "forward", "3"));
            assertEquals("R2D2", cmd.getRobotName());
        }

        @Test
        void generate_preservesCommandName() {
            Command cmd = Command.generate(req("HAL", "forward", "3"));
            assertEquals("forward", cmd.getCommandName());
        }

        @Test
        void generate_helpCommand_keepsRobotName() {
            Command cmd = Command.generate(req("Mark", "fly"));
            assertEquals("Mark", cmd.getRobotName());
        }
    }
}