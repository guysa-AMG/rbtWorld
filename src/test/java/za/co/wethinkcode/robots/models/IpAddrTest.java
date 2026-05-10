package za.co.wethinkcode.robots.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class IpAddrTest {

    @Test
    void record_storesIpAndPort() {
        IpAddr addr = new IpAddr("192.168.1.10", 2146);
        assertEquals("192.168.1.10", addr.ip());
        assertEquals(2146, addr.port());
    }

    @Test
    void record_acceptsLocalhost() {
        IpAddr addr = new IpAddr("localhost", 8080);
        assertEquals("localhost", addr.ip());
        assertEquals(8080, addr.port());
    }

    @Test
    void equals_trueForSameValues() {
        IpAddr a = new IpAddr("10.0.0.1", 5000);
        IpAddr b = new IpAddr("10.0.0.1", 5000);
        assertEquals(a, b);
    }

    @Test
    void equals_falseForDifferentPort() {
        IpAddr a = new IpAddr("10.0.0.1", 5000);
        IpAddr b = new IpAddr("10.0.0.1", 5001);
        assertNotEquals(a, b);
    }

    @Test
    void equals_falseForDifferentIp() {
        IpAddr a = new IpAddr("10.0.0.1", 5000);
        IpAddr b = new IpAddr("10.0.0.2", 5000);
        assertNotEquals(a, b);
    }

    @Test
    void hashCode_consistentForEqualRecords() {
        IpAddr a = new IpAddr("127.0.0.1", 9000);
        IpAddr b = new IpAddr("127.0.0.1", 9000);
        assertEquals(a.hashCode(), b.hashCode());
    }
}