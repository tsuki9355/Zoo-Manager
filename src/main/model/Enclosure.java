package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import persistence.Writable;

// Represents a named enclosure that holds up to capacity animals and has a cleanliness level.
// Each night its residents make it dirtier; a dirty enclosure hurts their health.
public class Enclosure implements Writable {

    public static final int DEFAULT_CAPACITY = 5;
    public static final int MAX_CAPACITY = 15;
    public static final int DEFAULT_CLEANLINESS = 10;
    public static final int MAX_CLEANLINESS = 15;
    public static final int DIRTY_LEVEL = 5;

    private static final int CLEAN_BOOST = 5;
    private static final int EXPANSION_SLOTS = 2;
    private static final int EXPANSION_COST_PER_SLOT = 50;

    private final String name;
    private final List<Animal> animals;
    private int capacity;
    private int cleanliness;

    // EFFECTS: creates an empty enclosure with the given name, DEFAULT_CAPACITY and DEFAULT_CLEANLINESS
    public Enclosure(String name) {
        this.name = name;
        this.animals = new ArrayList<>();
        this.capacity = DEFAULT_CAPACITY;
        this.cleanliness = DEFAULT_CLEANLINESS;
    }

    // MODIFIES: this, animal, animal's previous enclosure
    // EFFECTS: if this enclosure has room and doesn't already hold animal, moves animal in here
    //          (taking it out of its previous enclosure, if any) and returns true;
    //          otherwise does nothing and returns false
    public boolean addAnimal(Animal animal) {
        if (contains(animal) || isFull()) {
            return false;
        }
        if (animal.hasEnclosure()) {
            animal.getEnclosure().removeAnimal(animal);
        }
        animals.add(animal);
        animal.setEnclosure(this);
        return true;
    }

    // MODIFIES: this, animal
    // EFFECTS: if animal is in this enclosure, removes it, leaves it without an enclosure and returns true;
    //          otherwise returns false
    public boolean removeAnimal(Animal animal) {
        if (!animals.remove(animal)) {
            return false;
        }
        animal.setEnclosure(null);
        return true;
    }

    // MODIFIES: this
    // EFFECTS: increases cleanliness by CLEAN_BOOST, capped at MAX_CLEANLINESS
    void clean() {
        setCleanliness(cleanliness + CLEAN_BOOST);
    }

    // MODIFIES: this
    // EFFECTS: lowers cleanliness by one for each resident that isn't away in the hospital
    void passNight() {
        int mess = 0;
        for (Animal animal : animals) {
            if (!animal.isInHospital()) {
                mess++;
            }
        }
        setCleanliness(cleanliness - mess);
    }

    // MODIFIES: this
    // EFFECTS: adds EXPANSION_SLOTS spaces (up to MAX_CAPACITY) and returns true; false if already at MAX_CAPACITY
    boolean expand() {
        if (!canExpand()) {
            return false;
        }
        capacity = Math.min(capacity + EXPANSION_SLOTS, MAX_CAPACITY);
        return true;
    }

    public boolean canExpand() {
        return capacity < MAX_CAPACITY;
    }

    // EFFECTS: returns the cost of the next expansion (bigger enclosures cost more to grow)
    public int getExpansionCost() {
        return EXPANSION_COST_PER_SLOT * capacity;
    }

    public boolean contains(Animal animal) {
        return animals.contains(animal);
    }

    public boolean isFull() {
        return animals.size() >= capacity;
    }

    public boolean isEmpty() {
        return animals.isEmpty();
    }

    public boolean isDirty() {
        return cleanliness < DIRTY_LEVEL;
    }

    public boolean isWaitingRoom() {
        return Zoo.WAITING_ROOM.equals(name);
    }

    public String getName() {
        return name;
    }

    // EFFECTS: returns a read-only view of the animals in this enclosure
    public List<Animal> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    public int getNumAnimals() {
        return animals.size();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCleanliness() {
        return cleanliness;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, Math.min(capacity, MAX_CAPACITY));
    }

    public void setCleanliness(int cleanliness) {
        this.cleanliness = Math.max(0, Math.min(cleanliness, MAX_CLEANLINESS));
    }

    @Override
    public String toString() {
        return name + "  ✨ " + cleanliness + "   🐾 " + animals.size() + "/" + capacity;
    }

    // EFFECTS: returns this enclosure as a JSON object; its animals are saved by the zoo, each
    //          recording the name of the enclosure it lives in
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Name", name);
        json.put("Capacity", capacity);
        json.put("Cleaness", cleanliness);  // key spelling kept so existing save files still load
        return json;
    }
}
