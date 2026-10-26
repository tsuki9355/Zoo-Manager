package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import persistence.Writable;

// Represents a named enclosure that holds up to capacity animals and has a cleanliness level
public class Enclosure implements Writable {

    public static final int DEFAULT_CAPACITY = 5;
    public static final int DEFAULT_CLEANLINESS = 10;
    public static final int MAX_CLEANLINESS = 15;

    private static final int CLEAN_BOOST = 2;

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
    public void clean() {
        setCleanliness(cleanliness + CLEAN_BOOST);
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
        this.capacity = Math.max(0, capacity);
    }

    public void setCleanliness(int cleanliness) {
        this.cleanliness = Math.max(0, Math.min(cleanliness, MAX_CLEANLINESS));
    }

    @Override
    public String toString() {
        return name + "  ✨ " + cleanliness;
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
