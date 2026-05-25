// # Test if JSON messages are formatted correctly
package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.shared.Protocol;

public class ProtocolTest{

     //TODO Please Comment this line if Protocol is implemented
   // @Disabled("waiting on Protocols implementation")
    @Test
    void testForwardSerialization(){
        Protocol proto = new Protocol();
        ServerRequest req = new ServerRequest("mark", "Forward", new String[]{"10"});

        assertEquals("{'robot':'mark','command':'Forward','arguments':['10']}",proto.encodeRequest(req).replace("\"", "\'").replaceAll("\n", "")

          );
    }

    @Test
    void testBackwardSerialization(){
        Protocol proto = new Protocol();
        ServerRequest req = new ServerRequest("mark", "Forward", new String[]{"10"});

        assertEquals(req,proto.decodeRequest("{  \"robot\" : \"mark\",  \"command\" : \"Forward\",  \"arguments\" : [ \"10\" ]}"));
    }


        //TODO Please Comment this line if Protocol is implemented
       @Disabled("waiting on Protocols implementation")
    @Test
    void testForwardDeSerialization(){

    }



     @Disabled("waiting on Protocols implementation")
    @Test
    void testBackwardDeSerialization(){
        //TODO Please implement me
    }

    @Nested
    @DisplayName("Response encoding & decoding")
    class ResponseRoundTrip {

        @Test
        void encodeResponse_producesJsonWithResultField() {
            Protocol proto = new Protocol();
            ServerResponse res = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("ok").build())
                    .build();
            String json = proto.encodeResponse(res);
            assertNotNull(json);
            assertEquals(true, json.contains("\"result\":\"OK\""),
                    "Expected JSON to contain result field. Got: " + json);
        }

        @Test
        void decodeResponse_buildsResponseFromJson() {
            Protocol proto = new Protocol();
            String json = "{\"result\":\"OK\",\"data\":{\"message\":\"launched\"}}";
            ServerResponse res = proto.decodeResponse(json);
            assertNotNull(res);
            assertEquals(StatusCode.OK, res.getResult());
            assertEquals("launched", res.getData().getMessage());
        }


      
    }

    @Nested
    @DisplayName("Request validation in decodeRequest")
    class RequestValidation {

        @Test
        void decodeRequest_throwsWhenRobotMissing() {
            Protocol proto = new Protocol();
            String json = "{\"command\":\"forward\",\"arguments\":[\"5\"]}";
            assertThrows(UnsupportedOperationException.class,
                    () -> proto.decodeRequest(json));
        }

        @Test
        void decodeRequest_throwsWhenCommandMissing() {
            Protocol proto = new Protocol();
            String json = "{\"robot\":\"HAL\",\"arguments\":[\"5\"]}";
            assertThrows(UnsupportedOperationException.class,
                    () -> proto.decodeRequest(json));
        }

        @Test
        void decodeRequest_throwsWhenArgumentsMissing() {
            Protocol proto = new Protocol();
            String json = "{\"robot\":\"HAL\",\"command\":\"forward\"}";
            assertThrows(UnsupportedOperationException.class,
                    () -> proto.decodeRequest(json));
        }

        @Test
        void decodeRequest_throwsOnMalformedJson() {
            Protocol proto = new Protocol();
            assertThrows(UnsupportedOperationException.class,
                    () -> proto.decodeRequest("{ not valid json"));
        }
    }
}