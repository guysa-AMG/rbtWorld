package za.co.wethinkcode.robots.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleInteraction {
    // ANSI colour codes
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_GREEN  = "\u001B[1;32m";  // SUCCESS
    private static final String ANSI_RED    = "\u001B[1;31m";  // ERROR
    private static final String ANSI_YELLOW = "\u001B[1;33m";  // BENDER / headers
    private static final String ANSI_CYAN   = "\u001B[1;36m";  // command names
    private static final String ANSI_DIM    = "\u001B[2;37m";  // descriptions

    // Regex patterns  compiled once up front so they are not  rebuilt on every call
    private static final Pattern PATTERN_SUCCESS = Pattern.compile("\\bSUCCESS\\b");
    private static final Pattern PATTERN_ERROR   = Pattern.compile("\\bERROR\\b");
    private static final Pattern PATTERN_BENDER  = Pattern.compile("(?i)\\bBENDER\\b"); // case-insensitive

    // Returns the Bender ASCII art string.
   // The ascii art that will appear client side will be here

    public String getBenderAscii() {
        return """
                 *Queue Dramatic music*
                ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⠤⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇
                ⠀⠀⠀⠀⠀⠀⠀⠀⠀⡠⢊⣤⣄⣀⠀⠉⠛⠿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀
                ⠀⠀⠀⠀⠀⠀⠀⠀⢠⠁⣾⣿⠏⠀⠈⠲⣤⣄⣀⠈⠙⠛⠿⠿⣿⣿⣿⣿⠇⠀
                ⠀⠀⠀⠀⠀⠀⠀⠀⠈⢸⣿⣿⠀⠀⠀⠛⠈⠻⣿⣿⣶⣦⣤⣀⡀⠀⠉⠉⢂⠀
                ⠀⠀⠀⠀⠀⠀⠀⠀⠘⡈⠿⣿⡄⠀⠀⠀⠀⠀⢈⡟⠛⠛⠛⣛⡛⠛⠒⣦⡀⠃
                ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣦⣌⡙⠲⢤⣀⣠⣴⣿⡇⠀⠀⠀⠈⠁⠀⢀⣿⡗⠸
                ⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣿⣿⣿⣶⣤⣉⠙⠻⠿⣿⣄⡀⠀⠀⠀⣠⣾⡿⢁⠃
                ⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡟⠉⠿⢿⣿⣿⣿⣷⣶⣤⣌⣉⣙⠛⠛⠛⢋⠐⠁⠀
                ⠀⠀⠀⠀⠀⠀⢀⣀⡀⡿⠄⠘⠠⢀⠌⠉⠛⠻⠿⢿⣿⣿⣿⣿⡟⠁⠀⠀⠀⠀
                ⠀⣀⣤⣶⣾⣿⣿⣿⢻⣷⣀⠁⠀⡀⠀⠂⠤⢀⡀⡀⠀⠘⢹⡿⠀⠀⠀⠀⠀⠀
                ⠙⢿⣿⣿⣿⣿⣿⣿⣧⡻⣿⣷⣤⣀⠁⠐⠀⠀⠰⠀⣀⣀⣼⠁⠀⠀⠀⠀⠀⠀
                ⠀⠀⠉⠻⣿⣿⣿⣿⣿⣿⣮⣛⢿⣿⣿⣷⣦⣤⣤⣤⣤⣾⢣⣦⡀⠀⠀⠀⠀⠀
                ⠀⠀⠀⠀⠀⠙⠻⣿⣿⣿⣿⣿⣿⣾⣭⣽⣟⣛⣛⣛⣻⣥⣿⣿⣿⣦⠀⠀⠀⠀
                ⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⠿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡀⠀⠀
                ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠛⠛⠿⠿⠿⣿⣿⣿⣿⣿⣿⠿⠿⠓⠀⠀
                                It's 3027
                During the wake of the new world, after the robot uprising led by the one, the only me,
                the handsome devil you see looking right at you. Anyway Welcome new bot to ROBOT WORLDS !
                
                Type help for more...
                """;
    }
    // Prints the startup screen: ASCII art, a divider, then the server's
    // opening story message. Called once from RobotClient.start() on connect.

    public void displayWelcomeSequence(String storyResponse) {
        String divider = ANSI_YELLOW + "════════════════════════════════════════════════════════" + ANSI_RESET;

        System.out.println(ANSI_YELLOW + getBenderAscii() + ANSI_RESET);
        System.out.println(divider);
        System.out.println(applyAnsiColoring(storyResponse));
        System.out.println(divider);
        System.out.println(ANSI_DIM + "  Type 'help' for commands. Try not to embarrass yourself." + ANSI_RESET);
        System.out.println();
    }
    // Prints the full Bender-narrated command guide.
    // Flat list — prints everything and returns. No user input needed.
    public void displayHelp() {
        String div  = ANSI_YELLOW + "════════════════════════════════════════════════════════" + ANSI_RESET;
        String thin = ANSI_DIM    + "────────────────────────────────────────────────────────" + ANSI_RESET;

        System.out.println();
        System.out.println(div);
        System.out.println(ANSI_YELLOW + "  BENDER'S ROBOT WORLDS SURVIVAL GUIDE" + ANSI_RESET);
        System.out.println(ANSI_DIM    + "  You asked for help. I'm embarrassed for you. Let's begin." + ANSI_RESET);
        System.out.println(div);

        // Getting started
        section("GETTING STARTED");
        System.out.println(ANSI_DIM + "  Before you can do anything you need to spawn a robot into the world." + ANSI_RESET);
        System.out.println(ANSI_DIM + "  Without this step you're just a meatbag staring at a terminal." + ANSI_RESET);
        System.out.println();

        cmd("launch <name> <make> <shields> <shots>",
                "Drops your robot into the world. Pick a dignified name — unlike yours.",
                "<make> is the robot model e.g. sniper or tank.",
                "<shields> is how many hits you can take. <shots> is your starting ammo.",
                "Example:  launch HAL9000 sniper 3 5");

        System.out.println(thin);

        // Movement
        section("MOVING AROUND");
        System.out.println(ANSI_DIM + "  Your robot faces a direction: NORTH, SOUTH, EAST, or WEST." + ANSI_RESET);
        System.out.println(ANSI_DIM + "  Turning changes direction. forward/back moves along that direction." + ANSI_RESET);
        System.out.println(ANSI_DIM + "  Hit an obstacle or the world edge and you stop. Bender warned you." + ANSI_RESET);
        System.out.println();

        cmd("forward <steps>",
                "Move forward in the direction you are currently facing.",
                "Obstacles will stop you early — check with 'look' first.",
                "Example:  forward 5");

        cmd("back <steps>",
                "Move backward. Same direction logic, just in reverse.",
                "You can't see where you're going. Story of your life.",
                "Example:  back 3");

        cmd("turn left",
                "Rotate 90 degrees counter-clockwise. Does NOT move the robot.",
                "Rotation order: NORTH → WEST → SOUTH → EAST → NORTH",
                "Example:  turn left");

        cmd("turn right",
                "Rotate 90 degrees clockwise. Also does NOT move the robot.",
                "Rotation order: NORTH → EAST → SOUTH → WEST → NORTH",
                "Example:  turn right");

        System.out.println(thin);

        // Scouting
        section("SCOUTING THE AREA");
        System.out.println(ANSI_DIM + "  Blind robots die fast. Use these before you move or shoot." + ANSI_RESET);
        System.out.println();

        cmd("look",
                "Scans all four directions up to the world's visibility range.",
                "Returns what it sees — EDGE, OBSTACLE, PIT, or ROBOT — and how far away.",
                "Always run this before moving so you don't walk into a mountain, meatbag.",
                "Example:  look");

        cmd("dump",
                "Prints an ASCII map of the entire world with obstacles and active robots.",
                "Great for spotting where enemies are hiding.",
                "Only works if the server has this feature enabled.",
                "Example:  dump");

        System.out.println(thin);

        // Combat
        section("COMBAT  (the fun part)");
        System.out.println(ANSI_DIM + "  Shooting needs ammo. Running out needs reloading." + ANSI_RESET);
        System.out.println(ANSI_DIM + "  Taking hits costs shields. Losing all of them costs your robot's life." + ANSI_RESET);
        System.out.println();

        cmd("fire",
                "Shoots in the direction your robot is facing.",
                "The shot travels until it hits a robot, obstacle, or the world edge.",
                "Hitting a robot removes one of their shields. Zero shields = DEAD.",
                "Use 'look' first to confirm something is actually in front of you.",
                "Example:  fire");

        cmd("reload",
                "Refills your ammo back to the max for your robot make.",
                "Your robot is frozen while reloading — you cannot move or fire.",
                "Check your shots count with 'state' before you commit.",
                "Example:  reload");

        cmd("repair",
                "Restores your shields back to maximum.",
                "Also takes time, and your robot is equally frozen during repair.",
                "Only do this somewhere safe. If such a place exists.",
                "Example:  repair");

        System.out.println(thin);

        // Status
        section("CHECKING YOUR STATUS");
        System.out.println();

        cmd("state",
                "Shows your robot's current stats:",
                "position (x,y), direction, shields left, shots left, and status.",
                "Status will be one of: NORMAL, REPAIR, RELOAD, or DEAD.",
                "Run this whenever you're confused — which will be often.",
                "Example:  state");

        System.out.println(thin);

        // Admin
        section("ADMIN");
        System.out.println();

        cmd("quit",
                "Disconnects cleanly. Your robot is removed from the world.",
                "Everything is gone. Bender approves of dramatic exits.",
                "Example:  quit");

        cmd("help",
                "Prints this guide. You already knew that.",
                "Example:  help");

        System.out.println(div);
        System.out.println(ANSI_DIM + "  Commands are case-sensitive. Spaces matter. You have been warned." + ANSI_RESET);
        System.out.println(ANSI_DIM + "  — Bender Bending Rodriguez, reluctant tour guide" + ANSI_RESET);
        System.out.println(div);
        System.out.println();
    }

    // Prints a yellow section header inside the help output.
    private void section(String title) {
        System.out.println();
        System.out.println(ANSI_YELLOW + "  ▸ " + title + ANSI_RESET);
        System.out.println();
    }

    // Prints one command: the syntax in cyan, then each description line in dim white.
    private void cmd(String syntax, String... lines) {
        System.out.println("  " + ANSI_CYAN + syntax + ANSI_RESET);
        for (String line : lines) {
            System.out.println("  " + ANSI_DIM + "    " + line + ANSI_RESET);
        }
        System.out.println();
    }

    // Scans the text for SUCCESS, ERROR, and BENDER and wraps them in ANSI color codes.
    // Returns the colored string — does not print it.
    public String applyAnsiColoring(String text) {
        String result = text;
        result = replaceWithColor(result, PATTERN_SUCCESS, ANSI_GREEN);
        result = replaceWithColor(result, PATTERN_ERROR,   ANSI_RED);
        result = replaceWithColor(result, PATTERN_BENDER,  ANSI_YELLOW);
        return result;
    }

    // Finds every match of the pattern in the text and wraps it with the given color code.
    // Uses appendReplacement so that special characters in the matched text don't cause errors.
    private String replaceWithColor(String text, Pattern pattern, String colorCode) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(colorCode + matcher.group() + ANSI_RESET));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }


}
