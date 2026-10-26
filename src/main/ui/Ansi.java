package ui;

// ANSI colour helpers for the console app.
// Colours are turned off when output isn't an interactive terminal or the NO_COLOR environment variable is set.
final class Ansi {

    static final String RESET = "\u001B[0m";
    static final String BOLD = "\u001B[1m";
    static final String DIM = "\u001B[2m";
    static final String RED = "\u001B[31m";
    static final String GREEN = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE = "\u001B[34m";
    static final String MAGENTA = "\u001B[35m";
    static final String CYAN = "\u001B[36m";

    private static final boolean ENABLED = System.console() != null && System.getenv("NO_COLOR") == null;

    private Ansi() {
    }

    // EFFECTS: returns text wrapped in the given styles (or text unchanged if colours are off)
    static String paint(String text, String... styles) {
        if (!ENABLED || styles.length == 0) {
            return text;
        }
        return String.join("", styles) + text + RESET;
    }
}
