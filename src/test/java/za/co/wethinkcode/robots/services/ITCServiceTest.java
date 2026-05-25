package za.co.wethinkcode.robots.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;

public class ITCServiceTest {

    @Test
    public void testPushAndBroadcast() {
        ITCService svc = ITCService.getInstance();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        svc.registerClient("c1", pw);

        ServerResponse evt = ServerResponse.builder().result(za.co.wethinkcode.robots.models.StatusCode.OK)
                .data(ServerResponseData.builder().message("hello").build()).build();
        svc.pushEvent("c1", evt);
        String out = sw.toString();
        assertTrue(out.contains("hello"));

        svc.unregisterClient("c1");
    }
}
