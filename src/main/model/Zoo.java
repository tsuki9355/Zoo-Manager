package model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import persistence.Writable;

// Represents a zoo with a list of Enclosures, a list of Animals, and the money the zoo have
public class Zoo implements Writable {

    private static int INITIAL_MONEY = 1000;
    private static int INCOME_MULTIPLIER = 10;
 
    private ArrayList<Enclosure> enclosures;
    private ArrayList<Animal> animals;
    private int money;
    private String name;

    /*
     * EFFECTS: set money = INITIAL_MONEY;
     *          create an empty list of enclosures and animals
     */
    public Zoo(String name) {
        this.name = name;
        this.money = INITIAL_MONEY;
        enclosures = new ArrayList<Enclosure>();
        animals = new ArrayList<Animal>();
    }

    // MODIFIES:  this
    // EFFECTS:   increase the money by the number of animals + 2 * INCOME_MULTIPLIER
    public void increaseIncome() {
        this.money = this.money + 2 * INCOME_MULTIPLIER;
    }

    // MODIFIES: this
    // EFFECTS: create a new enclosure with the given name and adds it to enclosures
    public void addEnclosure(String name) {
        Enclosure newEnclosure = new Enclosure(name);
        enclosures.add(newEnclosure);
        EventLog.getInstance().logEvent(new Event(name + " added to " + this.name + "."));
    }

    // MODIFIES: this
    // EFFECTS: add a new enclosure e to zoo
    public void addEnclosure(Enclosure e) {
        enclosures.add(e);
        EventLog.getInstance().logEvent(new Event(e.getName() + " added to " + this.name + "."));
    }

    // MODIFIES: this
    // EFFECTS:  remove enclosure from enclosures if the name exist and if there are no animal
    //           in that enclosure, otherwise do nothing
    public void removeEnclosure(String name) {
        int index = 0;
        for (Enclosure e : enclosures) {
            if (e.getName() == name && e.getNumAnimal() == 0) {
                enclosures.remove(index);
                EventLog.getInstance().logEvent(new Event(e.getName() + " removed from " + this.name + "."));
                break;
            }
            index++;
        }
    }

    // MODIFIES: this
    // EFFECTS: create a new animal with the given name, species and age to animals
    public void addAnimal(String species, String name, int age) {
        Animal newAnimal = new Animal(species, name, age);
        animals.add(newAnimal);
        EventLog.getInstance().logEvent(new Event(name + " added to " + this.name + "."));
    }

    // MODIFIES: this
    // EFFECTS: add an animal a to zoo
    public void addAnimal(Animal a) {
        animals.add(a);
        EventLog.getInstance().logEvent(new Event(name + " added to " + this.name + "."));
    }

    // MODIFIES: this, Enclosure
    // EFFECTS: cleans all enclosure(calls clean() on all enclosure)
    public void cleanAllEnclosure() {
        for (Enclosure e : enclosures) {
            e.clean();
        }
    }

    // REQUIRES: both animalName and enclosureName must exist in the Zoo
    // MODIFIES: this, animal, enclosure
    // EFFECTS: adds the animal with animalName to the enclosure with enclosureName if there is capacity
    //          also sets the animal enclosure to enclosure with the given name
    public void assignAnimalToEnclosure(String animalName, String enclosureName) {
        int animalIndex = getAnimalIndex(animalName);
        int enclosureIndex = getEnclosureIndex(enclosureName);
        Enclosure e = enclosures.get(enclosureIndex);
        Animal a = animals.get(animalIndex);
        if (e.canAcceptAnimal()) {
            e.addAnimal(a);
            a.assign(e);
            EventLog.getInstance().logEvent(new Event(animalName + " added to " + enclosureName + "."));
        }
    }
 
    // REQUIRES: both animalName and enclosureName must exist in the Zoo
    // MODIFIES: this, animal, enclosure
    // EFFECTS: removes animal with animalName from the enclosure with enclosureName if exists
    //          remove animal's enclosure
    public void removeAnimalFromEnclosure(String animalName, String enclosureName) {
        int animalIndex = getAnimalIndex(animalName);
        int enclosureIndex = getEnclosureIndex(enclosureName);
        Animal a = animals.get(animalIndex);
        Enclosure e = enclosures.get(enclosureIndex);
        e.removeAnimal(a);
        a.setToNoEnclosure();
        EventLog.getInstance().logEvent(new Event(animalName + " removed from " + enclosureName + "."));
    } 

