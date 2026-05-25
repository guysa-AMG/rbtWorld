package za.co.wethinkcode.robots.models.transitmodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.PositionDeserializer;
import za.co.wethinkcode.robots.models.StatusCode;

public class TransitModelsTest {

    @Nested
    @DisplayName("ServerRequest")
    class Request {
        @Test void twoArgConstructorSetsEmptyArguments() {
            ServerRequest r = new ServerRequest("HAL", "look");
            assertEquals("HAL", r.getRobot());
            assertEquals("look", r.getCommand());
            assertEquals(0, r.getArguments().length);
        }

        @Test void threeArgConstructorStoresArgs() {
            ServerRequest r = new ServerRequest("HAL", "forward", new String[]{"5"});
            assertEquals("5", r.getArguments()[0]);
        }

        @Test void builderProducesEquivalentRequest() {
            ServerRequest a = ServerRequest.builder().robot("HAL").command("look").arguments(new String[]{}).build();
            assertEquals("HAL", a.getRobot());
            assertEquals("look", a.getCommand());
        }

        @Test void settersUpdateFields() {
            ServerRequest r = new ServerRequest();
            r.setRobot("X");
            r.setCommand("turn");
            r.setArguments(new String[]{"left"});
            assertEquals("X", r.getRobot());
            assertEquals("left", r.getArguments()[0]);
        }
    }

    @Nested
    @DisplayName("ServerResponse + state + data")
    class Response {

        private ServerResponse build(String msg) {
            ServerResponseState s = ServerResponseState.builder()
                    .position(new Position(0, 0)).direction(Directions.NORTH)
                    .shields(3).shots(3).status(OperationalMode.NORMAL).build();
            ServerResponseData d = ServerResponseData.builder()
                    .message(msg).position(new Position(0, 0)).visibility(5)
                    .reload(5f).repair(3f).shields(3).build();
            return ServerResponse.builder().result(StatusCode.OK).data(d).state(s).build();
        }

        @Test void equalsTrueForSameData() {
            assertTrue(build("hi").equals(build("hi")));
        }

        @Test void responseHoldsResultEnum() {
            assertEquals(StatusCode.OK, build("hi").getResult());
        }

        @Test void dataEqualsConsidersFields() {
            ServerResponseData a = ServerResponseData.builder()
                    .message("m").position(new Position(0, 0)).visibility(1)
                    .reload(1f).repair(1f).shields(1).build();
            ServerResponseData b = ServerResponseData.builder()
                    .message("m").position(new Position(0, 0)).visibility(1)
                    .reload(1f).repair(1f).shields(1).build();
            assertTrue(a.equals(b));
        }

        @Test void stateEqualsConsidersFields() {
            ServerResponseState a = ServerResponseState.builder()
                    .position(new Position(1, 1)).direction(Directions.EAST)
                    .shields(2).shots(2).status(OperationalMode.NORMAL).build();
            ServerResponseState b = ServerResponseState.builder()
                    .position(new Position(1, 1)).direction(Directions.EAST)
                    .shields(2).shots(2).status(OperationalMode.NORMAL).build();
            assertTrue(a.equals(b));
        }
    }

    @Nested
    @DisplayName("ServerResponseObject")
    class Objects {

        @Test void builderPopulatesFields() {
            ServerResponseObject o = ServerResponseObject.builder()
                    .direction(Directions.NORTH).distance(3)
                    .subtype("WALL").name("X")
                    .position(new Position(0, 3))
                    .build();
            assertEquals(Directions.NORTH, o.getDirection());
            assertEquals(3, o.getDistance());
            assertEquals("WALL", o.getSubtype());
            assertEquals("X", o.getName());
            assertNotNull(o.getPosition());
        }

        @Test void noArgsBuildsEmpty() {
            ServerResponseObject o = new ServerResponseObject();
            assertEquals(0, o.getDistance());
        }
    }

