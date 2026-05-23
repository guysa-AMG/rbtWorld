package za.co.wethinkcode.robots.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RobotClientResponseTest {

    @Test
    void noArgConstructor_leavesAllFieldsNull() {
        ServerResponse response = new ServerResponse();

        assertNull(response.getResult());
        assertNull(response.getData());
        assertNull(response.getState());
    }

    @Test
    void settersAndGetters_roundTripValues() {
        ServerResponse response = new ServerResponse().builder()
                                                      .result(StatusCode.OK)
                                                      .data(
                                                        ServerResponseData.builder()
                                                                          .message("Done")
                                                                          .build()
                                                      )
                                                      .state(
                                                        ServerResponseState.builder()
                                                                            .status(OperationalMode.NORMAL)
                                                                            .build()
                                                      )
                                                      .build();
                                                               

        // response.setResult("OK");
        // response.setMessage("Done");
        // response.setData("payload");
        // response.setState("ready");

        assertEquals(StatusCode.OK, response.getResult());
        assertEquals("Done", response.getData().getMessage());
        
        assertEquals(OperationalMode.NORMAL, response.getState().getStatus());
    }

    @Test
    void jacksonDeserialization_buildsFullResponseFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"data\":{\"message\":\"Launched\"},\"state\":null}";

        ServerResponse response = mapper.readValue(json, ServerResponse.class);

        assertEquals(StatusCode.OK, response.getResult());
        assertEquals("Launched", response.getData().getMessage());
        //assertNull(response.getData());
        assertNull(response.getState());
    }

    @Test
    void jacksonDeserialization_missingFieldsAreNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\"}";

        ServerResponse response = mapper.readValue(json, ServerResponse.class);

        assertEquals("OK", response.getResult().toString());
       // assertNull(response.getData().getMessage());
        assertNull(response.getData());
        assertNull(response.getState());
    }

    @Test
    void jacksonDeserialization_failsOnUnknownFieldsByDefault() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"surpriseField\":\"unexpected\"}";

        assertThrows(Exception.class,
                () -> mapper.readValue(json, ServerResponse.class));
    }
    @Disabled
    @Test
    void dataField_canHoldNestedMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"data\":{\"message\":\"Hit\",\"data\":{\"distance\":3,\"hit\":\"R2\"}}";

        ServerResponse response = mapper.readValue(json, ServerResponse.class);

        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof Map,
                "data should be a Map, got: " + response.getData().getClass());
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertEquals(3, data.get("distance"));
        assertEquals("R2", data.get("hit"));
    }

    @Disabled
    @Test
    void stateField_canHoldNestedObjectStructure() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"result\":\"OK\",\"data\":{\"message\":\"\"},"
                + "\"state\":{\"position\":{\"5\",\"3\"},\"direction\":\"NORTH\",\"shields\":10,\"shots\":5}}";

        ServerResponse response = mapper.readValue(json, ServerResponse.class);

        assertNotNull(response.getState());
        Map<String, Object> state = (Map<String, Object>) response.getState();
        assertEquals("NORTH", state.get("direction"));
        assertEquals(10, state.get("shields"));
        assertEquals(5, state.get("shots"));
    }





}
