package persistence;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import model.Zoo;

// Represents a writer that writes JSON representation of zoo to file
public class JsonWriter {
    private static final int TAB = 4;

    private final String destination;
    private PrintWriter writer;

    // EFFECTS: constructs a writer to write to destination file
    public JsonWriter(String destination) {
        this.destination = destination;
    }

    // MODIFIES: this
    // EFFECTS: opens writer; throws FileNotFoundException if destination file cannot be opened for writing
    public void open() throws FileNotFoundException {
        writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8));
    }

    // MODIFIES: this
    // EFFECTS: writes JSON representation of zoo to file
    public void write(Zoo zoo) {
        writer.print(zoo.toJson().toString(TAB));
    }

    // MODIFIES: this
    // EFFECTS: closes writer
    public void close() {
        writer.close();
    }

    // EFFECTS: writes zoo to the destination file in one step (open, write, close)
    //          throws FileNotFoundException if destination file cannot be opened for writing
    public void save(Zoo zoo) throws FileNotFoundException {
        open();
        try {
            write(zoo);
        } finally {
            close();
        }
    }
}
