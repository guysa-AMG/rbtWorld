package za.co.wethinkcode.robots.server.commands.MovementCommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ForwardCommandTest {

    @Test
    public void testParseSteps() {
        assertEquals(1, ForwardCommand.parseSteps(null));
        assertEquals(1, ForwardCommand.parseSteps(new String[]{}));
        assertEquals(3, ForwardCommand.parseSteps(new String[]{"3"}));
        assertEquals(1, ForwardCommand.parseSteps(new String[]{"x"}));
    }
}
