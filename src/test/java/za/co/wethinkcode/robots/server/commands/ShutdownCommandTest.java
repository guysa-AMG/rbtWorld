package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ShutdownCommandTest {

    /**
     * NOTE: ShutdownCommand.execute is currently unimplemented and throws
     * UnsupportedOperationException. These tests pin that contract until
     * the real shutdown logic lands.
     */

    @Test
    void execute_throwsUnsupportedOperationException() {
        ShutdownCommand cmd = new ShutdownCommand("HAL");
        assertThrows(UnsupportedOperationException.class,
                () -> cmd.execute(null, null));
    }

    @Test
    void getCommandName_isShutdown() {
        assertEquals("shutdown", new ShutdownCommand("HAL").getCommandName());
    }

    @Test
    void getRobotName_returnsConstructorValue() {
        assertEquals("HAL", new ShutdownCommand("HAL").getRobotName());
    }
}