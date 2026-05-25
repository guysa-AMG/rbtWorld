package za.co.wethinkcode.robots.client;

import za.co.wethinkcode.robots.server.commands.CommandTypeEnum;

/**
 * Small utility containing non-UI parsing/normalisation logic extracted from RobotClient
 * to make it easy to unit-test without involving sockets or Swing.
 */
public final class RobotClientLogic {

    private RobotClientLogic() { }

    /**
     * Normalize a user-typed line into the canonical '<robot> <command> [args]'.
     * Returns null when the line should be rejected (e.g. command-only when not launched).
     */
    public static String normaliseCommand(String userLine, String lockedRobotName) {
        if (userLine == null) return null;
        String[] parts = userLine.trim().split("\\s+");
        if (parts.length == 0) return userLine;

        boolean firstIsCommand = CommandTypeEnum.contains(parts[0]);

        if (firstIsCommand) {
            // User typed just '<command> [args]'
            if (lockedRobotName == null) return null;
            return lockedRobotName + " " + userLine.trim();
        }

        // First token is NOT a known command — treat it as a robot name.
        if (parts.length < 2 || !CommandTypeEnum.contains(parts[1])) return null;

        if (lockedRobotName != null && !parts[0].equalsIgnoreCase(lockedRobotName)) {
            // rewrite to use the locked name
            StringBuilder rest = new StringBuilder(lockedRobotName);
            for (int i = 1; i < parts.length; i++) rest.append(' ').append(parts[i]);
            return rest.toString();
        }
        return userLine.trim();
    }
}
