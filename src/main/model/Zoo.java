package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import persistence.Writable;

// Represents a zoo with a name, money, and the enclosures and animals it owns.
// Animal and enclosure names are unique within a zoo.
public class Zoo implements Writable {

    public static final String WAITING_ROOM = "Waiting Room";
    public static final int INITIAL_MONEY = 1000;
    public static final int MAX_RATING = 5;

    private static final int INCOME_PER_COLLECTION = 20;

    // Outcome of trying to put an animal into an enclosure
    public enum AssignResult {
        ASSIGNED, NO_SUCH_ANIMAL, NO_SUCH_ENCLOSURE, ALREADY_THERE, ENCLOSURE_FULL
    }

    private final String name;
    private final List<Enclosure> enclosures;
    private final List<Animal> animals;
    private int money;

    // EFFECTS: creates a zoo with the given name, INITIAL_MONEY and no enclosures or animals
    public Zoo(String name) {
        this.name = name;
        this.money = INITIAL_MONEY;
        this.enclosures = new ArrayList<>();
        this.animals = new ArrayList<>();
    }

    // MODIFIES: this
    // EFFECTS: adds INCOME_PER_COLLECTION to the zoo's money
    public void increaseIncome() {
        money += INCOME_PER_COLLECTION;
    }

    // MODIFIES: this
    // EFFECTS: creates an enclosure with the given name and adds it to the zoo;
    //          returns false (and does nothing) if an enclosure with that name already exists
    public boolean addEnclosure(String name) {
        return addEnclosure(new Enclosure(name));
    }

    // MODIFIES: this
    // EFFECTS: adds enclosure to the zoo; returns false (and does nothing) if its name is taken
    public boolean addEnclosure(Enclosure enclosure) {
        if (findEnclosure(enclosure.getName()) != null) {
            return false;
        }
        enclosures.add(enclosure);
        log(enclosure.getName() + " added to " + name + ".");
        return true;
    }

    // MODIFIES: this
    // EFFECTS: removes the enclosure with the given name if it exists and is empty and returns true;
    //          otherwise returns false
    public boolean removeEnclosure(String enclosureName) {
        Enclosure enclosure = findEnclosure(enclosureName);
        if (enclosure == null || !enclosure.isEmpty()) {
            return false;
        }
        enclosures.remove(enclosure);
        log(enclosureName + " removed from " + name + ".");
        return true;
    }

    // MODIFIES: this
    // EFFECTS: returns the waiting room enclosure, creating it first if the zoo doesn't have one
    public Enclosure getOrCreateWaitingRoom() {
        Enclosure waitingRoom = findEnclosure(WAITING_ROOM);
        if (waitingRoom == null) {
            waitingRoom = new Enclosure(WAITING_ROOM);
            addEnclosure(waitingRoom);
        }
        return waitingRoom;
    }

    // MODIFIES: this
    // EFFECTS: creates an animal with the given species, name and age and adds it to the zoo;
    //          returns false (and does nothing) if an animal with that name already exists
    public boolean addAnimal(String species, String animalName, int age) {
        return addAnimal(new Animal(species, animalName, age));
    }

    // MODIFIES: this
    // EFFECTS: adds animal to the zoo; returns false (and does nothing) if its name is taken
    public boolean addAnimal(Animal animal) {
        if (findAnimal(animal.getName()) != null) {
            return false;
        }
        animals.add(animal);
        log(animal.getName() + " added to " + name + ".");
        return true;
    }

    // MODIFIES: this, the animal, the enclosures involved
    // EFFECTS: moves the animal with animalName into the enclosure with enclosureName
    //          (out of its current enclosure, if any) and reports the outcome
    public AssignResult assignAnimalToEnclosure(String animalName, String enclosureName) {
        Animal animal = findAnimal(animalName);
        Enclosure enclosure = findEnclosure(enclosureName);
        if (animal == null) {
            return AssignResult.NO_SUCH_ANIMAL;
        } else if (enclosure == null) {
            return AssignResult.NO_SUCH_ENCLOSURE;
        } else if (enclosure.contains(animal)) {
            return AssignResult.ALREADY_THERE;
        } else if (enclosure.isFull()) {
            return AssignResult.ENCLOSURE_FULL;
        }
        enclosure.addAnimal(animal);
        log(animalName + " added to " + enclosureName + ".");
        return AssignResult.ASSIGNED;
    }

