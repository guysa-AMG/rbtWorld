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
                
                [too tired to type the story rn but it basically goes here]
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


}
