package za.co.wethinkcode.robots.server.commands.MovementCommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BackCommandTest {

    @Test
    public void testParseSteps() {
        assertEquals(1, BackCommand.parseSteps(null));
        assertEquals(1, BackCommand.parseSteps(new String[]{}));
        assertEquals(4, BackCommand.parseSteps(new String[]{"4"}));
        assertEquals(1, BackCommand.parseSteps(new String[]{"bad"}));
    }
}
