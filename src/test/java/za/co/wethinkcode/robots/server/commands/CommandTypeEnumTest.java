package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class CommandTypeEnumTest {

    @Test
    void enum_containsAllSupportedCommands() {
        String[] expected = {
            "shutdown", "off", "quit", "launch", "look", "robots",
            "forward", "back", "turn", "repair", "reload", "fire", "state"
        };
        for (String name : expected) {
            CommandTypeEnum value = CommandTypeEnum.valueOf(name);
            assertEquals(name, value.name());
        }
    }

    @Test
    void valueOf_unknown_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> CommandTypeEnum.valueOf("fly"));
    }

    @Test
    void valueOf_isCaseSensitive() {
        // protocol uses lowercase — uppercase should fail
        assertThrows(IllegalArgumentException.class,
                () -> CommandTypeEnum.valueOf("LAUNCH"));
    }

    @Test
    void values_returnsFourteenCommands() {
        assertEquals(14, CommandTypeEnum.values().length);
    }

    @Test
    void values_includesLaunchAndQuit() {
        assertTrue(Arrays.asList(CommandTypeEnum.values()).contains(CommandTypeEnum.launch));
        assertTrue(Arrays.asList(CommandTypeEnum.values()).contains(CommandTypeEnum.quit));
    }
}