    @Nested
    @DisplayName("ServerResponseRobot")
    class ResponseRobot {
        @Test void builderStoresAllStats() {
            ServerResponseRobot r = ServerResponseRobot.builder()
                    .name("HAL").position(new Position(0, 0))
                    .direction(Directions.NORTH).lives(3)
                    .shields(5).shots(5).kills(2)
                    .status(OperationalMode.NORMAL)
                    .build();
            assertEquals("HAL", r.getName());
            assertEquals(3, r.getLives());
            assertEquals(5, r.getShields());
            assertEquals(5, r.getShots());
            assertEquals(2, r.getKills());
            assertEquals(OperationalMode.NORMAL, r.getStatus());
        }
    }

    @Nested
    @DisplayName("Setter coverage on data classes")
    class Setters {
        @Test void responseObjectSettersRoundTrip() {
            ServerResponseObject o = new ServerResponseObject();
            o.setName("n"); o.setDistance(2); o.setSubtype("WALL");
            o.setDirection(Directions.NORTH); o.setPosition(new Position(0, 0));
            o.setType(za.co.wethinkcode.robots.models.ImpedimentType.OBSTACLE);
            assertEquals("n", o.getName());
            assertEquals(2, o.getDistance());
            assertEquals("WALL", o.getSubtype());
            assertEquals(Directions.NORTH, o.getDirection());
            assertNotNull(o.getPosition());
            assertEquals(za.co.wethinkcode.robots.models.ImpedimentType.OBSTACLE, o.getType());
        }

        @Test void responseRobotSettersRoundTrip() {
            ServerResponseRobot r = new ServerResponseRobot();
            r.setName("HAL"); r.setDirection(Directions.NORTH);
            r.setLives(3); r.setShields(5); r.setShots(5);
            r.setKills(2); r.setStatus(OperationalMode.NORMAL);
            r.setPosition(new Position(1, 1));
            assertEquals("HAL", r.getName());
            assertEquals(Directions.NORTH, r.getDirection());
            assertEquals(3, r.getLives());
            assertEquals(5, r.getShields());
            assertEquals(5, r.getShots());
            assertEquals(2, r.getKills());
            assertEquals(OperationalMode.NORMAL, r.getStatus());
            assertEquals(1, r.getPosition().getX());
        }

        @Test void responseStateSettersRoundTrip() {
            ServerResponseState s = new ServerResponseState();
            s.setPosition(new Position(0, 0));
            s.setDirection(Directions.EAST);
            s.setShields(2);
            s.setShots(2);
            s.setStatus(OperationalMode.NORMAL);
            assertEquals(Directions.EAST, s.getDirection());
            assertEquals(2, s.getShields());
        }

        @Test void responseDataSettersRoundTrip() {
            ServerResponseData d = new ServerResponseData();
            d.setMessage("m"); d.setDistance(1); d.setRobot("HAL");
            d.setVisibility(5); d.setReload(1.0f); d.setRepair(1.0f);
            d.setShields(3); d.setPosition(new Position(0, 0));
            assertEquals("m", d.getMessage());
            assertEquals("HAL", d.getRobot());
            assertEquals(5, d.getVisibility());
        }
    }

    @Nested
    @DisplayName("PositionDeserializer JSON parsing")
    class Deserializer {
        @Test void parsesArrayForm() throws Exception {
            ObjectMapper m = new ObjectMapper();
            m.findAndRegisterModules();
            Position p = m.readValue("[3,5]", Position.class);
            assertEquals(3, p.getX());
            assertEquals(5, p.getY());
        }

        @Test void parsesObjectForm() throws Exception {
            ObjectMapper m = new ObjectMapper();
            m.findAndRegisterModules();
            Position p = m.readValue("{\"x\":-2,\"y\":7}", Position.class);
            assertEquals(-2, p.getX());
            assertEquals(7, p.getY());
        }

        @Test void objectFormIgnoresUnknownFields() throws Exception {
            ObjectMapper m = new ObjectMapper();
            m.findAndRegisterModules();
            Position p = m.readValue("{\"x\":1,\"y\":2,\"other\":9}", Position.class);
            assertEquals(1, p.getX());
            assertEquals(2, p.getY());
        }
    }
}
