package za.co.wethinkcode.robots.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;

public class ProtocolTest {

    @Test
    public void testEncodeDecodeRequest() {
        Protocol p = new Protocol();
        ServerRequest req = new ServerRequest("HAL","launch", new String[0]);
        String json = p.encodeRequest(req);
        assertNotNull(json);
        ServerRequest parsed = p.decodeRequest(json);
        assertEquals(req.getRobot(), parsed.getRobot());
        assertEquals(req.getCommand(), parsed.getCommand());
    }

    @Test
    public void testEncodeDecodeResponseAndUpdate() {
        Protocol p = new Protocol();
        ServerResponse res = new ServerResponse();
        ServerResponseData data = new ServerResponseData();
        data.setMessage("hello");
        res.setData(data);
        res.setResult(null);
        String json = p.encodeResponse(res);
        assertNotNull(json);
        ServerResponse parsed = p.decodeResponse(json);
        assertEquals("hello", parsed.getData().getMessage());

        ServerResponse old = new ServerResponse();
        p.updatResponse(old, parsed);
        assertEquals("hello", old.getData().getMessage());
    }
}