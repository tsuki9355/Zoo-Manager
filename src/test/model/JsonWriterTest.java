package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import persistence.JsonReader;
import persistence.JsonWriter;

public class JsonWriterTest {

    // EFFECTS: saves zoo to the general test file and reads it back, failing the test on any error
    private Zoo roundTrip(Zoo zoo) {
        try {
            new JsonWriter("./data/TestGeneralZoo.json").save(zoo);
            return new JsonReader("./data/TestGeneralZoo.json").read();
        } catch (IOException e) {
            fail("Exceptions should not have been thrown here");
            return null;
        }
    }

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
    void testWriteEmptyZoo() {
        try {
            JsonWriter writer = new JsonWriter("./data/TestEmptyZoo.json");
            writer.open();
            writer.write(new Zoo("Angus"));
            writer.close();

            Zoo zoo = new JsonReader("./data/TestEmptyZoo.json").read();
            assertEquals("Angus", zoo.getName());
            assertEquals(0, zoo.getNumEnclosures());
            assertEquals(1000, zoo.getMoney());
        } catch (IOException e) {
            fail("Exceptions should not have been thrown here");
        }
    }

    @Test
    void testWriteGeneralZoo() {
        Zoo zoo = new Zoo("Angus");
        zoo.addAnimal("Orca", "Tuki", 16);
        zoo.addAnimal("Hippo", "Bob", 7);
        zoo.addEnclosure(Zoo.WAITING_ROOM);
        zoo.addEnclosure("Water");
        zoo.assignAnimalToEnclosure("Tuki", "Water");
        zoo.playAnimal("Tuki");

        zoo = roundTrip(zoo);
        assertEquals("Angus", zoo.getName());
        assertEquals(2, zoo.getNumEnclosures());
        assertEquals(1000, zoo.getMoney());

        Animal tuki = zoo.findAnimal("Tuki");
        assertEquals("Orca", tuki.getSpecies());
        assertEquals(16, tuki.getAge());
        assertEquals(120, tuki.getHappiness());
        assertTrue(tuki.hasPlayedToday());
        assertFalse(tuki.hasRestedToday());

        Enclosure water = zoo.findEnclosure("Water");
        assertEquals(10, water.getCleanliness());
        assertEquals(1, water.getNumAnimals());
        assertSame(water, tuki.getEnclosure());
        assertNull(zoo.findAnimal("Bob").getEnclosure());
    }

    @Test
    void testWriteGameProgress() {
        Zoo zoo = new Zoo("Angus");
        zoo.setEventChance(0);
        zoo.setMoney(5000);
        zoo.upgradeZoo();
        zoo.upgradeHospital();
        zoo.buyFood(1);
        zoo.addAnimal("Lion", "Leo", 4);
        zoo.sendToHospital("Leo");
        zoo.endDay();

        Zoo loaded = roundTrip(zoo);
        assertEquals(zoo.getMoney(), loaded.getMoney());
        assertEquals(30, loaded.getFood());
        assertEquals(2, loaded.getDay());
        assertEquals(2, loaded.getLevel());
        assertEquals(2, loaded.getHospital().getCapacity());
        Animal leo = loaded.findAnimal("Leo");
        assertEquals(1, leo.getHospitalNights());
        assertSame(leo, loaded.getHospital().getPatients().get(0));
    }

    @Test
    void testSaveAfterLoad() {
        // round-tripping a loaded zoo used to crash because loaded animals had no enclosure
        try {
            Zoo zoo = new JsonReader("./data/TestMultipleAnimalsZoo.json").read();
            Zoo reloaded = roundTrip(zoo);
            assertEquals(3, reloaded.findEnclosure(Zoo.WAITING_ROOM).getNumAnimals());
        } catch (IOException e) {
            fail("Exceptions should not have been thrown here");
        }
    }
}
