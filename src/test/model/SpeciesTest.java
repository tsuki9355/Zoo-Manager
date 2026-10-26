package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SpeciesTest {

    @Test
    void testFromName() {
        assertEquals(Species.RED_PANDA, Species.fromName("Red Panda"));
        assertEquals(Species.RED_PANDA, Species.fromName("red panda"));
        assertEquals(Species.UNKNOWN, Species.fromName("Charizard"));
    }

    @Test
    void testCatalog() {
        assertEquals(16, Species.catalog().size());
        assertFalse(Species.catalog().contains(Species.UNKNOWN));
        assertEquals(Species.MEERKAT, Species.catalog().get(0));
    }

    @Test
    void testUnlocking() {
        assertTrue(Species.PENGUIN.isUnlockedAt(1));
        assertFalse(Species.LION.isUnlockedAt(2));
        assertTrue(Species.LION.isUnlockedAt(3));
        assertFalse(Species.UNKNOWN.isUnlockedAt(Zoo.MAX_LEVEL));
    }

    @Test
    void testDetails() {
        assertEquals("Elephant", Species.ELEPHANT.getDisplayName());
        assertEquals(1800, Species.ELEPHANT.getPrice());
        assertEquals(4, Species.ELEPHANT.getRequiredLevel());
        assertEquals(5, Species.ELEPHANT.getFoodPerMeal());
        assertEquals(200, Species.ELEPHANT.getAppeal());
    }
}
