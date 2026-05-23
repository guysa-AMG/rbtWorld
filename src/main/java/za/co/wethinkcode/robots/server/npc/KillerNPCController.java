package za.co.wethinkcode.robots.server.npc;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.services.ITCService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side ticker that drives the Guyser_Thekiller NPC.
 *
 * Behaviour:
 *   - If NPC is not in the world AND respawn delay has elapsed → spawn at a safe random cell.
 *   - Otherwise, pick a target (idle player, obstacle-spammer, or closest after peace timer).
 *   - Per tick, the NPC takes ONE action: rotate towards target, fire if in range+LOS, or step forward.
 *   - On NPC kill of a player → broadcast threat message.
 */
public class KillerNPCController implements Runnable {

    private final Logger log = LoggerFactory.getLogger(KillerNPCController.class);
    private final RobotWorld world;
    private volatile boolean running = true;

    /** Wall-clock time at which the NPC may next spawn. 0 = spawn ASAP. */
    private long respawnAt = 0;

    /** Wall-clock time of the most recent kill anywhere in the arena (used for peace timer). */
    private volatile long lastKillTime = System.currentTimeMillis();

    public KillerNPCController(RobotWorld world) {
        this.world = world;
    }

    public void stop() { this.running = false; }

    /** Public hook so command handlers can tell us a kill happened (resets peace timer). */
    public void recordKill() { this.lastKillTime = System.currentTimeMillis(); }

    @Override
    public void run() {
        log.info("KillerNPCController started");
        while (running) {
            try {
                Thread.sleep(KillerNPC.TICK_MS);
                tick();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("KillerNPC tick failed", e);
            }
        }
        log.info("KillerNPCController stopped");
    }

    private void tick() {
        long now = System.currentTimeMillis();
        BaseRobot npc = world.getAllRobots().get(KillerNPC.NAME);
        if (npc == null) {
            if (now >= respawnAt) {
                spawnNPC();
            }
            return;
        }

        BaseRobot target = pickTarget(now);
        if (target == null) {
            // Idle wander — gentle to keep him on screen
            wander(npc);
            return;
        }

        actAgainst(npc, target);
    }

    private void spawnNPC() {
        boolean ok = world.addRobot(KillerNPC.NAME);
        if (!ok) return;
        log.info("Guyser_Thekiller has appeared at " + world.getAllRobots().get(KillerNPC.NAME).getPosition());
        announceAppearance();
    }

    private BaseRobot pickTarget(long now) {
        BaseRobot blockSpammer = null;
        BaseRobot idleVictim = null;
        long longestIdle = 0;

        for (BaseRobot r : world.getAllRobots().values()) {
            if (KillerNPC.NAME.equals(r.getName())) continue;
            if (r.getPosition() == null) continue;

            if (r.getBlockedCount() >= KillerNPC.BLOCKED_THRESHOLD) {
                if (blockSpammer == null || r.getBlockedCount() > blockSpammer.getBlockedCount()) {
                    blockSpammer = r;
                }
            }

            long idleFor = now - r.getLastMoveTimestamp();
            if (idleFor >= KillerNPC.IDLE_THRESHOLD_MS && idleFor > longestIdle) {
                idleVictim = r;
                longestIdle = idleFor;
            }
        }

        if (blockSpammer != null) return blockSpammer;
        if (idleVictim != null) return idleVictim;

        // Peace too long → pick closest player to NPC
        if (now - lastKillTime >= KillerNPC.PEACE_THRESHOLD_MS) {
            BaseRobot npc = world.getAllRobots().get(KillerNPC.NAME);
            if (npc == null) return null;
            return closestPlayerTo(npc);
        }
        return null;
    }

    private BaseRobot closestPlayerTo(BaseRobot npc) {
        BaseRobot best = null;
        int bestDist = Integer.MAX_VALUE;
        for (BaseRobot r : world.getAllRobots().values()) {
            if (KillerNPC.NAME.equals(r.getName())) continue;
            if (r.getPosition() == null) continue;
            int d = chebyshev(npc.getPosition(), r.getPosition());
            if (d < bestDist) { bestDist = d; best = r; }
        }
        return best;
    }

