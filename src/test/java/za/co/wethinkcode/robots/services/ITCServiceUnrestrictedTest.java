package za.co.wethinkcode.robots.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.shared.Protocol;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;

public class ITCServiceUnrestrictedTest {

    @Test
    public void testDoThisCommandUnRestrictedExecutesServerCommand() {
        ITCService svc = ITCService.getInstance();
        var world = WorldGenerator.build();
        svc.setWorld(world);

        Protocol p = new Protocol();
        ServerRequest req = ServerRequest.builder().robot("srv").command("robots").arguments(new String[]{}).build();
        String json = p.encodeRequest(req);

        String respJson = svc.doThisCommandUnRestricted(json);
        assertNotNull(respJson);
        ServerResponse res = p.decodeResponse(respJson);
        assertNotNull(res);
        // robots command should return OK and include data
        assertEquals(za.co.wethinkcode.robots.models.StatusCode.OK, res.getResult());
    }
}
