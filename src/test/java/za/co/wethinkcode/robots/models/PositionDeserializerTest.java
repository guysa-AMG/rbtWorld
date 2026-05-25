package za.co.wethinkcode.robots.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class PositionDeserializerTest {

    @Test
    public void testArrayDeserialization() throws Exception {
        ObjectMapper m = new ObjectMapper();
        Position p = m.readValue("[3,4]", Position.class);
        assertNotNull(p);
        assertEquals(3, p.getX());
        assertEquals(4, p.getY());
    }

    @Test
    public void testObjectDeserialization() throws Exception {
        ObjectMapper m = new ObjectMapper();
        Position p = m.readValue("{\"x\":10,\"y\":20}", Position.class);
        assertNotNull(p);
        assertEquals(10, p.getX());
        assertEquals(20, p.getY());
    }
}
