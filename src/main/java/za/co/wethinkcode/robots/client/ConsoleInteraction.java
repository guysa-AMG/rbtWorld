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
}