    // MODIFIES: this, animal
    // EFFECTS: feed all animals (calls feed() on every animal)
    public void feedAllAnimals() {
        for (Animal a : animals) {
            a.feed();
        }
    }

    // MODIFIES: this, animal
    // EFFECTS: call feed with the animal with the given name, if name doesn't exist then do nothing
    public void feedAnimal(String name)  {
        int index = getAnimalIndex(name);
        Animal a = animals.get(index);
        a.feed();
    }

    // MODIFIES: this, animal
    // EFFECTS: call sleep with the animal with the given name, if name doesn't exist then do nothing
    public void sleepAnimal(String name) {
        int index = getAnimalIndex(name);
        Animal a = animals.get(index);
        a.sleep();
    }

    // MODIFIES: this, animal
    // EFFECTS: call play with the animal with the given name, if name doesn't exist then do nothing
    public void playAnimal(String name) {
        int index = getAnimalIndex(name);
        Animal a = animals.get(index);
        a.play();
    }

    // MODIFIES: this, animal
    // EFFECTS: play with all animals (calls play() on every animal)
    public void playWithAllAnimals() {
        for (Animal a : animals) {
            a.play();
        }
    }

    // MODIFIES: this, animal
    // EFFECTS: make all animals sleep (class sleep() on every animal)
    public void makeAllAnimalSleep() {
        for (Animal a : animals) {
            a.sleep();
        }
    }

    // REQUIRES: animalName to exist in the zoo
    // MODIFIES: this, animal
    // EFFECTS: restore the animal with animalName to full condition (calls treatment())
    public void treatAnimal(String animalName) {
        int index = getAnimalIndex(animalName);
        Animal a = animals.get(index);
        a.treatment();
    }

    // EFFECTS: return a list of all animals
    public ArrayList<Animal> getAllAnimals() {
        return animals;
    }

    // EFFECTS: return the index of animal with given name in animals
    public int getAnimalIndex(String name) {
        int index = 0;
        for (Animal animal : animals) {
            if (animal.name == name) {
                break;
            }
            index++;
        }
        return index;
    }

    // EFFECTS: return the index of enclosure with given name in enclosures
    public int getEnclosureIndex(String name) {
        int index = 0;
        for (Enclosure e : enclosures) {
            if (e.getName() == name) {
                break;
            }
            index++;
        }
        return index;
    }

    // EFFECTS: return the number of animals in the zoo
    public int getNumAnimals() {
        return animals.size();
    }

    // EFFECTS: return the number of enclosures in the zoo
    public int getNumEnclosures() {
        return enclosures.size();
    }

    // EFFECTS: return the zoo name
    public String getZooName() {
        return this.name;
    }

    // EFFECTS: return the zoo money
    public int getZooMoney() {
        return this.money;
    }

    // EFFECTS: return the enclosure's name in enclosures at index
    public String getEnclosureName(int index) {
        Enclosure e = enclosures.get(index);
        return e.getName();
    }

    // EFFECTS: return the number of animals in the given enclosure
    public int getNumAnimalInEnclosure(Enclosure enclosure) {
        return enclosure.getAnimalListSize();
    }

    // EFFECTS: return a list of all enclosures
    public ArrayList<Enclosure> getAllEnclosure() {
        return enclosures;
    }

    // EFFECTS: set the animal's enclosure to the enclosure with the given name
    public void setEnclosureByName(Animal a, String name) {
        for (Enclosure e : enclosures) {
            if (e.getName() == name) {
                a.setEnclosure(e);
                break;
            }
        }
    }

    public void setMoney(int money) {
        this.money = money;
    }

    // EFFECTS: return zoo as a JSON Object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Name", this.name);
        json.put("Money", this.money);
        json.put("Animals", animalsToJson());
        json.put("Enclosures", enclosuresToJson());
        return json;
    }

    // EFFECTS: return animals in this zoo as a JSON array
    private JSONArray animalsToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Animal a : animals) {
            jsonArray.put(a.toJson());
        }
        return jsonArray;
    }

    // EFFECTS: returns enclosures in this zoo as a JSON array
    private JSONArray enclosuresToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Enclosure e : enclosures) {
            jsonArray.put(e.toJson());
        }
        return jsonArray;
    }
}
