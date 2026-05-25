package za.co.wethinkcode.robots.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.errors.InvalidCommandException;
import za.co.wethinkcode.robots.errors.ParsingException;
import za.co.wethinkcode.robots.errors.UnsupportedException;
import za.co.wethinkcode.robots.models.impediment.ImpedimentsType;
import za.co.wethinkcode.robots.server.commands.CommandTypeEnum;

public class EnumsTest {

    @Nested
    @DisplayName("Directions enum")
    class DirectionsEnum {
        @Test void hasFourValues() { assertEquals(4, Directions.values().length); }
        @Test void valueOfNorth() { assertEquals(Directions.NORTH, Directions.valueOf("NORTH")); }
        @Test void valueOfSouth() { assertEquals(Directions.SOUTH, Directions.valueOf("SOUTH")); }
        @Test void valueOfEast()  { assertEquals(Directions.EAST,  Directions.valueOf("EAST")); }
        @Test void valueOfWest()  { assertEquals(Directions.WEST,  Directions.valueOf("WEST")); }
    }

    @Nested
    @DisplayName("StatusCode enum")
    class StatusCodeEnum {
        @Test void hasTwoValues() { assertEquals(2, StatusCode.values().length); }
        @Test void okExists()    { assertEquals(StatusCode.OK, StatusCode.valueOf("OK")); }
        @Test void errorExists() { assertEquals(StatusCode.ERROR, StatusCode.valueOf("ERROR")); }
    }

    @Nested
    @DisplayName("OperationalMode enum")
    class OperationalModeEnum {
        @Test void hasFourValues() { assertEquals(4, OperationalMode.values().length); }
        @Test void normalExists() { assertEquals(OperationalMode.NORMAL, OperationalMode.valueOf("NORMAL")); }
        @Test void deadExists()   { assertEquals(OperationalMode.DEAD,   OperationalMode.valueOf("DEAD")); }
        @Test void reloadExists() { assertEquals(OperationalMode.RELOAD, OperationalMode.valueOf("RELOAD")); }
        @Test void repairExists() { assertEquals(OperationalMode.REPAIR, OperationalMode.valueOf("REPAIR")); }
    }

    @Nested
    @DisplayName("ImpedimentType enum")
    class ImpedimentTypeEnum {
        @Test void hasThreeValues()  { assertEquals(3, ImpedimentType.values().length); }
        @Test void obstacleExists()  { assertEquals(ImpedimentType.OBSTACLE, ImpedimentType.valueOf("OBSTACLE")); }
        @Test void robotExists()     { assertEquals(ImpedimentType.ROBOT,    ImpedimentType.valueOf("ROBOT")); }
        @Test void edgeExists()      { assertEquals(ImpedimentType.EDGE,     ImpedimentType.valueOf("EDGE")); }
    }

    @Nested
    @DisplayName("CommandTypeEnum")
    class CommandTypeEnumTest {
        @Test void containsExpectedMembers() {
            assertNotNull(CommandTypeEnum.valueOf("launch"));
            assertNotNull(CommandTypeEnum.valueOf("fire"));
            assertNotNull(CommandTypeEnum.valueOf("subscribe"));
            assertNotNull(CommandTypeEnum.valueOf("unsubscribe"));
        }
        @Test void hasShutdownMember() { assertNotNull(CommandTypeEnum.valueOf("shutdown")); }
    }

    @Nested
    @DisplayName("ImpedimentsType marker annotations")
    class ImpedimentsTypeMarkers {
        @Test void typeIsInstantiable() {
            assertNotNull(new ImpedimentsType());
        }
    }

    @Nested
    @DisplayName("Exception types")
    class Exceptions {
        @Test void invalidCommandIsThrowable() {
            assertThrows(InvalidCommandException.class, () -> { throw new InvalidCommandException(); });
        }
        @Test void parsingExceptionIsThrowable() {
            assertThrows(ParsingException.class, () -> { throw new ParsingException(); });
        }
        @Test void unsupportedExceptionIsThrowable() {
            assertThrows(UnsupportedException.class, () -> { throw new UnsupportedException(); });
        }
    }

    @Nested
    @DisplayName("IpAddr record")
    class IpAddrRecord {
        @Test void recordStoresIpAndPort() {
            IpAddr a = new IpAddr("127.0.0.1", 5000);
            assertEquals("127.0.0.1", a.ip());
            assertEquals(5000, a.port());
        }

        @Test void recordEqualityByValue() {
            assertEquals(new IpAddr("h", 1), new IpAddr("h", 1));
        }
    }
}
