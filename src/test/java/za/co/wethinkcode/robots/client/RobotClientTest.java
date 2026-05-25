package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.errors.InvalidCommandException;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;

public class RobotClientTest {

    @Test
    public void testToRequest_valid() throws Exception {
        ServerRequest req = RobotClient.toRequest("HAL launch");
        assertNotNull(req);
        assertEquals("HAL", req.getRobot());
        assertEquals("launch", req.getCommand());
    }

    @Test
    public void testToRequest_invalidCommandThrows() {
        assertThrows(InvalidCommandException.class, () -> RobotClient.toRequest("HAL unknowncmd"));
    }
}
