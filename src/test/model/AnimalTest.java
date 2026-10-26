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
        animalA = new Animal("Penguin", "pingu", 1);
        enclosureA = new Enclosure("Ice Rink");
    }

    @Test
    void testConstructor() {
        assertEquals("Penguin", animalA.getSpecies());
        assertEquals(Species.PENGUIN, animalA.getSpeciesInfo());
        assertEquals("pingu", animalA.getName());
        assertEquals(1, animalA.getAge());
        assertEquals(100, animalA.getHappiness());
        assertEquals(100, animalA.getHealth());
        assertEquals(100, animalA.getHunger());
        assertFalse(animalA.hasEnclosure());
        assertNull(animalA.getEnclosureName());
        assertFalse(animalA.isInHospital());
        assertFalse(animalA.hasPlayedToday());
        assertFalse(animalA.hasRestedToday());
    }

    @Test
    void testUnknownSpecies() {
        assertEquals(Species.UNKNOWN, new Animal("Charizard", "Chou", 12).getSpeciesInfo());
    }

    @Test
    void testPlayOncePerDay() {
        assertTrue(animalA.play());
        assertFalse(animalA.play());
        assertEquals(120, animalA.getHappiness());
        assertTrue(animalA.hasPlayedToday());
    }

    @Test
    void testPlayCap() {
        animalA.setHappiness(145);
        animalA.play();
        assertEquals(150, animalA.getHappiness());
    }

    @Test
    void testRestOncePerDay() {
        assertTrue(animalA.rest());
        assertFalse(animalA.rest());
        assertEquals(110, animalA.getHealth());
    }

    @Test
    void testFeedAndCap() {
        animalA.feed();
        assertEquals(130, animalA.getHunger());
        animalA.feed();
        assertEquals(150, animalA.getHunger());
    }

    @Test
    void testTreat() {
        animalA.treat();
        assertEquals(150, animalA.getHappiness());
        assertEquals(150, animalA.getHunger());
        assertEquals(150, animalA.getHealth());
    }

    @Test
    void testPassNightSettled() {
        enclosureA.addAnimal(animalA);
        animalA.play();
        animalA.rest();
        animalA.passNight();
        assertEquals(80, animalA.getHunger());
        assertEquals(110, animalA.getHappiness());
        assertEquals(105, animalA.getHealth());
        assertFalse(animalA.hasPlayedToday());
        assertFalse(animalA.hasRestedToday());
    }

    @Test
    void testPassNightNeglected() {
        // no enclosure, about to go hungry, and nothing done today
        animalA.setHunger(70);
        animalA.passNight();
        assertEquals(50, animalA.getHunger());
        assertEquals(80, animalA.getHappiness());
        assertEquals(85, animalA.getHealth());
    }

    @Test
    void testPassNightDirtyEnclosure() {
        enclosureA.addAnimal(animalA);
        enclosureA.setCleanliness(2);
        animalA.passNight();
        assertEquals(85, animalA.getHealth());
    }

    @Test
    void testPassNightWaitingRoomIsUnsettling() {
        new Enclosure(Zoo.WAITING_ROOM).addAnimal(animalA);
        assertFalse(animalA.isSettled());
        animalA.passNight();
        assertEquals(80, animalA.getHappiness());
    }

    @Test
    void testPassNightInHospitalSkipsDecay() {
        animalA.setHospitalNights(2);
        animalA.passNight();
        assertEquals(100, animalA.getHunger());
        assertEquals(100, animalA.getHealth());
    }

    @Test
    void testStatusChecks() {
        assertFalse(animalA.isOnDisplay());
        enclosureA.addAnimal(animalA);
        assertTrue(animalA.isOnDisplay());
        animalA.setHospitalNights(1);
        assertFalse(animalA.isOnDisplay());
        animalA.setHunger(10);
        animalA.setHappiness(10);
        animalA.setHealth(10);
        assertTrue(animalA.isHungry());
        assertTrue(animalA.isUnhappy());
        assertTrue(animalA.isSick());
    }

    @Test
    void testBirthday() {
        animalA.haveBirthday();
        assertEquals(2, animalA.getAge());
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
        animalA.setHealth(90);
        animalA.setHappiness(120);
        animalA.setHunger(150);
        assertEquals(120, animalA.getAverageCondition());
    }

    @Test
    void testGetEnclosureName() {
        enclosureA.addAnimal(animalA);
        assertTrue(animalA.hasEnclosure());
        assertEquals(enclosureA, animalA.getEnclosure());
        assertEquals("Ice Rink", animalA.getEnclosureName());
    }

    @Test
    void testToJson() {
        animalA.setHospitalNights(1);
        animalA.setDoneToday(true, false);
        JSONObject json = animalA.toJson();
        assertEquals("pingu", json.getString("Name"));
        assertEquals("", json.getString("Enclosure"));
        assertEquals(1, json.getInt("HospitalNights"));
        assertTrue(json.getBoolean("PlayedToday"));
        assertFalse(json.getBoolean("RestedToday"));
    }
}
