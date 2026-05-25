package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;

public class CommandGenerateTest {

    @Test
    public void testKnownCommands() {
        ServerRequest r = new ServerRequest("bob", "forward", new String[]{"1"});
        Command c = Command.generate(r);
        assertEquals("ForwardCommand", c.getClass().getSimpleName());

        r = new ServerRequest("bob", "turn", new String[]{"left"});
        c = Command.generate(r);
        assertEquals("TurnCommand", c.getClass().getSimpleName());

        r = new ServerRequest("bob", "launch");
        c = Command.generate(r);
        assertEquals("LaunchCommand", c.getClass().getSimpleName());
    }

    @Test
    public void testDefaultHelp() {
        ServerRequest r = new ServerRequest("bob", "unknowncmd");
        Command c = Command.generate(r);
        assertTrue(c instanceof HelpCommand);
    }
}
