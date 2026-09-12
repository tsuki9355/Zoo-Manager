package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AnimalTest {

    private Animal animalA;
    private Enclosure enclosureA;
    private Enclosure enclosureB;
    
    @BeforeEach
    void runBefore() {
        animalA = new Animal("Orca", "tuki", 1);
        enclosureA = new Enclosure("Water Tank");
        enclosureB = new Enclosure("Orca Enclosure");
    }

    @Test
    void testConstructor() {
        assertEquals("Orca", animalA.getSpecie());
        assertEquals("tuki", animalA.getName());
        assertEquals(1, animalA.getAge());
        assertEquals(100, animalA.getHappinness());
        assertEquals(100, animalA.getHealth());
        assertEquals(100, animalA.getHunger());
    }

    @Test
    void testSetEnclosure() {
        animalA.setEnclosure(enclosureA);
        assertEquals(enclosureA, animalA.getEnclosure());
    }

    @Test
    void testPlay() {
        animalA.play();
        assertEquals(115, animalA.getHappinness());
    }

    @Test
    void testPlayMultiple() {
        animalA.play();
        animalA.play();
        animalA.play();
        assertEquals(145, animalA.getHappinness());
    }

    @Test
    void testPlayCap() {
        animalA.play();
        animalA.play();
        animalA.play();
        animalA.play();
        assertEquals(150, animalA.getHappinness());
    }

    @Test
    void testFeed() {
        animalA.feed();
        assertEquals(110, animalA.getHunger());
    }

    @Test
    void testFeedMultiple() {
        animalA.feed();
        animalA.feed();
        animalA.feed();
        assertEquals(130, animalA.getHunger());
    }
        
    @Test
    void testFeedCap() {
        animalA.feed();
        animalA.feed();
        animalA.feed();
        animalA.feed();
        animalA.feed();
           animalA.feed();
        assertEquals(150, animalA.getHunger());
    }

    @Test
    void testGetEnclosureName() {
       animalA.assign(enclosureA);
       assertEquals("Water Tank", animalA.getEnclosureName());
   }

    @Test
    void testSleep() {
        animalA.sleep();
        assertEquals(105, animalA.getHealth());
    }

    @Test
    void testSleepMultiple() {
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        assertEquals(115, animalA.getHealth());
    }

    @Test
    void testSleepCap() {
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        animalA.sleep();
        assertEquals(150, animalA.getHealth());
    }

    @Test
    void testAssign() {
        animalA.assign(enclosureA);
        assertEquals(enclosureA, animalA.getEnclosure());
    }

    @Test
    void testAssignMultiple() {
        animalA.assign(enclosureA);
        animalA.assign(enclosureB);
        assertEquals(enclosureA, animalA.getEnclosure());
    }

    @Test
    void testChange() {
        animalA.assign(enclosureA);
        assertEquals(enclosureA, animalA.getEnclosure());
        animalA.changeEnclosure(enclosureB);
        assertEquals(enclosureB, animalA.getEnclosure());
    }

    @Test
    void testSwapSame() {
        animalA.assign(enclosureA);
        assertEquals(enclosureA, animalA.getEnclosure());
        animalA.changeEnclosure(enclosureA);
        assertEquals(enclosureA, animalA.getEnclosure());
    }

    @Test
    void testTreatment() {
        animalA.treatment();
        assertEquals(150, animalA.getHappinness());
        assertEquals(150, animalA.getHunger());
        assertEquals(150, animalA.getHealth());
    }
}
