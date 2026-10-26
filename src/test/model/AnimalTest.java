package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AnimalTest {

    private Animal animalA;
    private Enclosure enclosureA;

    @BeforeEach
    void runBefore() {
        animalA = new Animal("Orca", "tuki", 1);
        enclosureA = new Enclosure("Water Tank");
    }

    // EFFECTS: calls action count times
    private void repeat(int count, Runnable action) {
        for (int i = 0; i < count; i++) {
            action.run();
        }
    }

    @Test
    void testConstructor() {
        assertEquals("Orca", animalA.getSpecies());
        assertEquals("tuki", animalA.getName());
        assertEquals(1, animalA.getAge());
        assertEquals(100, animalA.getHappiness());
        assertEquals(100, animalA.getHealth());
        assertEquals(100, animalA.getHunger());
        assertFalse(animalA.hasEnclosure());
        assertNull(animalA.getEnclosureName());
    }

    @Test
    void testPlay() {
        animalA.play();
        assertEquals(115, animalA.getHappiness());
    }

    @Test
    void testPlayCap() {
        repeat(4, animalA::play);
        assertEquals(150, animalA.getHappiness());
    }

    @Test
    void testFeed() {
        animalA.feed();
        assertEquals(110, animalA.getHunger());
    }

    @Test
    void testFeedCap() {
        repeat(6, animalA::feed);
        assertEquals(150, animalA.getHunger());
    }

    @Test
    void testSleep() {
        animalA.sleep();
        assertEquals(105, animalA.getHealth());
    }

    @Test
    void testSleepCap() {
        repeat(11, animalA::sleep);
        assertEquals(150, animalA.getHealth());
    }

    @Test
    void testTreat() {
        animalA.treat();
        assertEquals(150, animalA.getHappiness());
        assertEquals(150, animalA.getHunger());
        assertEquals(150, animalA.getHealth());
    }

    @Test
    void testSettersClamp() {
        animalA.setHealth(500);
        animalA.setHappiness(-5);
        animalA.setHunger(42);
        assertEquals(150, animalA.getHealth());
        assertEquals(0, animalA.getHappiness());
        assertEquals(42, animalA.getHunger());
    }

    @Test
    void testAverageCondition() {
        animalA.play();
        animalA.feed();
        animalA.sleep();
        assertEquals((115 + 110 + 105) / 3, animalA.getAverageCondition());
    }

    @Test
    void testGetEnclosureName() {
        enclosureA.addAnimal(animalA);
        assertTrue(animalA.hasEnclosure());
        assertEquals(enclosureA, animalA.getEnclosure());
        assertEquals("Water Tank", animalA.getEnclosureName());
    }

    @Test
    void testToJsonWithoutEnclosure() {
        JSONObject json = animalA.toJson();
        assertEquals("tuki", json.getString("Name"));
        assertEquals("", json.getString("Enclosure"));
    }
}
