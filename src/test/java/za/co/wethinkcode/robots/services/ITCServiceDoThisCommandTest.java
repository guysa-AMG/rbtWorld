package za.co.wethinkcode.robots.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.shared.Protocol;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;

public class ITCServiceDoThisCommandTest {

    @Test
    public void testDoThisCommandLaunch() {
        ITCService svc = ITCService.getInstance();
        var world = WorldGenerator.build();
        svc.setWorld(world);

        Protocol p = new Protocol();
        ServerRequest req = ServerRequest.builder().robot("tester").command("launch").arguments(new String[]{"balanced"}).build();
        String json = p.encodeRequest(req);

        String res = svc.doThisCommand(json,null);
        assertNotNull(res);
        // Robot should have been added to the world
        assertTrue(world.getAllRobots().containsKey("tester"));
    }
}