    private void actAgainst(BaseRobot npc, BaseRobot target) {
        Position np = npc.getPosition();
        Position tp = target.getPosition();
        int dx = tp.getX() - np.getX();
        int dy = tp.getY() - np.getY();

        // If aligned in one axis → consider firing
        if (dx == 0 || dy == 0) {
            Directions wanted = directionFromDelta(dx, dy);
            if (wanted != null && npc.getDirection() != wanted) {
                rotateTowards(npc, wanted);
                return;
            }
            if (wanted != null && Math.abs(dx) + Math.abs(dy) <= Iworld.bulletRange) {
                fire(npc, target);
                return;
            }
        }

        // Otherwise, take a step toward target along the dominant axis
        Directions chase = (Math.abs(dx) >= Math.abs(dy))
                ? (dx > 0 ? Directions.EAST : Directions.WEST)
                : (dy > 0 ? Directions.NORTH : Directions.SOUTH);

        if (npc.getDirection() != chase) {
            rotateTowards(npc, chase);
        } else {
            world.moveRobot(KillerNPC.NAME, 1);
        }
    }

    private void wander(BaseRobot npc) {
        // Just nudge forward one cell; if blocked, rotate
        boolean moved = world.moveRobot(KillerNPC.NAME, 1);
        if (!moved) {
            rotateTowards(npc, rightOf(npc.getDirection()));
        }
    }

    private void fire(BaseRobot npc, BaseRobot target) {
        if (npc.getShoots() <= 0) {
            npc.refillAmmo();
            return;
        }
        npc.decrementBullets();
        int dist = chebyshev(npc.getPosition(), target.getPosition());
        int damage = Math.max(1, Iworld.bulletRange - dist + 1);
        boolean lethal = target.takeDamage(damage, KillerNPC.NAME);
        if (lethal) {
            npc.incrementKills();
            this.lastKillTime = System.currentTimeMillis();
            String victim = target.getName();
            world.removeRobot(victim);

            // Tell the victim they were eliminated by the NPC
            ServerResponse killEvent = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder()
                            .message("KILLED_BY " + KillerNPC.NAME + " (damage " + damage + ")")
                            .robot(KillerNPC.NAME)
                            .distance(damage)
                            .build())
                    .build();
            ITCService.getInstance().pushEvent(victim, killEvent);

            // Broadcast threat
            ServerResponse broadcast = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder()
                            .message("[" + KillerNPC.NAME + "] " + KillerNPC.THREAT)
                            .robot(KillerNPC.NAME)
                            .build())
                    .build();
            ITCService.getInstance().broadcastEvent(broadcast);
        } else {
            // Non-lethal hit — push HIT_BY to victim
            ServerResponse hitEvent = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder()
                            .message("HIT_BY " + KillerNPC.NAME + " (damage " + damage + ")")
                            .robot(KillerNPC.NAME)
                            .distance(damage)
                            .build())
                    .build();
            ITCService.getInstance().pushEvent(target.getName(), hitEvent);
        }
    }

    private void announceAppearance() {
        ServerResponse evt = ServerResponse.builder()
                .result(StatusCode.OK)
                .data(ServerResponseData.builder()
                        .message("[" + KillerNPC.NAME + "] I have arrived. Mind your steps.")
                        .robot(KillerNPC.NAME)
                        .build())
                .build();
        ITCService.getInstance().broadcastEvent(evt);
    }

    /** Called by FireCommand when a player kills the NPC. */
    public void onNPCKilled(String byPlayer) {
        this.respawnAt = System.currentTimeMillis() + KillerNPC.RESPAWN_DELAY_MS;
        ServerResponse evt = ServerResponse.builder()
                .result(StatusCode.OK)
                .data(ServerResponseData.builder()
                        .message("[" + KillerNPC.NAME + "] You got me… for now. (" + byPlayer + ")")
                        .robot(KillerNPC.NAME)
                        .build())
                .build();
        ITCService.getInstance().broadcastEvent(evt);
    }

    private void rotateTowards(BaseRobot npc, Directions wanted) {
        Directions cur = npc.getDirection();
        // Choose shortest rotation
        if (wanted == cur) return;
        if (wanted == rightOf(cur)) npc.turnRight();
        else npc.turnLeft();
    }

    private static int chebyshev(Position a, Position b) {
        return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
    }

    private static Directions directionFromDelta(int dx, int dy) {
        if (dx == 0 && dy > 0) return Directions.NORTH;
        if (dx == 0 && dy < 0) return Directions.SOUTH;
        if (dy == 0 && dx > 0) return Directions.EAST;
        if (dy == 0 && dx < 0) return Directions.WEST;
        return null;
    }

    private static Directions rightOf(Directions d) {
        return switch (d) {
            case NORTH -> Directions.EAST;
            case EAST -> Directions.SOUTH;
            case SOUTH -> Directions.WEST;
            case WEST -> Directions.NORTH;
        };
    }
}
