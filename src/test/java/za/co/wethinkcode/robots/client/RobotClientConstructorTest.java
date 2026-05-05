package za.co.wethinkcode.robots.client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class RobotClientConstructorTest {
    @Test
    void testValidConstructor(){
        assertDoesNotThrow(() -> new RobotClient("localhost", 2146));
    }

    @Test
    void testBlankHost(){
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RobotClient("  ", 2146)
        );
        assertEquals("host must not be blank", exception.getMessage());
    }

    @Test
    void testNullHost(){
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
            () -> new RobotClient(null, 2146)
        );
        // expected message: Host cannot be null
        assertTrue("Host cannot be null".equals(exception.getMessage()) || 
                   "host must not be blank".equals(exception.getMessage()),
                   "Unexpected exception message: " + exception.getMessage());
    }

    @Test
    void testInvalidPortTooLow(){
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RobotClient("localhost", 0)
        );
        assertEquals("port must be between 1 and 65535", exception.getMessage());
    }

    @Test
    void testInvalidPortTooHigh(){
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new RobotClient("localhost", 65536)
        );
        assertEquals("port must be between 1 and 65535", exception.getMessage());
    }

    @Test
    void constructor_rejectsEmptyHost(){
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RobotClient("", 2146)
        );
        assertEquals("host must not be blank", ex.getMessage());
    }
    // this test will test whitespace A string of just spaces (" ") is rejected just like null and ""
    @Test
    void constructor_rejectsBlankHost(){
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RobotClient(" ", 2146)
        );
        assertEquals("host must not be blank", ex.getMessage());
    }

    @Test
    void constructor_rejectsZeroPort() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RobotClient("localhost", 0)
        );
        assertEquals("port must be between 1 and 65535", ex.getMessage());
    }

    @Test
    void constructor_rejectsNegativePort() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RobotClient("localhost", -1)
        );
        assertEquals("port must be between 1 and 65535", ex.getMessage());
    }

    @Test
    void constructor_rejectsPortAboveMax() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RobotClient("localhost", 65536)
        );
        assertEquals("port must be between 1 and 65535", ex.getMessage());
    }

    // Port = 1 is accepted (lower boundary inclusive)
    @Test
    void constructor_acceptsPortAtLowerBound() {
        assertDoesNotThrow(() -> new RobotClient("localhost", 1));
    }




}
