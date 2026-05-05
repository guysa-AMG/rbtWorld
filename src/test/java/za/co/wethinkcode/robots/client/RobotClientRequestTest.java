package za.co.wethinkcode.robots.client;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class RobotClientRequestTest {

    @Test
    void noArgConstructor_leavesAllFieldsNull() {
        RobotClient.Request request = new RobotClient.Request();

        assertNull(request.getRobot());
        assertNull(request.getCommand());
        assertNull(request.getArguments());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        String[] args = {"forward", "5"};

        RobotClient.Request request = new RobotClient.Request("HAL", "move", args);

        assertEquals("HAL", request.getRobot());
        assertEquals("move", request.getCommand());
        assertArrayEquals(new String[]{"forward", "5"}, request.getArguments());
    }

    @Test
    void settersAndGetters_roundTripValues() {
        RobotClient.Request request = new RobotClient.Request();

        request.setRobot("R2D2");
        request.setCommand("look");
        request.setArguments(new String[]{"north"});

        assertEquals("R2D2", request.getRobot());
        assertEquals("look", request.getCommand());
        assertArrayEquals(new String[]{"north"}, request.getArguments());
    }

    @Test
    void setArguments_acceptsNullWithoutThrowing() {
        RobotClient.Request request = new RobotClient.Request("HAL", "quit", new String[0]);

        assertDoesNotThrow(() -> request.setArguments(null));
        assertNull(request.getArguments());
    }

    @Test
    void jacksonSerialization_producesExpectedJsonShape() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RobotClient.Request request = new RobotClient.Request("HAL", "launch", new String[0]);

        String json = mapper.writeValueAsString(request);

        assertEquals("{\"robot\":\"HAL\",\"command\":\"launch\",\"arguments\":[]}", json);
    }

    @Test
    void jacksonDeserialization_buildsRequestFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"robot\":\"HAL\",\"command\":\"launch\",\"arguments\":[]}";

        RobotClient.Request request = mapper.readValue(json, RobotClient.Request.class);

        assertEquals("HAL", request.getRobot());
        assertEquals("launch", request.getCommand());
        assertArrayEquals(new String[0], request.getArguments());
    }

    @Test
    void roundTrip_jsonPreservesAllFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RobotClient.Request original = new RobotClient.Request(
                "R2D2",
                "move",
                new String[]{"forward", "5"}
        );

        String json = mapper.writeValueAsString(original);
        RobotClient.Request rebuilt = mapper.readValue(json, RobotClient.Request.class);

        assertEquals(original.getRobot(), rebuilt.getRobot());
        assertEquals(original.getCommand(), rebuilt.getCommand());
        assertArrayEquals(original.getArguments(), rebuilt.getArguments());
    }

    // Special characters in robot name escape correctly
    @Test
    void specialCharactersInRobotName_escapedCorrectlyInJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RobotClient.Request original = new RobotClient.Request(
                "HAL\"with\\quotes",
                "launch",
                new String[0]
        );

        String json = mapper.writeValueAsString(original);
        RobotClient.Request rebuilt = mapper.readValue(json, RobotClient.Request.class);

        assertTrue(json.contains("HAL\\\"with\\\\quotes"),
                "JSON should escape both the double-quote and the backslash. Got: " + json);
        assertEquals("HAL\"with\\quotes", rebuilt.getRobot());
    }

}
