package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HospitalTest {

    private Hospital hospital;
    private Animal animalA;
    private Animal animalB;

    @BeforeEach
    void runBefore() {
        hospital = new Hospital();
        animalA = new Animal("Lion", "Leo", 4);
        animalB = new Animal("Zebra", "Stripes", 2);
    }

    @Test
    void testConstructor() {
        assertEquals(1, hospital.getLevel());
        assertEquals(1, hospital.getCapacity());
        assertEquals(400, hospital.getUpgradeCost());
        assertFalse(hospital.isFull());
    }

    @Test
    void testAdmitUntilFull() {
        assertTrue(hospital.admit(animalA));
        assertTrue(animalA.isInHospital());
        assertEquals(Hospital.STAY_NIGHTS, animalA.getHospitalNights());
        assertTrue(hospital.isFull());
        assertFalse(hospital.admit(animalB));
        assertFalse(animalB.isInHospital());
    }

    @Test
    void testStayAndDischarge() {
        animalA.setHealth(20);
        hospital.admit(animalA);
        assertEquals(Collections.emptyList(), hospital.passNight());
        assertEquals(20, animalA.getHealth());
        assertEquals(Arrays.asList(animalA), hospital.passNight());
        assertFalse(animalA.isInHospital());
        assertEquals(Animal.MAX_CONDITION, animalA.getHealth());
        assertEquals(Animal.MAX_CONDITION, animalA.getHunger());
        assertTrue(hospital.getPatients().isEmpty());
    }

    @Test
    void testUpgrade() {
        assertTrue(hospital.upgrade());
        assertEquals(2, hospital.getCapacity());
        assertEquals(800, hospital.getUpgradeCost());
        hospital.admit(animalA);
        assertTrue(hospital.admit(animalB));
    }

    @Test
    void testUpgradeToMax() {
        hospital.setLevel(Hospital.MAX_LEVEL);
        assertFalse(hospital.upgrade());
        hospital.setLevel(99);
        assertEquals(Hospital.MAX_LEVEL, hospital.getLevel());
    }

    @Test
    void testReadmitKeepsRemainingNights() {
        animalA.setHospitalNights(1);
        hospital.readmit(animalA);
        hospital.readmit(animalA);
        assertEquals(1, hospital.getPatients().size());
        assertEquals(Arrays.asList(animalA), hospital.passNight());
    }
}
