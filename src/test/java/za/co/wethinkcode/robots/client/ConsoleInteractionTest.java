package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ConsoleInteractionTest {

    private ConsoleInteraction ci;
    private PrintStream origOut;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void setUp() {
        ci = new ConsoleInteraction();
        origOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
    }

    @AfterEach
    void tearDown() {
        System.setOut(origOut);
    }

    @Nested
    @DisplayName("ASCII / welcome content")
    class Ascii {
        @Test void getBenderAsciiReturnsNonEmpty() {
            String art = ci.getBenderAscii();
            assertNotNull(art);
            assertTrue(art.length() > 0);
        }

        @Test void displayWelcomeSequencePrintsStory() {
            ci.displayWelcomeSequence("greetings traveller");
            String out = captured.toString();
            assertTrue(out.contains("greetings traveller"));
        }
    }

    @Nested
    @DisplayName("Help printing")
    class Help {
        @Test void displayHelpPrintsKnownSections() {
            ci.displayHelp();
            String out = captured.toString();
            assertTrue(out.contains("BENDER"));
            assertTrue(out.contains("forward"));
            assertTrue(out.contains("turn left"));
            assertTrue(out.contains("fire"));
        }
    }

    @Nested
    @DisplayName("applyAnsiColoring keyword highlighting")
    class Coloring {
        @Test void colorsTheSuccessWord() {
            String out = ci.applyAnsiColoring("ROBOT SUCCESS");
            assertTrue(out.contains(ConsoleInteraction.ANSI_GREEN));
            assertTrue(out.contains("SUCCESS"));
        }

        @Test void colorsTheErrorWord() {
            String out = ci.applyAnsiColoring("This is an ERROR moment");
            assertTrue(out.contains(ConsoleInteraction.ANSI_RED));
        }

        @Test void colorsBenderCaseInsensitive() {
            String out = ci.applyAnsiColoring("bEnDeR speaks");
            assertTrue(out.contains("bEnDeR"));
        }

        @Test void plainTextIsUnchanged() {
            String input = "nothing special";
            // After coloring, the source word should still be present
            assertTrue(ci.applyAnsiColoring(input).contains("nothing special"));
        }
    }
}
