package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The zoo hospital: a limited number of beds where animals stay a few nights to be fully treated.
// Upgrading the hospital adds beds.
public class Hospital {

    public static final int MAX_LEVEL = 5;
    public static final int STAY_NIGHTS = 2;
    public static final int ADMISSION_FEE = 60;

    private static final int UPGRADE_COST_PER_LEVEL = 400;

    private final List<Animal> patients;
    private int level;

    // EFFECTS: creates a level-1 hospital with no patients
    public Hospital() {
        this.patients = new ArrayList<>();
        this.level = 1;
    }

    // MODIFIES: this, animal
    // EFFECTS: if there is a free bed and animal isn't already a patient, admits it for STAY_NIGHTS nights
    //          and returns true; otherwise returns false
    boolean admit(Animal animal) {
        if (isFull() || patients.contains(animal)) {
            return false;
        }
        patients.add(animal);
        animal.setHospitalNights(STAY_NIGHTS);
        return true;
    }

    // MODIFIES: this
    // EFFECTS: re-adds an animal that was already mid-stay (used when loading a saved zoo)
    void readmit(Animal animal) {
        if (!patients.contains(animal)) {
            patients.add(animal);
        }
    }

    // MODIFIES: this, every patient
    // EFFECTS: counts one night off every patient's stay; patients whose stay is over are fully treated,
    //          discharged and returned
    List<Animal> passNight() {
        List<Animal> discharged = new ArrayList<>();
        for (Animal patient : patients) {
            patient.setHospitalNights(patient.getHospitalNights() - 1);
            if (!patient.isInHospital()) {
                patient.treat();
                discharged.add(patient);
            }
        }
        patients.removeAll(discharged);
        return discharged;
    }

    // MODIFIES: this
    // EFFECTS: adds one bed (one level); returns false if already at MAX_LEVEL
    boolean upgrade() {
        if (level >= MAX_LEVEL) {
            return false;
        }
        level++;
        return true;
    }

    // EFFECTS: returns the cost of the next upgrade
    public int getUpgradeCost() {
        return UPGRADE_COST_PER_LEVEL * level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(level, MAX_LEVEL));
    }

    // EFFECTS: returns the number of beds (one per level)
    public int getCapacity() {
        return level;
    }

    public boolean isFull() {
        return patients.size() >= getCapacity();
    }

    public List<Animal> getPatients() {
        return Collections.unmodifiableList(patients);
    }
}
