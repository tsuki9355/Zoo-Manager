package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ZooTest {

    private Zoo zoo;

    @BeforeEach
    void runBefore() {
        zoo = new Zoo("Jurrassic Park");
    }

    @Test
    void testConstructor(){
        assertEquals("Jurrassic Park", zoo.getZooName());
        assertEquals(1000, zoo.getZooMoney());
        assertEquals(0, zoo.getNumEnclosures());
        assertEquals(0, zoo.getNumAnimals());
    }

    @Test
    void testIncreaseIncome() {
        zoo.increaseIncome();
        zoo.increaseIncome();
        assertEquals(1040, zoo.getZooMoney());
    }

    @Test
    void testAddEnclosure() {
        zoo.addEnclosure("Orca's Enclosure");
        assertEquals("Orca's Enclosure", zoo.getEnclosureName(0));
        assertEquals(1, zoo.getNumEnclosures());
    }

    @Test
    void testAddEnclosureMultiple() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addEnclosure("Penguin's Enclosure");
        assertEquals("Orca's Enclosure", zoo.getEnclosureName(0));
        assertEquals("Penguin's Enclosure", zoo.getEnclosureName(1));
        assertEquals(2, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosure() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.removeEnclosure("Orca's Enclosure");
        assertEquals(0, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureMultiple() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addEnclosure("Penguin's Enclosure");
        zoo.removeEnclosure("Orca's Enclosure");
        zoo.removeEnclosure("Penguin's Enclosure"); 
        assertEquals(0, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureWithAnimal() {
        zoo.addEnclosure("Shark's Enclosure");
        zoo.addAnimal("Shark", "tuki", 5);
        zoo.assignAnimalToEnclosure("tuki", "Shark's Enclosure");
        zoo.removeEnclosure("Shark's Enclosure");
        assertEquals(1, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureCondition() {
        Enclosure e = new Enclosure("Water");
        Animal a = new Animal("Orca", "Tuki", 10);
        e.addAnimal(a);
        zoo.removeEnclosure("Water");
        assertEquals(0, zoo.getNumEnclosures());
    }

    @Test
    void testAddAnimal() {
        zoo.addAnimal("Tiger", "King", 5);
        assertEquals(1, zoo.getNumAnimals());
    }

    @Test
    void testAddAnimalMultiple() {
        zoo.addAnimal("Sloth", "Slothy", 3);
        zoo.addAnimal("Crocodile", "Crocy", 3);
        assertEquals(2, zoo.getNumAnimals());
    }

    @Test
    void testCleanAllEnclosure() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addEnclosure("Penguin's Enclosure");
        zoo.cleanAllEnclosure();
        ArrayList<Enclosure> e = zoo.getAllEnclosure();
        Enclosure enclosureA = e.get(0);
        Enclosure enclosureB = e.get(1);
        assertEquals(12, enclosureA.getCleaness());
        assertEquals(12, enclosureB.getCleaness());
    }

    @Test 
    void testAssignAnimalToEnclosure() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addAnimal("Orca", "tuki", 3);
        zoo.assignAnimalToEnclosure("tuki", "Orca's Enclosure");
        ArrayList<Enclosure> e = zoo.getAllEnclosure();
        ArrayList<Animal> a = zoo.getAllAnimals();
        Enclosure enclosureA = e.get(0);
        Animal animalA = a.get(0);
        assertEquals(enclosureA, animalA.getEnclosure());
        assertEquals(1, zoo.getNumAnimalInEnclosure(enclosureA));   
    }

    @Test
    void testAssignAnimalToEnclosureFull() {
        zoo.addEnclosure("water");
        zoo.addAnimal("Orca", "tuki", 3);
        zoo.addAnimal("Orcab", "tukib", 3);
        zoo.addAnimal("Orcac", "tukic", 3);
        zoo.addAnimal("Orcad", "tukid", 3);
        zoo.addAnimal("Orcae", "tukie", 3);
        zoo.addAnimal("Orcaf", "tukif", 3);
        zoo.assignAnimalToEnclosure("tuki", "water");
        zoo.assignAnimalToEnclosure("tukib", "water");
        zoo.assignAnimalToEnclosure("tukic", "water");
        zoo.assignAnimalToEnclosure("tukid", "water");
        zoo.assignAnimalToEnclosure("tukie", "water");
        zoo.assignAnimalToEnclosure("tukif", "water");
        ArrayList<Enclosure> e = zoo.getAllEnclosure();
        ArrayList<Animal> a = zoo.getAllAnimals();
        Enclosure enclosureA = e.get(0);
        Animal animalA = a.get(0);
        assertEquals(enclosureA, animalA.getEnclosure());
        assertEquals(5, zoo.getNumAnimalInEnclosure(enclosureA));   
    }

    @Test
    void testGetEnclosureIndex() {
        zoo.addEnclosure("water");
        zoo.addEnclosure("fire");
        int index = zoo.getEnclosureIndex("fire");
        assertEquals(1, index);
    }

    @Test
    void testRemoveAnimalFromEnclosure() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addAnimal("Orca", "tuki", 3);
        zoo.assignAnimalToEnclosure("tuki", "Orca's Enclosure");
        ArrayList<Enclosure> e = zoo.getAllEnclosure();
        Enclosure enclosureA = e.get(0);
        assertEquals(1, zoo.getNumAnimalInEnclosure(enclosureA));
        zoo.removeAnimalFromEnclosure("tuki", "Orca's Enclosure");
        assertEquals(0, zoo.getNumAnimalInEnclosure(enclosureA));
    }

    @Test
    void testRemoveAnimalFromEnclosureMultiple() {
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addAnimal("Orca", "tuki", 3);
        zoo.addAnimal("Pig", "Piggy", 1);
        zoo.assignAnimalToEnclosure("tuki", "Orca's Enclosure");
        zoo.assignAnimalToEnclosure("Piggy", "Orca's Enclosure");
        ArrayList<Enclosure> e = zoo.getAllEnclosure();
        Enclosure enclosureA = e.get(0);
        assertEquals(2, zoo.getNumAnimalInEnclosure(enclosureA));
        zoo.removeAnimalFromEnclosure("tuki", "Orca's Enclosure");
        zoo.removeAnimalFromEnclosure("Piggy", "Orca's Enclosure");
        assertEquals(0, zoo.getNumAnimalInEnclosure(enclosureA));
    }

    @Test
    void testFeedAllAnimals() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.feedAllAnimals();
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        Animal animalB = a.get(1);
        assertEquals(110, animalA.getHunger());
        assertEquals(110, animalB.getHunger());
    }

    @Test
    void testFeedAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.feedAnimal("tuki");
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        assertEquals(110, animalA.getHunger());
    }

    @Test
    void testFeedAnimalMultiple() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.feedAnimal("tuki");
        zoo.feedAnimal("tuki");
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        assertEquals(120, animalA.getHunger());
    }

    @Test
    void TestSetEnclosureByName() {
        Animal a = new Animal("Orca", "tuki", 2);
        zoo.addEnclosure("Water");
        zoo.addEnclosure("Fire");
        zoo.addAnimal(a);
        zoo.setEnclosureByName(a, "Water");
        assertEquals("Water", a.getEnclosure().getName());
    }

    @Test
    void testGetEnclosureIndexCondition() {
        zoo.addEnclosure("Water");
        zoo.addEnclosure("Fire");
        zoo.addEnclosure("Earth");
        assertEquals(2, zoo.getEnclosureIndex("Earth"));
    }

    @Test
    void testGetEnclosureIndexNoEnclosure() {
        zoo.getEnclosureIndex("Water");
        assertEquals(0, zoo.getNumEnclosures());
    }

    @Test
    void testGetAnimalIndex() {
        assertEquals(0, zoo.getAnimalIndex("Fire"));
    }

    @Test
    void testPlayAllAnimals() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.playWithAllAnimals();
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        Animal animalB = a.get(1);
        assertEquals(115, animalA.getHappinness());
        assertEquals(115, animalB.getHappinness());
    }

    @Test 
    void testPlayAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.playAnimal("tuki");
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        assertEquals(115, animalA.getHappinness());
    }

    @Test
    void testPlayAnimalMultiple() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.playAnimal("tuki");
        zoo.playAnimal("tuki");
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        assertEquals(130, animalA.getHappinness());
    }

    @Test
    void testSleepAllAnimals() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.makeAllAnimalSleep();
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        Animal animalB = a.get(1);
        assertEquals(105, animalA.getHealth());
        assertEquals(105, animalB.getHealth());
    }

    @Test
    void testSleepAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.sleepAnimal("tuki");
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        assertEquals(105, animalA.getHealth());
    }

    @Test
    void testSleepAnimalMultiple() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.sleepAnimal("tuki");
        zoo.sleepAnimal("tuki");
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        assertEquals(110, animalA.getHealth());
    }

    @Test
    void testTreatAnimal() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.treatAnimal("tuki");
        ArrayList<Animal> a = zoo.getAllAnimals();
        Animal animalA = a.get(0);
        assertEquals(150, animalA.getHappinness());
        assertEquals(150, animalA.getHealth());
        assertEquals(150, animalA.getHunger());
    }
}
