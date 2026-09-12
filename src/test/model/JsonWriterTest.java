package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import persistence.JsonReader;
import persistence.JsonWriter;

public class JsonWriterTest {

    @Test
    void testWriterInvalidFile() {
        try {
            JsonWriter writer = new JsonWriter("./data/my\0illegal:fileName.json");
            writer.open();
            fail("IOException was expected");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    void testEmptyZoo() {
        try {
            Zoo zoo = new Zoo("Angus");
            JsonWriter writer = new JsonWriter("./data/TestEmptyZoo.json");
            writer.open();
            writer.write(zoo);
            writer.close();
    
            JsonReader reader = new JsonReader("./data/TestEmptyZoo.json");
            zoo = reader.read();
            assertEquals("Angus", zoo.getZooName());
            assertEquals(0, zoo.getNumEnclosures());
            assertEquals(1000, zoo.getZooMoney());
        } catch (IOException e) {
            fail("Exceptions should not have been thrown here");
        }
    }

    @Test
    void testGeneralZoo() {
        try {
            Zoo zoo = new Zoo("Angus");
            zoo.addAnimal("Orca", "Tuki", 16);
            zoo.addEnclosure("Waiting Room");
            zoo.addEnclosure("Water");
            zoo.assignAnimalToEnclosure("Tuki", "Water");
            JsonWriter writer = new JsonWriter("./data/TestGeneralZoo.json");
            writer.open();
            writer.write(zoo);
            writer.close();
    
            JsonReader reader = new JsonReader("./data/TestGeneralZoo.json");
            zoo = reader.read();
            ArrayList<Animal> animals = zoo.getAllAnimals();
            ArrayList<Enclosure> enclosures = zoo.getAllEnclosure();
            assertEquals("Angus", zoo.getZooName());
            assertEquals(2, zoo.getNumEnclosures());
            assertEquals(1000, zoo.getZooMoney());
            assertEquals("Orca", animals.get(0).getSpecie());
            assertEquals("Tuki", animals.get(0).getName());
            assertEquals(16, animals.get(0).getAge());

            assertEquals("Water", enclosures.get(1).getName());
            assertEquals(10, enclosures.get(1).getCleaness());
            assertEquals(1, enclosures.get(1).getNumAnimal());

        } catch (IOException e) {
            // fail("Exceptions should not have been thrown here");
        }
    }
}
