package za.co.wethinkcode.robots.client.gui;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.server.world.WorldGenerator;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldPanel extends JPanel {

    private static final int CELL = 22;
    private static final String KILLER_NPC_NAME = "Bender";
    private static final BufferedImage KILLER_IMAGE = loadImage("/images/killer_robot.jpg");
    private static final BufferedImage PLAYER_SHEET = loadImage("/images/player_robot_motions.jpeg");
    private static final java.util.Map<Directions, BufferedImage> PLAYER_FRAMES = slicePlayerFrames();

    private static BufferedImage loadImage(String path) {
        try (java.io.InputStream in = WorldPanel.class.getResourceAsStream(path)) {
            if (in == null) return null;
            return javax.imageio.ImageIO.read(in);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Slice the player sprite sheet into 4 directional frames. The sheet's top row has 7 cells:
     * 1=idle-left, 2=idle-right, 3=up/back, 4=down/front (and 3 more we ignore).
     * Crops are proportional so this still works if the sheet is later resized.
     */
    private static java.util.Map<Directions,BufferedImage> slicePlayerFrames() {
        java.util.Map<Directions, BufferedImage> out = new java.util.EnumMap<>(Directions.class);
        if (PLAYER_SHEET == null) return out;
        int w = PLAYER_SHEET.getWidth();
        int h = PLAYER_SHEET.getHeight();
        int cellW = w / 7;
        int cellH = h / 2;          // top row only
        out.put(Directions.WEST,  PLAYER_SHEET.getSubimage(0 * cellW, 0, cellW, cellH));
        out.put(Directions.EAST,  PLAYER_SHEET.getSubimage(1 * cellW, 0, cellW, cellH));
        out.put(Directions.NORTH, PLAYER_SHEET.getSubimage(2 * cellW, 0, cellW, cellH));
        out.put(Directions.SOUTH, PLAYER_SHEET.getSubimage(3 * cellW, 0, cellW, cellH));
        return out;
    }

    private final int worldWidth;
    private final int worldHeight;
    private final List<Obstacle> obstacles;
    private final Map<String, RobotMarker> robots = new ConcurrentHashMap<>();
    private final java.util.Set<Position> pickups = ConcurrentHashMap.newKeySet();
    private String selfName;
    private volatile BulletFx bullet;
    private volatile Hud hud = new Hud(0, 0, 0, 0, 0);
    private volatile Position selfPosition;     // null until launched
    private volatile boolean lookExpanded = false; // true between a successful look and the next move

    public WorldPanel() {
        RobotWorld template = WorldGenerator.build();
        this.worldWidth = template.getWidth();
        this.worldHeight = template.getHeight();
        this.obstacles = new ArrayList<>();
        for (Object o : template.getObstacles()) {
            if (o instanceof Obstacle obs) obstacles.add(obs);
        }
        setBackground(new Color(20, 20, 25));
        setPreferredSize(new Dimension(worldWidth * CELL + 2, worldHeight * CELL + 2));
        
    }
  
    public void setSelfName(String name) {
        this.selfName = name;
        repaint();
    }

    public void updateRobot(String name, Position pos, Directions dir) {
        if (name == null || pos == null) return;
        robots.put(name, new RobotMarker(pos.getX(), pos.getY(), dir));
        repaint();
    }

    public void removeRobot(String name) {
        robots.remove(name);
        repaint();
    }

    public void setPickups(List<Position> list) {
        pickups.clear();
        if (list != null) {
            for (Position p : list) pickups.add(new Position(p.getX(), p.getY()));
        }
        repaint();
    }

    public void setHud(int lives, int shots, int magMax, int shield, int kills) {
        this.hud = new Hud(lives, shots, magMax, shield, kills);
        repaint();
    }

    /** Centre and radius of the lit-up area. {@code selfPos == null} disables fog. */
    public void setFog(Position selfPos, boolean lookExpanded) {
        this.selfPosition = selfPos == null ? null : new Position(selfPos.getX(), selfPos.getY());
        this.lookExpanded = lookExpanded;
        repaint();
    }

    private int visibilityRadius() {
        return lookExpanded
                ? 2 * za.co.wethinkcode.robots.server.world.Iworld.bulletRange
                : za.co.wethinkcode.robots.server.world.Iworld.bulletRange;
    }

    private boolean inSight(int wx, int wy) {
        if (selfPosition == null) return true;
        int dx = Math.abs(wx - selfPosition.getX());
        int dy = Math.abs(wy - selfPosition.getY());
        return Math.max(dx, dy) <= visibilityRadius();
    }

    /**
     * Replace the entire robot list with the supplied snapshot. Robots not present in
     * the snapshot are removed (so killed/disconnected robots disappear).
     */
    public void setAllRobots(java.util.List<za.co.wethinkcode.robots.models.transitmodels.ServerResponseRobot> snapshot) {
        if (snapshot == null) return;
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (var r : snapshot) {
            if (r.getPosition() == null || r.getName() == null) continue;
            robots.put(r.getName(),
                    new RobotMarker(r.getPosition().getX(), r.getPosition().getY(), r.getDirection()));
            seen.add(r.getName());
        }
        robots.keySet().retainAll(seen);
        repaint();
    }

    /**
     * Flash a bullet trail from the shooter for {@code distance} cells in {@code dir},
     * then fade out. Called from the EDT.
     */
    public void flashBullet(String shooter, Directions dir, int distance, boolean hit) {
        RobotMarker m = robots.get(shooter);
        if (m == null || dir == null || distance <= 0) return;
        long start = System.currentTimeMillis();
        bullet = new BulletFx(m.x, m.y, dir, distance, hit, start);
        repaint();

        Timer t = new Timer(60, null);
        t.addActionListener(ev -> {
            BulletFx b = bullet;
            if (b == null) { t.stop(); return; }
            long elapsed = System.currentTimeMillis() - b.start;
            if (elapsed >= 700) {
                bullet = null;
                t.stop();
            }
            repaint();
        });
        t.setRepeats(true);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawObstacles(g2);
        drawPickups(g2);
        drawPlayerRobots(g2);   // self + other players (others filtered by visibility)
        drawFog(g2);            // hides everything outside the visible square
        drawNpc(g2);            // NPC always visible — drawn on top of fog
        drawBullet(g2);
        drawHud(g2);

        g2.dispose();
    }

    /** Heavy black mask covering everything outside the visibility square. */
    private void drawFog(Graphics2D g2) {
        if (selfPosition == null) return; // not launched yet — show full map
        int radius = visibilityRadius();
        int[] c = worldToScreen(selfPosition.getX(), selfPosition.getY());
        int side = (2 * radius + 1) * CELL;
        int x = c[0] - side / 2;
        int y = c[1] - side / 2;

        java.awt.geom.Area mask = new java.awt.geom.Area(new java.awt.Rectangle(0, 0, getWidth(), getHeight()));
        mask.subtract(new java.awt.geom.Area(new java.awt.Rectangle(x, y, side, side)));
        g2.setColor(new Color(0, 0, 0, 235));
        g2.fill(mask);

        // Subtle red border on the visible square so the player can see where the fog edge is.
        g2.setColor(new Color(120, 30, 30, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x, y, side, side);
    }

    private void drawPickups(Graphics2D g2) {
        long t = System.currentTimeMillis();
        for (Position p : pickups) {
            int[] s = worldToScreen(p.getX(), p.getY());
            // Pulse the ammo box so it stands out
            float pulse = (float) (0.6 + 0.4 * Math.sin(t / 250.0));
            int alpha = (int) (170 + 60 * pulse);
            int sz = CELL - 4;
            g2.setColor(new Color(70, 50, 20));
            g2.fillRect(s[0] - sz / 2, s[1] - sz / 2, sz, sz);
            g2.setColor(new Color(255, 215, 80, alpha));
            g2.fillRect(s[0] - sz / 2 + 1, s[1] - sz / 2 + 1, sz - 2, sz - 2);
            g2.setColor(Color.BLACK);
            g2.setFont(getFont().deriveFont(java.awt.Font.BOLD, 13f));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            String label = "A";
            g2.drawString(label, s[0] - fm.stringWidth(label) / 2, s[1] + fm.getAscent() / 2 - 1);
        }
    }

    private void drawHud(Graphics2D g2) {
        Hud h = this.hud;
        String left = String.format("Lives: %d   Bullets: %d/%d   Shield: %d   Kills: %d",
                h.lives, h.shots, h.magMax, h.shield, h.kills);
        java.awt.Font f = getFont().deriveFont(java.awt.Font.BOLD, 15f);
        g2.setFont(f);
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int padX = 10, padY = 6;
        int w = fm.stringWidth(left) + padX * 2;
        int hh = fm.getHeight() + padY;
        // Background pill
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(6, 6, w, hh, 10, 10);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(6, 6, w, hh, 10, 10);
        // Text
        g2.setColor(new Color(240, 240, 240));
        g2.drawString(left, 6 + padX, 6 + padY / 2 + fm.getAscent());
    }

    private void drawBullet(Graphics2D g2) {
        BulletFx b = bullet;
        if (b == null) return;
        long elapsed = System.currentTimeMillis() - b.start;
        float life = Math.min(1f, elapsed / 700f);
        float alpha = Math.max(0f, 1f - life);

        int dx = (b.dir == Directions.EAST) ? 1 : (b.dir == Directions.WEST) ? -1 : 0;
        int dy = (b.dir == Directions.NORTH) ? 1 : (b.dir == Directions.SOUTH) ? -1 : 0;

        int[] from = worldToScreen(b.fromX, b.fromY);
        int[] to = worldToScreen(b.fromX + dx * b.distance, b.fromY + dy * b.distance);

        // Beam
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(255, 220, 80, (int) (alpha * 220)));
        g2.drawLine(from[0], from[1], to[0], to[1]);
        // Inner hot line
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(255, 255, 200, (int) (alpha * 255)));
        g2.drawLine(from[0], from[1], to[0], to[1]);

        // Impact flash
        int r = CELL - 2;
        int shrink = (int) (life * (r / 2));
        int rad = Math.max(2, r - shrink);
        Color core = b.hit
                ? new Color(255, 80, 40, (int) (alpha * 240))
                : new Color(255, 200, 80, (int) (alpha * 200));
        g2.setColor(core);
        g2.fillOval(to[0] - rad / 2, to[1] - rad / 2, rad, rad);
        g2.setColor(new Color(255, 255, 255, (int) (alpha * 220)));
        int spark = Math.max(1, rad / 3);
        g2.fillOval(to[0] - spark / 2, to[1] - spark / 2, spark, spark);
        
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(40, 40, 50));
        for (int gx = 0; gx <= worldWidth; gx++) {
            int px = gx * CELL;
            g2.drawLine(px, 0, px, worldHeight * CELL);
        }
        for (int gy = 0; gy <= worldHeight; gy++) {
            int py = gy * CELL;
            g2.drawLine(0, py, worldWidth * CELL, py);
        }
        // Centre cross-hairs
        g2.setColor(new Color(70, 70, 90));
        int cx = (worldWidth / 2) * CELL + CELL / 2;
        int cy = (worldHeight / 2) * CELL + CELL / 2;
        g2.drawLine(0, cy, worldWidth * CELL, cy);
        g2.drawLine(cx, 0, cx, worldHeight * CELL);
    }

    private void drawObstacles(Graphics2D g2) {
        for (Obstacle obs : obstacles) {
            String type = obs.getType();
            // Obstacle exposes only type + isAt() publicly — rebuild the rect by scanning cells.
            for (int wx = -worldWidth / 2; wx <= worldWidth / 2; wx++) {
                for (int wy = -worldHeight / 2; wy <= worldHeight / 2; wy++) {
                    if (obs.isAt(wx, wy)) {
                        paintCell(g2, wx, wy, colorFor(type));
                    }
                }
            }
        }
    }

    /**
     * Draw self and other players (NOT the killer NPC). Other players are skipped unless
     * the player has used {@code look} AND they're inside the visibility radius.
     */
    private void drawPlayerRobots(Graphics2D g2) {
        for (Map.Entry<String, RobotMarker> e : robots.entrySet()) {
            String name = e.getKey();
            if (KILLER_NPC_NAME.equals(name)) continue;

            RobotMarker m = e.getValue();
            boolean isSelf = name.equalsIgnoreCase(selfName);

            // Other players: hidden unless look-expanded AND in sight
            if (!isSelf) {
                if (!lookExpanded) continue;
                if (!inSight(m.x, m.y)) continue;
            }

            int[] screen = worldToScreen(m.x, m.y);
            Color ring = isSelf ? new Color(255, 215, 0) : new Color(0, 200, 255);
            Directions dir = m.dir == null ? Directions.SOUTH : m.dir;
            java.awt.image.BufferedImage frame = PLAYER_FRAMES.get(dir);
            int size = (int) (CELL * 1.7);
            int half = size / 2;

            if (frame != null) {
                g2.drawImage(frame, screen[0] - half, screen[1] - half, size, size, null);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(ring);
                g2.drawRect(screen[0] - half, screen[1] - half, size, size);
            } else {
                Path2D.Double arrow = arrowFor(screen[0], screen[1], dir);
                g2.setColor(ring);
                g2.fill(arrow);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(arrow);
            }

            drawDirectionChip(g2, screen[0], screen[1], half, dir, ring);

            g2.setColor(isSelf ? new Color(255, 235, 130) : Color.WHITE);
            g2.setFont(getFont().deriveFont(java.awt.Font.BOLD, 12f));
            g2.drawString(name, screen[0] - (name.length() * 3), screen[1] - half - 4);
        }
    }

    /** The killer NPC is drawn AFTER the fog so it's always visible. */
    private void drawNpc(Graphics2D g2) {
        RobotMarker npc = robots.get(KILLER_NPC_NAME);
        if (npc == null) return;
        int[] s = worldToScreen(npc.x, npc.y);
        drawKillerNpc(g2, s[0], s[1], npc.dir, KILLER_NPC_NAME);
    }

    /** A small filled triangle on the outside of the player's box pointing in the facing direction. */
    private void drawDirectionChip(Graphics2D g2, int cx, int cy, int half, Directions dir, Color color) {
        int tip = 6;     // how far outside the box the tip pokes
        int wide = 5;    // half-width of the chip base
        int gap = 1;     // small gap from the box edge so it doesn't overlap the ring
        Path2D.Double chip = new Path2D.Double();
        switch (dir) {
            case NORTH -> {
                int baseY = cy - half - gap;
                chip.moveTo(cx, baseY - tip);
                chip.lineTo(cx - wide, baseY);
                chip.lineTo(cx + wide, baseY);
            }
            case SOUTH -> {
                int baseY = cy + half + gap;
                chip.moveTo(cx, baseY + tip);
                chip.lineTo(cx - wide, baseY);
                chip.lineTo(cx + wide, baseY);
            }
            case EAST -> {
                int baseX = cx + half + gap;
                chip.moveTo(baseX + tip, cy);
                chip.lineTo(baseX, cy - wide);
                chip.lineTo(baseX, cy + wide);
            }
            case WEST -> {
                int baseX = cx - half - gap;
                chip.moveTo(baseX - tip, cy);
                chip.lineTo(baseX, cy - wide);
                chip.lineTo(baseX, cy + wide);
            }
        }
        chip.closePath();
        g2.setColor(color);
        g2.fill(chip);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(chip);
    }

    private void drawKillerNpc(Graphics2D g2, int cx, int cy, Directions dir, String name) {
        int size = (int) (CELL * 1.6); // big enough to spot, still fits in the grid

        if (KILLER_IMAGE != null) {
            // Rotate the image to match the NPC's facing direction.
            double angle = switch (dir == null ? Directions.NORTH : dir) {
                case NORTH -> 0;
                case EAST  -> Math.PI / 2;
                case SOUTH -> Math.PI;
                case WEST  -> -Math.PI / 2;
            };
            java.awt.geom.AffineTransform old = g2.getTransform();
            g2.rotate(angle, cx, cy);
            g2.drawImage(KILLER_IMAGE, cx - size / 2, cy - size / 2, size, size, null);
            g2.setTransform(old);
        } else {
            // Fallback if the image didn't load: red square so the NPC is still visible.
            g2.setColor(new Color(220, 30, 30));
            g2.fillRect(cx - size / 2, cy - size / 2, size, size);
        }

        // Red threat ring around the NPC
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(255, 60, 60));
        g2.drawRect(cx - size / 2, cy - size / 2, size, size);

        // Name label in red so players can identify it quickly
        g2.setColor(new Color(255, 90, 90));
        g2.setFont(getFont().deriveFont(java.awt.Font.BOLD, 11f));
        g2.drawString(name, cx - (name.length() * 3), cy - size / 2 - 3);
    }

    private Path2D.Double arrowFor(int cx, int cy, Directions dir) {
        int r = CELL / 2 - 2;
        Path2D.Double p = new Path2D.Double();
        if (dir == null) dir = Directions.NORTH;
        switch (dir) {
            case NORTH -> { p.moveTo(cx, cy - r); p.lineTo(cx - r, cy + r); p.lineTo(cx + r, cy + r); }
            case SOUTH -> { p.moveTo(cx, cy + r); p.lineTo(cx - r, cy - r); p.lineTo(cx + r, cy - r); }
            case EAST  -> { p.moveTo(cx + r, cy); p.lineTo(cx - r, cy - r); p.lineTo(cx - r, cy + r); }
            case WEST  -> { p.moveTo(cx - r, cy); p.lineTo(cx + r, cy - r); p.lineTo(cx + r, cy + r); }
        }
        p.closePath();
        return p;
    }

    private void paintCell(Graphics2D g2, int worldX, int worldY, Color c) {
        int[] s = worldToScreen(worldX, worldY);
        g2.setColor(c);
        g2.fillRect(s[0] - CELL / 2 + 1, s[1] - CELL / 2 + 1, CELL - 1, CELL - 1);
    }

    // Convert world coords (centre origin, +Y up) to pixel coords (+Y down)
    private int[] worldToScreen(int wx, int wy) {
        int sx = (wx + worldWidth / 2) * CELL + CELL / 2;
        int sy = (worldHeight / 2 - wy) * CELL + CELL / 2;
        return new int[] { sx, sy };
    }

    private static Color colorFor(String type) {
        return switch (type) {
            case "MOUNTAIN" -> new Color(150, 100, 60);
            case "LAKE"     -> new Color(60, 130, 220);
            case "TREE"     -> new Color(40, 160, 70);
            case "WALL"     -> new Color(200, 200, 200);
            case "ROCK"     -> new Color(130, 130, 130);
            case "PIT"      -> new Color(220, 40, 40);
            default          -> Color.MAGENTA;
        };
    }

    private record RobotMarker(int x, int y, Directions dir) {}

    private record BulletFx(int fromX, int fromY, Directions dir, int distance, boolean hit, long start) {}

    private record Hud(int lives, int shots, int magMax, int shield, int kills) {}
}
