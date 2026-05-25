package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommandTypeEnumTest {

    @Test
    public void testContains() {
        assertTrue(CommandTypeEnum.contains("launch"));
        assertTrue(CommandTypeEnum.contains("LAUNCH"));
        assertFalse(CommandTypeEnum.contains("notacmd"));
        assertFalse(CommandTypeEnum.contains(null));
    }
}