package ui;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

// Reads validated input from the user and prints styled messages, so the app never crashes on bad input
final class ConsoleIO {

    // Thrown when standard input is closed (e.g. Ctrl+Z / Ctrl+D), so the app can exit cleanly
    static final class InputClosedException extends RuntimeException {
    }

    private final Scanner scanner;

    ConsoleIO(InputStream in) {
        this.scanner = new Scanner(in);
    }

    // EFFECTS: prints prompt and returns the trimmed line the user types
    String readLine(String prompt) {
        System.out.print(Ansi.paint("  " + prompt + " ", Ansi.CYAN) + Ansi.paint("> ", Ansi.BOLD));
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    // EFFECTS: keeps prompting until the user types something non-blank, then returns it
    String readNonEmpty(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            warn("Please type something first.");
        }
    }

    // EFFECTS: keeps prompting until the user types a whole number in [min, max], then returns it
    int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the warning below
            }
            warn("Please enter a number from " + min + " to " + max + ".");
        }
    }

    // EFFECTS: asks a yes/no question and returns true for yes
    boolean confirm(String question) {
        while (true) {
            String answer = readLine(question + " (y/n)").toLowerCase();
            if (answer.startsWith("y")) {
                return true;
            } else if (answer.startsWith("n")) {
                return false;
            }
            warn("Just 'y' or 'n', please.");
        }
    }

    // EFFECTS: shows options as a numbered list and returns the one the user picks by number,
    //          or null if they leave it blank to cancel or there is nothing to choose from
    <T> T choose(String title, List<T> options, Function<T, String> label) {
        if (options.isEmpty()) {
            warn("There's nothing to choose from yet.");
            return null;
        }
        say(Ansi.paint(title, Ansi.BOLD));
        for (int i = 0; i < options.size(); i++) {
            say("  " + Ansi.paint(String.format("%2d)", i + 1), Ansi.CYAN) + " " + label.apply(options.get(i)));
        }
        while (true) {
            String line = readLine("Pick a number (blank to cancel)");
            if (line.isEmpty()) {
                return null;
            }
            try {
                int index = Integer.parseInt(line) - 1;
                if (index >= 0 && index < options.size()) {
                    return options.get(index);
                }
            } catch (NumberFormatException e) {
                // fall through to the warning below
            }
            warn("That's not one of the options.");
        }
    }

    // EFFECTS: waits for the user to press Enter
    void pause() {
        readLine(Ansi.paint("Press Enter to continue", Ansi.DIM));
    }

    void say(String message) {
        System.out.println("  " + message);
    }

    void blank() {
        System.out.println();
    }

    void success(String message) {
        say(Ansi.paint("+ ", Ansi.GREEN, Ansi.BOLD) + Ansi.paint(message, Ansi.GREEN));
    }

    void warn(String message) {
        say(Ansi.paint("! ", Ansi.YELLOW, Ansi.BOLD) + Ansi.paint(message, Ansi.YELLOW));
    }

    void error(String message) {
        say(Ansi.paint("x ", Ansi.RED, Ansi.BOLD) + Ansi.paint(message, Ansi.RED));
    }
}
