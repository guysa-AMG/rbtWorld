package za.co.wethinkcode.robots.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RobotClientResponseTest {

    @Test
    void noArgConstructor_leavesAllFieldsNull() {
        RobotClient.Response response = new RobotClient.Response();

        assertNull(response.getResult());
        assertNull(response.getMessage());
        assertNull(response.getData());
        assertNull(response.getState());
    }

    @Test
    void settersAndGetters_roundTripValues() {
        RobotClient.Response response = new RobotClient.Response();

        response.setResult("OK");
        response.setMessage("Done");
        response.setData("payload");
        response.setState("ready");

        assertEquals("OK", response.getResult());
        assertEquals("Done", response.getMessage());
        assertEquals("payload", response.getData());
        assertEquals("ready", response.getState());
    }

    @Test
    void jacksonDeserialization_buildsFullResponseFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"message\":\"Launched\",\"data\":null,\"state\":null}";

        RobotClient.Response response = mapper.readValue(json, RobotClient.Response.class);

        assertEquals("OK", response.getResult());
        assertEquals("Launched", response.getMessage());
        assertNull(response.getData());
        assertNull(response.getState());
    }

    @Test
    void jacksonDeserialization_missingFieldsAreNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\"}";

        RobotClient.Response response = mapper.readValue(json, RobotClient.Response.class);

        assertEquals("OK", response.getResult());
        assertNull(response.getMessage());
        assertNull(response.getData());
        assertNull(response.getState());
    }

    @Test
    void jacksonDeserialization_failsOnUnknownFieldsByDefault() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"surpriseField\":\"unexpected\"}";

        assertThrows(Exception.class,
                () -> mapper.readValue(json, RobotClient.Response.class));
    }

    @Test
    void dataField_canHoldNestedMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"message\":\"Hit\",\"data\":{\"distance\":3,\"hit\":\"R2\"}}";

        RobotClient.Response response = mapper.readValue(json, RobotClient.Response.class);

        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof Map,
                "data should be a Map, got: " + response.getData().getClass());
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertEquals(3, data.get("distance"));
        assertEquals("R2", data.get("hit"));
    }


    @Test
    void stateField_canHoldNestedObjectStructure() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"message\":\"\","
                + "\"state\":{\"position\":[5,3],\"direction\":\"NORTH\",\"shields\":10,\"shots\":5}}";

        RobotClient.Response response = mapper.readValue(json, RobotClient.Response.class);

        assertNotNull(response.getState());
        Map<String, Object> state = (Map<String, Object>) response.getState();
        assertEquals("NORTH", state.get("direction"));
        assertEquals(10, state.get("shields"));
        assertEquals(5, state.get("shots"));
    }





}
