package za.co.wethinkcode.robots.server.commands;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.ServerResponseData;
import za.co.wethinkcode.robots.models.ServerResponseState;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;

public class FireCommand extends Command {

    FireCommand(String[] arguStrings, String rbtNameString) {
        super("fire", arguStrings, rbtNameString);
    }

    @Override
    public ServerResponse execute(Iworld world, BaseRobot robot) {
        if (!robot.decrementBullets()) {
            return ServerResponse.builder()
                    .result(StatusCode.ERROR)
                    .data(ServerResponseData.builder().message("OUT_OF_AMMO").build())
                    .state(stateOf(robot))
                    .build();
        }

        Trace trace = traceBullet(world, robot);

        ServerResponseData.ServerResponseDataBuilder dataB = ServerResponseData.builder()
                .distance(trace.distance);

        if (trace.victim != null) {
            int damage = Math.max(1, Iworld.bulletRange - trace.distance + 1);
            BaseRobot victim = trace.victim;
            String victimName = victim.getName();
            boolean lethal = victim.takeDamage(damage, robot.getName());
            if (lethal) {
                robot.incrementKills();
                // Push to victim BEFORE removing — so the event carries their final state.
                pushKilledEvent(victim, robot.getName(), damage);
                if (world instanceof za.co.wethinkcode.robots.server.world.RobotWorld rw) {
                    rw.removeRobot(victimName);
                }
                dataB.message("KILLED " + victimName + " (damage " + damage + ")")
                     .robot(victimName);
            } else {
                dataB.message("HIT " + victimName + " (damage " + damage + ")")
                     .robot(victimName)
                     .state(stateOf(victim));
                pushHitEvent(victim, robot.getName(), damage);
            }
        } else {
            dataB.message("Miss");
        }

        return ServerResponse.builder()
                .result(StatusCode.OK)
                .data(dataB.build())
                .state(stateOf(robot))
                .build();
    }

    private Trace traceBullet(Iworld world, BaseRobot shooter) {
        Position start = shooter.getPosition();
        Directions dir = shooter.getDirection();
        int dx = (dir == Directions.EAST) ? 1 : (dir == Directions.WEST) ? -1 : 0;
        int dy = (dir == Directions.NORTH) ? 1 : (dir == Directions.SOUTH) ? -1 : 0;

        int xLimit = (world.getWidth() - 1) / 2;
        int yLimit = (world.getHeight() - 1) / 2;

        int x = start.getX();
        int y = start.getY();
        int travelled = 0;

        for (int step = 1; step <= Iworld.bulletRange; step++) {
            x += dx;
            y += dy;

            if (x < -xLimit || x > xLimit || y < -yLimit || y > yLimit) {
                return new Trace(null, travelled);
            }

            BaseRobot hit = robotAt(world, x, y, shooter);
            if (hit != null) {
                return new Trace(hit, step);
            }

            if (world.isPositionBlocked(x, y)) {
                return new Trace(null, step);
            }

            travelled = step;
        }
        return new Trace(null, travelled);
    }

    private BaseRobot robotAt(Iworld world, int x, int y, BaseRobot self) {
        for (BaseRobot other : world.getAllRobots().values()) {
            if (other == self) continue;
            Position p = other.getPosition();
            if (p != null && p.getX() == x && p.getY() == y) return other;
        }
        return null;
    }

    private void pushHitEvent(BaseRobot victim, String shooter, int damage) {
        ServerResponse evt = ServerResponse.builder()
                .result(StatusCode.OK)
                .data(ServerResponseData.builder()
                        .message("HIT_BY " + shooter + " (damage " + damage + ")")
                        .robot(shooter)
                        .distance(damage)
                        .build())
                .state(stateOf(victim))
                .build();
        za.co.wethinkcode.robots.services.ITCService.getInstance().pushEvent(victim.getName(), evt);
    }

    private void pushKilledEvent(BaseRobot victim, String shooter, int damage) {
        ServerResponse evt = ServerResponse.builder()
                .result(StatusCode.OK)
                .data(ServerResponseData.builder()
                        .message("KILLED_BY " + shooter + " (damage " + damage + ")")
                        .robot(shooter)
                        .distance(damage)
                        .build())
                .state(stateOf(victim))
                .build();
        za.co.wethinkcode.robots.services.ITCService.getInstance().pushEvent(victim.getName(), evt);
    }

    private ServerResponseState stateOf(BaseRobot r) {
        return ServerResponseState.builder()
                .position(r.getPosition())
                .direction(r.getDirection())
                .shields(r.getShield())
                .shots(r.getShoots())
                .status(r.getOperationState())
                .build();
    }

    private record Trace(BaseRobot victim, int distance) {}
}
