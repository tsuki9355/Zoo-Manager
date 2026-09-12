package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnclosureTest {

    private Enclosure enclosureA;

    private Animal animalA;
    private Animal animalB;
    private Animal animalC;
    private Animal animalD;
    private Animal animalE;
    private Animal animalF;

    @BeforeEach
    void runBefore() {
        enclosureA = new Enclosure("Orca's Enclosure");
        animalA = new Animal("Penguin", "pingu", 3);
        animalB = new Animal("Orca", "tuki", 2);
        animalC = new Animal("Bat", "Xavier", 2);
        animalD = new Animal("Giraffe", "longus", 12);
        animalE = new Animal("Sloth", "Pedro", 7);
        animalF = new Animal("Turtle", "Patrick", 73);
    }

    @Test
    void testConstructor() {
        assertEquals(0, enclosureA.getNumAnimal());
        assertEquals(10, enclosureA.getCleaness());
        assertEquals(5, enclosureA.getCapacity());
        assertEquals("Orca's Enclosure", enclosureA.getName());
        assertEquals(0, enclosureA.getAnimalListSize());
    }

    @Test
    void testGetAnimalIndexEmpty() {
        assertEquals(0, enclosureA.getAnimalIndex(animalA));
    }


    @Test
    void testAddAnimal() {
        enclosureA.addAnimal(animalA);
        assertEquals(animalA, enclosureA.getAnimal(0));
    }

    @Test
    void testAddAnimalMultiple() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        assertEquals(animalB, enclosureA.getAnimal(1));
    }

    @Test
    void testAddAnimalTwice() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalA);
        assertEquals(1, enclosureA.getNumAnimal());
    }

    @Test
    void testAddAnimalCap() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        enclosureA.addAnimal(animalC);
        enclosureA.addAnimal(animalD);
        enclosureA.addAnimal(animalE);
        enclosureA.addAnimal(animalF);
        assertEquals(animalE, enclosureA.getLastAnimal());
    }

    @Test
    void testRemoveAnimal() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        enclosureA.removeAnimal(animalB);
        assertEquals(animalA, enclosureA.getLastAnimal());
        assertEquals(1, enclosureA.getNumAnimal());
    }

    @Test
    void testRemoveAnimalMultiple() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        enclosureA.addAnimal(animalC);
        enclosureA.addAnimal(animalD);
        enclosureA.removeAnimal(animalA);
        enclosureA.removeAnimal(animalD);
        assertEquals(animalC, enclosureA.getLastAnimal());
        assertEquals(2, enclosureA.getNumAnimal());
    }

    @Test
    void testRemoveAnimalCap() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        enclosureA.removeAnimal(animalA);
        enclosureA.removeAnimal(animalB);
        enclosureA.removeAnimal(animalB);
        assertEquals(0, enclosureA.getNumAnimal());
    }

    @Test
    void testRemoveAnimalTwice() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        enclosureA.addAnimal(animalC);
        enclosureA.removeAnimal(animalC);
        enclosureA.removeAnimal(animalC);
        assertEquals(animalB, enclosureA.getLastAnimal());
        assertEquals(2, enclosureA.getNumAnimal());
    }

    @Test
    void testClean() {
        enclosureA.clean();
        assertEquals(12, enclosureA.getCleaness());
    }

    @Test
    void testCleanMultiple() {
        enclosureA.clean();
        enclosureA.clean();
        assertEquals(14, enclosureA.getCleaness());
    }

    @Test
    void testCleanCap() {
        enclosureA.clean();
        enclosureA.clean();
        enclosureA.clean();
        assertEquals(15, enclosureA.getCleaness());
    }

    @Test 
    void testCanAcceptAnimal() {
        assertTrue(enclosureA.canAcceptAnimal());
    }

    @Test
    void testCanAcceptAnimalCap() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        enclosureA.addAnimal(animalC);
        enclosureA.addAnimal(animalD);
        enclosureA.addAnimal(animalE);
        assertFalse(enclosureA.canAcceptAnimal());
    }

    @Test
    void testGetAnimalIndex() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        int index = enclosureA.getAnimalIndex(animalB);
        assertEquals(1, index);
    }

    @Test
    void testGetFirstAnimal() {
        enclosureA.addAnimal(animalA);
        enclosureA.addAnimal(animalB);
        assertEquals(animalA, enclosureA.getFirstAnimal());
    }

}
