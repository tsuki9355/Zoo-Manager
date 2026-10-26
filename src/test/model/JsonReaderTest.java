package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import persistence.JsonReader;

public class JsonReaderTest {

    // EFFECTS: reads the zoo saved in the given data file, failing the test if it can't be read
    private Zoo read(String fileName) {
        try {
            return new JsonReader("./data/" + fileName).read();
        } catch (IOException e) {
            fail("Couldn't read from file " + fileName);
            return null;
        }
    }

    @Test
    void testReadNonExistentFile() {
        JsonReader reader = new JsonReader("./data/non_existent_file.json");
        assertThrows(IOException.class, reader::read);
    }

    @Test
    void testReadEmptyZoo() {
        Zoo zoo = read("TestEmptyZoo.json");
        assertEquals("Angus", zoo.getName());
        assertEquals(0, zoo.getNumAnimals());
        assertEquals(0, zoo.getNumEnclosures());
        assertEquals(1000, zoo.getMoney());
    }

    @Test
    void testReadLegacyFormatSharesAnimalObjects() {
        // older save files also list each enclosure's animals; they must resolve to the zoo's own animals
        Zoo zoo = read("TestMultipleAnimalsZoo.json");
        assertEquals(3, zoo.getNumAnimals());
        assertEquals(1, zoo.getNumEnclosures());
        Enclosure waitingRoom = zoo.findEnclosure(Zoo.WAITING_ROOM);
        assertEquals(3, waitingRoom.getNumAnimals());
        for (Animal animal : zoo.getAnimals()) {
            assertSame(waitingRoom, animal.getEnclosure());
        }
    }

    @Test
    void testReadMultipleAnimalsAndEnclosures() {
        Zoo zoo = read("TestMultipleAnimalsAndEnclosure.json");
        assertEquals("Angus", zoo.getName());
        assertEquals(3, zoo.getNumAnimals());
        assertEquals(3, zoo.getNumEnclosures());
        assertEquals(1000, zoo.getMoney());
        assertEquals("Water", zoo.findAnimal("Tuki").getEnclosureName());
    }

    @Test
    void testReadZooWithDifferentStatus() {
        Zoo zoo = read("TestZooWithDifferentStatus.json");
        assertEquals(1020, zoo.getMoney());

        List<Animal> animals = zoo.getAnimals();
        assertAnimal(animals.get(0), "Orca", "Tuki", 16);
        assertAnimal(animals.get(1), "Orca", "Tukincho", 17);

        Enclosure water = zoo.getEnclosures().get(1);
        assertEquals("Water", water.getName());
        assertEquals(12, water.getCleanliness());
        assertEquals(1, water.getNumAnimals());
        assertSame(animals.get(0), water.getAnimals().get(0));
    }

    // EFFECTS: checks the animal's identity and the condition stats stored in the status test file
    private void assertAnimal(Animal animal, String species, String name, int age) {
        assertEquals(species, animal.getSpecies());
        assertEquals(name, animal.getName());
        assertEquals(age, animal.getAge());
        assertEquals(115, animal.getHappiness());
        assertEquals(110, animal.getHunger());
        assertEquals(105, animal.getHealth());
    }
}
