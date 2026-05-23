package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;

public class ErrorCommandTest {

    @Test
    void execute_returnsErrorResultWithMessage() {
        ErrorCommand cmd = new ErrorCommand("robot HAL has not been launched", "HAL");
        ServerResponse res = cmd.execute(null, null);

        assertNotNull(res);
        assertEquals(StatusCode.ERROR, res.getResult());
        assertNotNull(res.getData());
        assertEquals("robot HAL has not been launched", res.getData().getMessage());
    }

    @Test
    void getCommandName_isError() {
        ErrorCommand cmd = new ErrorCommand("any message", "Mark");
        assertEquals("error", cmd.getCommandName());
    }

    @Test
    void getRobotName_returnsConstructorValue() {
        ErrorCommand cmd = new ErrorCommand("any message", "Mark");
        assertEquals("Mark", cmd.getRobotName());
    }

    @Test
    void getAttribute_holdsTheErrorMessage() {
        ErrorCommand cmd = new ErrorCommand("custom failure text", "HAL");
        assertEquals("custom failure text", cmd.getAttribute());
    }
}