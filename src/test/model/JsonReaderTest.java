package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import persistence.JsonReader;

public class JsonReaderTest {


    @Test
    void testReadNonExistentFile() {
        JsonReader reader = new JsonReader("./data/non_existent_file.json");
        try {
            Zoo zoo = reader.read();
            fail("expected IOException");
        } catch (IOException e) {
            
        }
    }

    @Test
    void testReadEmptyZoo() {
        JsonReader reader = new JsonReader("./data/TestEmptyZoo.json");
        try {
            Zoo zoo = reader.read();
            assertEquals("Angus", zoo.getZooName());
            assertEquals(0, zoo.getNumAnimals());
            assertEquals(0, zoo.getNumEnclosures());
            assertEquals(1000, zoo.getZooMoney());
        } catch (IOException e) {
            fail("Couldn't read from file");
        }
    }

    @Test
    void testZooOneAnimal() {
        JsonReader reader = new JsonReader("./data/testZooOneAnimal.json");
        try {
            Zoo zoo = reader.read();
            assertEquals("Angus", zoo.getZooName());
            assertEquals(1, zoo.getNumAnimals());
            assertEquals(1, zoo.getNumEnclosures());
            assertEquals(1000, zoo.getZooMoney());
        } catch (IOException e) {
            fail("Coudn't read from file");
        }
    }

    @Test
    void testMultipleAnimalZoo() {
        JsonReader reader = new JsonReader("./data/TestMultipleAnimalsZoo.json");
        try {
            Zoo zoo = reader.read();
            assertEquals("Angus", zoo.getZooName());
            assertEquals(3, zoo.getNumAnimals());
            assertEquals(1, zoo.getNumEnclosures());
            assertEquals(1000, zoo.getZooMoney());
        } catch (IOException e) {
            fail("Coudn't read from file");
        }
    }

    @Test
    void testMultipleAnimalsAndEnclosureZoo() {
        JsonReader reader = new JsonReader("./data/TestMultipleAnimalsAndEnclosure.json");
        try {
            Zoo zoo = reader.read();
            assertEquals("Angus", zoo.getZooName());
            assertEquals(3, zoo.getNumAnimals());
            assertEquals(3, zoo.getNumEnclosures());
            assertEquals(1000, zoo.getZooMoney());
        } catch (IOException e) {
            fail("Coudn't read from file");
        }
    }

    @Test
    void testMultipleAnimalsAndEnclosureWithStatusZoo() {
        JsonReader reader = new JsonReader("./data/TestZooWithDifferentStatus.json");
        try {
            Zoo zoo = reader.read();
            assertEquals(1020, zoo.getZooMoney());

            ArrayList<Animal> animals = zoo.getAllAnimals();
            ArrayList<Enclosure> enclosures = zoo.getAllEnclosure();
            assertEquals(115, animals.get(0).getHappinness());
            assertEquals(110, animals.get(0).getHunger());
            assertEquals(105, animals.get(0).getHealth());
            assertEquals("Orca", animals.get(0).getSpecie());
            assertEquals("Tuki", animals.get(0).getName());
            assertEquals(16, animals.get(0).getAge());

            assertEquals(115, animals.get(1).getHappinness());
            assertEquals(110, animals.get(1).getHunger());
            assertEquals(105, animals.get(1).getHealth());
            assertEquals("Orca", animals.get(1).getSpecie());
            assertEquals("Tukincho", animals.get(1).getName());
            assertEquals(17, animals.get(1).getAge());

            assertEquals("Water", enclosures.get(1).getName());
            assertEquals(12, enclosures.get(1).getCleaness());
            assertEquals(1, enclosures.get(1).getNumAnimal());
            
        } catch (IOException e) {
            fail("Coudn't read from file");
        }
    }

}
