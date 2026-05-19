package za.co.wethinkcode.robots.server.npc;

public final class KillerNPC {
    private KillerNPC() {}

    public static final String NAME = "Guyser_Thekiller";

    /** Time between controller ticks. */
    public static final long TICK_MS = 500L;

    /** How long after death before the NPC respawns. */
    public static final long RESPAWN_DELAY_MS = 60_000L;

    /** Player idle threshold — no successful move for this long → marks player as a target. */
    public static final long IDLE_THRESHOLD_MS = 30_000L;

    /** Consecutive BLOCKED moves before a player is marked as a target. */
    public static final int BLOCKED_THRESHOLD = 5;

    /** If no kill anywhere in the world for this long → hunt closest player. */
    public static final long PEACE_THRESHOLD_MS = 120_000L;

    /** Threat message the NPC announces on each kill. */
    public static final String THREAT = "leave this place you waste child in a man's arena";
}
