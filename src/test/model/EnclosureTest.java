package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnclosureTest {

    private Enclosure enclosureA;
    private Enclosure enclosureB;

    private Animal animalA;
    private Animal animalB;
    private Animal animalC;
    private Animal animalD;
    private Animal animalE;
    private Animal animalF;

    @BeforeEach
    void runBefore() {
        enclosureA = new Enclosure("Orca's Enclosure");
        enclosureB = new Enclosure("Penguin's Enclosure");
        animalA = new Animal("Penguin", "pingu", 3);
        animalB = new Animal("Orca", "tuki", 2);
        animalC = new Animal("Bat", "Xavier", 2);
        animalD = new Animal("Giraffe", "longus", 12);
        animalE = new Animal("Sloth", "Pedro", 7);
        animalF = new Animal("Turtle", "Patrick", 73);
    }

    @Test
    void testConstructor() {
        assertEquals(0, enclosureA.getNumAnimals());
        assertEquals(10, enclosureA.getCleanliness());
        assertEquals(5, enclosureA.getCapacity());
        assertEquals("Orca's Enclosure", enclosureA.getName());
        assertTrue(enclosureA.isEmpty());
        assertFalse(enclosureA.isFull());
    }

    @Test
    void testAddAnimal() {
        assertTrue(enclosureA.addAnimal(animalA));
        assertEquals(Arrays.asList(animalA), enclosureA.getAnimals());
        assertEquals(enclosureA, animalA.getEnclosure());
    }

    @Test
    void testAddAnimalTwice() {
        enclosureA.addAnimal(animalA);
        assertFalse(enclosureA.addAnimal(animalA));
        assertEquals(1, enclosureA.getNumAnimals());
    }

    @Test
    void testAddAnimalCap() {
        for (Animal a : Arrays.asList(animalA, animalB, animalC, animalD, animalE)) {
            assertTrue(enclosureA.addAnimal(a));
        }
        assertTrue(enclosureA.isFull());
        assertFalse(enclosureA.addAnimal(animalF));
        assertEquals(5, enclosureA.getNumAnimals());
        assertNull(animalF.getEnclosure());
    }

    @Test
    void testAddAnimalMovesFromPreviousEnclosure() {
        enclosureA.addAnimal(animalA);
        assertTrue(enclosureB.addAnimal(animalA));
        assertFalse(enclosureA.contains(animalA));
        assertTrue(enclosureB.contains(animalA));
        assertEquals(enclosureB, animalA.getEnclosure());
    }

    @Test
    void testRemoveAnimalTwice() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        assertTrue(enclosureA.removeAnimal(animalB));
        assertFalse(enclosureA.removeAnimal(animalB));
        assertEquals(1, enclosureA.getNumAnimals());
    }

    @Test
    void testRemoveAnimalNotHere() {
        enclosureB.addAnimal(animalA);
        assertFalse(enclosureA.removeAnimal(animalA));
        assertEquals(enclosureB, animalA.getEnclosure());
    }

    @Test
    void testGetAnimalsIsReadOnly() {
        List<Animal> animals = enclosureA.getAnimals();
        assertThrows(UnsupportedOperationException.class, () -> animals.add(animalA));
    }

    @Test
    void testClean() {
        enclosureA.clean();
        assertEquals(12, enclosureA.getCleanliness());
    }

    @Test
    void testCleanCap() {
        enclosureA.clean();
        enclosureA.clean();
        enclosureA.clean();
        assertEquals(15, enclosureA.getCleanliness());
    }

    @Test
    void testSetters() {
        enclosureA.setCapacity(1);
        enclosureA.setCleanliness(99);
        assertEquals(1, enclosureA.getCapacity());
        assertEquals(15, enclosureA.getCleanliness());
        enclosureA.addAnimal(animalA);
        assertTrue(enclosureA.isFull());
    }
}
