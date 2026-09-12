package model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import persistence.Writable;

// Represent an enclosure with capacity, number of animals in the enclosure and a list of animals.
public class Enclosure implements Writable {

    private static int DEFAULT_CAPACITY = 5;
    private static int DEFAULT_CLEANESS = 10;
    private static int MAX_CLEAN = 15;

    private int numAnimals;
    private ArrayList<Animal> animals;
    private int cleaness;
    private int capacity;
    private String name;

    /*
     * EFFECTS: numAnimals is set to 0, list of animals is empty
     *          cleaness is set to DEFAULT_CLEANESS
     *          capacity is set to DEFAULT_CAPACITY
     *          this.name is set to name
     */
    public Enclosure(String name) {
        this.numAnimals = 0;
        animals = new ArrayList<Animal>();
        this.cleaness = DEFAULT_CLEANESS;
        this.capacity = DEFAULT_CAPACITY;
        this.name = name;
    }

    // MODIFIES: this
    // EFFECTS: Add animal to animals if numAnimals < capacity and if animal doesn't already exist, 
    //          otherwise do nothing
    public void addAnimal(Animal animal) {
        if (getExist(animal) == false && numAnimals < capacity) {
            animals.add(animal);
            this.numAnimals++;
        }
    }

    // MODIFIES: this
    // EFFECTS: remove animal from animals along with the corresponding enclosure
    //          otherwise do nothing 
    public void removeAnimal(Animal animal) {
        if (getExist(animal)) {
            animals.remove(animal);
            this.numAnimals--;
        }
    }

    // EFFECTS: return the index of animal in animals
    public int getAnimalIndex(Animal animal) {
        int index = 0;
        for (Animal a : animals) {
            if (a == animal) {
                break;
            }
            index++;
        }
        return index;
    }

    // MODIFIES: this
    // EFFECTS:  increase the cleaness by 2, if clean > MAX_CLEAN, clean = MAX_CLEAN
    public void clean() {
        this.cleaness += 2;
        if (this.cleaness > MAX_CLEAN) {
            this.cleaness = MAX_CLEAN;
        }
    }

    // EFFECTS: returns true if numAnimals < Capacity and false otherwise
    public boolean canAcceptAnimal() {
        return this.numAnimals < this.capacity;
    }

    public int getCleaness() {
        return this.cleaness;
    }

    public int getNumAnimal() {
        return this.numAnimals;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public String getName() {
        return this.name;
    }

    public int getAnimalListSize() {
        return animals.size();
    }

    public ArrayList<Animal> getAllAnimals() {
        return this.animals;
    }

    @Override
    public String toString() {
        return getName() + "  ✨ " + getCleaness();
    }

    public Animal getAnimal(int index) {
        return animals.get(index);
    }

    public Animal getLastAnimal() {
        return animals.get(numAnimals - 1);
    }

    public Animal getFirstAnimal() {
        return animals.get(0);
    }

    public boolean getExist(Animal animal) {
        return animals.contains(animal);
    }

    public void setCleaness(int cleaness) {
        this.cleaness = cleaness;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setNumAnimals(int numAnimals) {
        this.numAnimals = numAnimals;
    }

    public void setAnimals(ArrayList<Animal> a) {
        this.animals = a;
    }

    // return an enclosure as a JSON object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("numAnimals", this.numAnimals);
        json.put("Animals", animalsToJson());
        json.put("Cleaness", this.cleaness);
        json.put("Name", this.name);
        json.put("Capacity", this.capacity);
        return json;
    }

    // returns animals in this enclosure as a JSON array
    private JSONArray animalsToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Animal a : animals) {
            jsonArray.put(a.toJson());
        }
        return jsonArray;
    }

}
