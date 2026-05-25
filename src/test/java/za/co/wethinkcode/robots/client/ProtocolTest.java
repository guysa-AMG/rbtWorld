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

        @Test
        void roundTripResponse_preservesResultAndMessage() {
            Protocol proto = new Protocol();
            ServerResponse original = ServerResponse.builder()
                    .result(StatusCode.ERROR)
                    .data(ServerResponseData.builder().message("oops").build())
                    .build();
            String json = proto.encodeResponse(original);
            ServerResponse rebuilt = proto.decodeResponse(json);
            assertEquals(original.getResult(), rebuilt.getResult());
            assertEquals(original.getData().getMessage(), rebuilt.getData().getMessage());
        }

        @Test
        void decodeResponse_ignoresUnknownFields() {
            Protocol proto = new Protocol();
            String json = "{\"result\":\"OK\",\"data\":{\"message\":\"hi\"},\"someExtra\":\"value\"}";
            ServerResponse res = proto.decodeResponse(json);
            assertEquals(StatusCode.OK, res.getResult());
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

    @Nested
    @DisplayName("encodeRequest & updatResponse")
    class EncodeAndMerge {

        @Test
        void encodeRequest_emitsRobotAndCommandFields() {
            Protocol p = new Protocol();
            ServerRequest req = new ServerRequest("HAL", "look", new String[]{});
            String json = p.encodeRequest(req);
            org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"robot\":\"HAL\""));
            org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"command\":\"look\""));
        }

        @Test
        void updatResponse_runsWithoutError() {
            Protocol p = new Protocol();
            ServerResponse old = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("old").build())
                    .build();
            ServerResponse fresh = ServerResponse.builder()
                    .result(StatusCode.ERROR)
                    .data(ServerResponseData.builder().message("new").build())
                    .build();
            p.updatResponse(old, fresh);
            assertNotNull(old);
        }
    }
}