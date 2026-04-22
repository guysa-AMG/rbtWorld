// # Test if JSON messages are formatted correctly
package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.shared.Protocol;

public class ProtocolTest{

     //TODO Please Comment this line if Protocol is implemented
    @Disabled("waiting on Protocols implementation")
    @Test
    void testForwardSerialization(){
        Protocol proto = new Protocol();
        ServerRequest req = new ServerRequest("mark", "Forward", new String[]{"10"});

        assertEquals(proto.encodeRequest(req),"{robot: mark, command: forward, arguments: [10]}");
    }


    //TODO Please Comment this line if Protocol is implemented
     @Disabled("waiting on Protocols implementation")
    @Test
    void testBackwardSerialization(){
        Protocol proto = new Protocol();
        ServerRequest req = new ServerRequest("mark", "Forward", new String[]{"10"});

        assertEquals(req,proto.decodeRequest("{robot: mark, command: forward, arguments: [10]}"));
    }


        //TODO Please Comment this line if Protocol is implemented
       @Disabled("waiting on Protocols implementation")
    @Test
    void testForwardDeSerialization(){
     //TODO Please implement me
    }



     @Disabled("waiting on Protocols implementation")
    @Test
    void testBackwardDeSerialization(){
        //TODO Please implement me
    }

  

}