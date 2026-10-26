package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Zoo.AssignResult;

public class ZooTest {

    private Zoo zoo;

    @BeforeEach
    void runBefore() {
        zoo = new Zoo("Jurrassic Park");
    }

    @Test
    void testConstructor() {
        assertEquals("Jurrassic Park", zoo.getName());
        assertEquals(1000, zoo.getMoney());
        assertEquals(0, zoo.getNumEnclosures());
        assertEquals(0, zoo.getNumAnimals());
    }

    @Test
    void testIncreaseIncome() {
        zoo.increaseIncome();
        zoo.increaseIncome();
        assertEquals(1040, zoo.getMoney());
    }

    @Test
    void testAddEnclosureMultiple() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addEnclosure("Penguin's Enclosure");
        assertEquals("Orca's Enclosure", zoo.getEnclosures().get(0).getName());
        assertEquals("Penguin's Enclosure", zoo.getEnclosures().get(1).getName());
        assertEquals(2, zoo.getNumEnclosures());
    }

    @Test
    void testAddEnclosureDuplicateName() {
        zoo.addEnclosure("Water");
        assertFalse(zoo.addEnclosure("Water"));
        assertEquals(1, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosure() {
        zoo.addEnclosure("Orca's Enclosure");
        assertTrue(zoo.removeEnclosure("Orca's Enclosure"));
        assertEquals(0, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureByTypedName() {
        // names typed by a user are new String objects, not literals; lookups must use equals()
        zoo.addEnclosure("Water");
        assertTrue(zoo.removeEnclosure(new String("Water")));
        assertEquals(0, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureWithAnimal() {
        zoo.addEnclosure("Shark's Enclosure");
        zoo.addAnimal("Shark", "tuki", 5);
        zoo.assignAnimalToEnclosure("tuki", "Shark's Enclosure");
        assertFalse(zoo.removeEnclosure("Shark's Enclosure"));
        assertEquals(1, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureMissing() {
        assertFalse(zoo.removeEnclosure("Water"));
    }

    @Test
    void testGetOrCreateWaitingRoom() {
        Enclosure waitingRoom = zoo.getOrCreateWaitingRoom();
        assertEquals(Zoo.WAITING_ROOM, waitingRoom.getName());
        assertSame(waitingRoom, zoo.getOrCreateWaitingRoom());
        assertEquals(1, zoo.getNumEnclosures());
    }

    @Test
    void testAddAnimalMultiple() {
        zoo.addAnimal("Sloth", "Slothy", 3);
        zoo.addAnimal("Crocodile", "Crocy", 3);
        assertEquals(2, zoo.getNumAnimals());
    }

    @Test
    void testAddAnimalDuplicateName() {
        zoo.addAnimal("Sloth", "Slothy", 3);
        assertFalse(zoo.addAnimal(new Animal("Crocodile", "Slothy", 3)));
        assertEquals(1, zoo.getNumAnimals());
    }

    @Test
    void testFindAnimalAndEnclosure() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addEnclosure("Water");
        assertEquals("Orca", zoo.findAnimal("tuki").getSpecies());
        assertEquals("Water", zoo.findEnclosure("Water").getName());
        assertNull(zoo.findAnimal("Fire"));
        assertNull(zoo.findEnclosure("Fire"));
    }

    @Test
    void testCleanAllEnclosures() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addEnclosure("Penguin's Enclosure");
        zoo.cleanAllEnclosures();
        assertEquals(12, zoo.getEnclosures().get(0).getCleanliness());
        assertEquals(12, zoo.getEnclosures().get(1).getCleanliness());
    }

    @Test
    void testAssignAnimalMovesBetweenEnclosures() {
        zoo.addEnclosure("Water");
        zoo.addEnclosure("Fire");
        zoo.addAnimal("Orca", "tuki", 3);
        zoo.assignAnimalToEnclosure("tuki", "Water");
        assertEquals(AssignResult.ASSIGNED, zoo.assignAnimalToEnclosure("tuki", "Fire"));
        assertEquals(0, zoo.findEnclosure("Water").getNumAnimals());
        assertEquals(1, zoo.findEnclosure("Fire").getNumAnimals());
        assertEquals("Fire", zoo.findAnimal("tuki").getEnclosureName());
    }

    @Test
    void testAssignAnimalToEnclosureFailures() {
        zoo.addEnclosure("Water");
        zoo.addAnimal("Orca", "tuki", 3);
        assertEquals(AssignResult.NO_SUCH_ANIMAL, zoo.assignAnimalToEnclosure("nobody", "Water"));
        assertEquals(AssignResult.NO_SUCH_ENCLOSURE, zoo.assignAnimalToEnclosure("tuki", "Nowhere"));
        zoo.assignAnimalToEnclosure("tuki", "Water");
        assertEquals(AssignResult.ALREADY_THERE, zoo.assignAnimalToEnclosure("tuki", "Water"));
    }

    @Test
    void testAssignAnimalToEnclosureFull() {
        zoo.addEnclosure("water");
        String[] names = {"tuki", "tukib", "tukic", "tukid", "tukie", "tukif"};
        for (String name : names) {
            zoo.addAnimal("Orca", name, 3);
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(AssignResult.ASSIGNED, zoo.assignAnimalToEnclosure(names[i], "water"));
        }
        assertEquals(AssignResult.ENCLOSURE_FULL, zoo.assignAnimalToEnclosure("tukif", "water"));
        assertEquals(5, zoo.findEnclosure("water").getNumAnimals());
        assertNull(zoo.findAnimal("tukif").getEnclosure());
    }

    @Test
    void testRemoveAnimalFromEnclosure() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addAnimal("Orca", "tuki", 3);
        zoo.assignAnimalToEnclosure("tuki", "Orca's Enclosure");
        assertTrue(zoo.removeAnimalFromEnclosure("tuki", "Orca's Enclosure"));
        assertEquals(0, zoo.getEnclosures().get(0).getNumAnimals());
        assertNull(zoo.findAnimal("tuki").getEnclosure());
    }

    @Test
    void testRemoveAnimalFromEnclosureFailures() {
        zoo.addEnclosure("Water");
        zoo.addAnimal("Orca", "tuki", 3);
        assertFalse(zoo.removeAnimalFromEnclosure("tuki", "Water"));
        assertFalse(zoo.removeAnimalFromEnclosure("nobody", "Water"));
        assertFalse(zoo.removeAnimalFromEnclosure("tuki", "Nowhere"));
    }

    @Test
    void testFeedAllAnimals() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.feedAllAnimals();
        assertEquals(110, zoo.getAnimals().get(0).getHunger());
        assertEquals(110, zoo.getAnimals().get(1).getHunger());
    }

    @Test
    void testFeedAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        assertTrue(zoo.feedAnimal("tuki"));
        assertTrue(zoo.feedAnimal("tuki"));
        assertEquals(120, zoo.findAnimal("tuki").getHunger());
        assertFalse(zoo.feedAnimal("nobody"));
    }

    @Test
    void testPlayWithAllAnimals() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.playWithAllAnimals();
        assertEquals(115, zoo.getAnimals().get(0).getHappiness());
        assertEquals(115, zoo.getAnimals().get(1).getHappiness());
    }

    @Test
    void testPlayAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        assertTrue(zoo.playAnimal("tuki"));
        assertTrue(zoo.playAnimal("tuki"));
        assertEquals(130, zoo.findAnimal("tuki").getHappiness());
        assertFalse(zoo.playAnimal("nobody"));
    }

    @Test
    void testSleepAllAnimals() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.sleepAllAnimals();
        assertEquals(105, zoo.getAnimals().get(0).getHealth());
        assertEquals(105, zoo.getAnimals().get(1).getHealth());
    }

    @Test
    void testSleepAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        assertTrue(zoo.sleepAnimal("tuki"));
        assertTrue(zoo.sleepAnimal("tuki"));
        assertEquals(110, zoo.findAnimal("tuki").getHealth());
        assertFalse(zoo.sleepAnimal("nobody"));
    }

    @Test
    void testTreatAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        assertTrue(zoo.treatAnimal("tuki"));
        Animal animalA = zoo.findAnimal("tuki");
        assertEquals(150, animalA.getHappiness());
        assertEquals(150, animalA.getHealth());
        assertEquals(150, animalA.getHunger());
        assertFalse(zoo.treatAnimal("nobody"));
    }

    @Test
    void testRatingEmptyZoo() {
        assertEquals(0, zoo.getRating());
    }

    @Test
    void testRatingDefaultAndBest() {
        zoo.addEnclosure("Water");
        zoo.addAnimal("Orca", "tuki", 2);
        assertEquals(3, zoo.getRating());
        zoo.treatAnimal("tuki");
        zoo.cleanAllEnclosures();
        zoo.cleanAllEnclosures();
        zoo.cleanAllEnclosures();
        assertEquals(Zoo.MAX_RATING, zoo.getRating());
    }
}