    // MODIFIES: this, the animal, the enclosure
    // EFFECTS: if the animal with animalName lives in the enclosure with enclosureName, removes it from
    //          there (leaving it without an enclosure) and returns true; otherwise returns false
    public boolean removeAnimalFromEnclosure(String animalName, String enclosureName) {
        Animal animal = findAnimal(animalName);
        Enclosure enclosure = findEnclosure(enclosureName);
        if (animal == null || enclosure == null || !enclosure.removeAnimal(animal)) {
            return false;
        }
        log(animalName + " removed from " + enclosureName + ".");
        return true;
    }

    // MODIFIES: the animal
    // EFFECTS: feeds the animal with the given name; returns false if there is no such animal
    public boolean feedAnimal(String animalName) {
        Animal animal = findAnimal(animalName);
        if (animal != null) {
            animal.feed();
        }
        return animal != null;
    }

    // MODIFIES: the animal
    // EFFECTS: plays with the animal with the given name; returns false if there is no such animal
    public boolean playAnimal(String animalName) {
        Animal animal = findAnimal(animalName);
        if (animal != null) {
            animal.play();
        }
        return animal != null;
    }

    // MODIFIES: the animal
    // EFFECTS: puts the animal with the given name to sleep; returns false if there is no such animal
    public boolean sleepAnimal(String animalName) {
        Animal animal = findAnimal(animalName);
        if (animal != null) {
            animal.sleep();
        }
        return animal != null;
    }

    // MODIFIES: the animal
    // EFFECTS: fully treats the animal with the given name; returns false if there is no such animal
    public boolean treatAnimal(String animalName) {
        Animal animal = findAnimal(animalName);
        if (animal != null) {
            animal.treat();
        }
        return animal != null;
    }

    // MODIFIES: every animal
    // EFFECTS: feeds every animal in the zoo
    public void feedAllAnimals() {
        animals.forEach(Animal::feed);
    }

    // MODIFIES: every animal
    // EFFECTS: plays with every animal in the zoo
    public void playWithAllAnimals() {
        animals.forEach(Animal::play);
    }

    // MODIFIES: every animal
    // EFFECTS: puts every animal in the zoo to sleep
    public void sleepAllAnimals() {
        animals.forEach(Animal::sleep);
    }

    // MODIFIES: every enclosure
    // EFFECTS: cleans every enclosure in the zoo
    public void cleanAllEnclosures() {
        enclosures.forEach(Enclosure::clean);
    }

    // EFFECTS: returns a visitor rating from 0 to MAX_RATING stars, based mostly on how well the animals
    //          are doing and partly on how clean the enclosures are; a zoo with no animals scores 0
    public int getRating() {
        if (animals.isEmpty()) {
            return 0;
        }
        double condition = animals.stream().mapToInt(Animal::getAverageCondition).average().orElse(0);
        double cleanliness = enclosures.stream().mapToInt(Enclosure::getCleanliness).average().orElse(0);
        double score = 0.7 * condition / Animal.MAX_CONDITION + 0.3 * cleanliness / Enclosure.MAX_CLEANLINESS;
        return (int) Math.round(score * MAX_RATING);
    }

    // EFFECTS: returns the animal with the given name, or null if there is none
    public Animal findAnimal(String animalName) {
        for (Animal animal : animals) {
            if (animal.getName().equals(animalName)) {
                return animal;
            }
        }
        return null;
    }

    // EFFECTS: returns the enclosure with the given name, or null if there is none
    public Enclosure findEnclosure(String enclosureName) {
        for (Enclosure enclosure : enclosures) {
            if (enclosure.getName().equals(enclosureName)) {
                return enclosure;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    // EFFECTS: returns a read-only view of the zoo's animals
    public List<Animal> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    // EFFECTS: returns a read-only view of the zoo's enclosures
    public List<Enclosure> getEnclosures() {
        return Collections.unmodifiableList(enclosures);
    }

    public int getNumAnimals() {
        return animals.size();
    }

    public int getNumEnclosures() {
        return enclosures.size();
    }

    // EFFECTS: records description in the event log
    private void log(String description) {
        EventLog.getInstance().logEvent(new Event(description));
    }

    // EFFECTS: returns this zoo as a JSON object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Name", name);
        json.put("Money", money);
        json.put("Enclosures", toJsonArray(enclosures));
        json.put("Animals", toJsonArray(animals));
        return json;
    }

    // EFFECTS: returns the given items as a JSON array
    private static JSONArray toJsonArray(List<? extends Writable> items) {
        JSONArray jsonArray = new JSONArray();
        for (Writable item : items) {
            jsonArray.put(item.toJson());
        }
        return jsonArray;
    }
}
