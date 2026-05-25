package za.co.wethinkcode.robots.client;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseObject;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;
import za.co.wethinkcode.robots.shared.Protocol;
import za.co.wethinkcode.robots.server.world.Iworld;

import java.util.List;

/**
 * Extracted non-UI logic from RobotClient.handleGuiResponse so it can be unit tested.
 * Methods return small DTOs or decision flags; GUI side still consumes them.
 */
public final class RobotClientResponseHandler {

    private RobotClientResponseHandler() {}

    public static ServerResponse decode(String json) {
        return new Protocol().decodeResponse(json);
    }

    public static boolean isDeathMessage(String message) {
        return message != null && (message.startsWith("KILLED_BY ") || "FELL_IN_PIT".equalsIgnoreCase(message) || (message.contains("has not been launched")));
    }

    public static boolean isHitMessage(String message) {
        return message != null && message.startsWith("HIT_BY ");
    }

    public static boolean shouldExpandLook(String lastCommand) {
        return "look".equalsIgnoreCase(lastCommand);
    }

    public static boolean isFireResult(String lastCommand) {
        return "fire".equalsIgnoreCase(lastCommand);
    }

}
