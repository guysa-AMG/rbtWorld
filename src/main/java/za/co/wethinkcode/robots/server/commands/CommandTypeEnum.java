package za.co.wethinkcode.robots.server.commands;

public enum CommandTypeEnum {
    shutdown,
    off,
    quit,
    launch,
    look,
    robots,
    forward,
    dump,
    back,
    turn,
    repair,
    reload,
    fire,
    state,
    subscribe,
    unsubscribe;

    public static boolean contains(String token) {
        if (token == null) return false;
        for (CommandTypeEnum c : values()) {
            if (c.name().equalsIgnoreCase(token)) return true;
        }
        return false;
    }
}
