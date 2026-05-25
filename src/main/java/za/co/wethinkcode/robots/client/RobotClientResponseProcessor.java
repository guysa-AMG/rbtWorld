package za.co.wethinkcode.robots.client;

import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseState;

public final class RobotClientResponseProcessor {
    private RobotClientResponseProcessor() {}

    public static String formatLogLine(ServerResponse response) {
        if (response == null) return "";
        String result = response.getResult() == null ? "UNKNOWN" : response.getResult().toString();
        var data = response.getData();
        String message = (data == null || data.getMessage() == null) ? "" : data.getMessage();
        if (message.startsWith("[Guyser_Thekiller]")) return ">>> " + message + " <<<";
        return "<< [" + result + "] " + message;
    }

    public static boolean shouldExpandLook(String lastCommand) {
        return "look".equalsIgnoreCase(lastCommand);
    }

    public static boolean shouldFlashBullet(String lastCommand, String robotName, ServerResponseState oldState, ServerResponseData data) {
        return "fire".equalsIgnoreCase(lastCommand) && robotName != null && oldState != null && data != null;
    }

    public static int computeFlashDistance(ServerResponseData data) {
        if (data == null) return -1;
        int dist = Math.max(1, data.getDistance());
        return dist;
    }

    public static boolean isHitMessage(String message) {
        return message != null && message.startsWith("HIT_BY ");
    }

    public static boolean isKilledMessage(String message) {
        return message != null && message.startsWith("KILLED_BY ");
    }

    public static boolean isFellInPit(String message) { return "FELL_IN_PIT".equalsIgnoreCase(message); }
}
