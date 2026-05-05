package za.co.wethinkcode.robots.client;
import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.ServerRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class RobotClientRequestTest {

    @Test
    void noArgConstructor_leavesAllFieldsNull() {
        ServerRequest request = new ServerRequest();

        assertNull(request.getRobot());
        assertNull(request.getCommand());
        assertNull(request.getArguments());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        String[] args = {"forward", "5"};

        ServerRequest request = new ServerRequest("HAL", "move", args);

        assertEquals("HAL", request.getRobot());
        assertEquals("move", request.getCommand());
        assertArrayEquals(new String[]{"forward", "5"}, request.getArguments());
    }

    @Test
    void settersAndGetters_roundTripValues() {
        ServerRequest request = new ServerRequest();

        request.setRobot("R2D2");
        request.setCommand("look");
        request.setArguments(new String[]{"north"});

        assertEquals("R2D2", request.getRobot());
        assertEquals("look", request.getCommand());
        assertArrayEquals(new String[]{"north"}, request.getArguments());
    }

    @Test
    void setArguments_acceptsNullWithoutThrowing() {
        ServerRequest request = new ServerRequest("HAL", "quit", new String[0]);

        assertDoesNotThrow(() -> request.setArguments(null));
        assertNull(request.getArguments());
    }

    @Test
    void jacksonSerialization_producesExpectedJsonShape() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ServerRequest request = new ServerRequest("HAL", "launch", new String[0]);

        String json = mapper.writeValueAsString(request);

        assertEquals("{\"robot\":\"HAL\",\"command\":\"launch\",\"arguments\":[]}", json);
    }

    @Test
    void jacksonDeserialization_buildsRequestFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"robot\":\"HAL\",\"command\":\"launch\",\"arguments\":[]}";

        ServerRequest request = mapper.readValue(json, ServerRequest.class);

        assertEquals("HAL", request.getRobot());
        assertEquals("launch", request.getCommand());
        assertArrayEquals(new String[0], request.getArguments());
    }

    @Test
    void roundTrip_jsonPreservesAllFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ServerRequest original = new ServerRequest(
                "R2D2",
                "move",
                new String[]{"forward", "5"}
        );

        String json = mapper.writeValueAsString(original);
       ServerRequest rebuilt = mapper.readValue(json, ServerRequest.class);

        assertEquals(original.getRobot(), rebuilt.getRobot());
        assertEquals(original.getCommand(), rebuilt.getCommand());
        assertArrayEquals(original.getArguments(), rebuilt.getArguments());
    }

    // Special characters in robot name escape correctly
    @Test
    void specialCharactersInRobotName_escapedCorrectlyInJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ServerRequest original = new ServerRequest(
                "HAL\"with\\quotes",
                "launch",
                new String[0]
        );

        String json = mapper.writeValueAsString(original);
        ServerRequest rebuilt = mapper.readValue(json, ServerRequest.class);

        assertTrue(json.contains("HAL\\\"with\\\\quotes"),
                "JSON should escape both the double-quote and the backslash. Got: " + json);
        assertEquals("HAL\"with\\quotes", rebuilt.getRobot());
    }

}
