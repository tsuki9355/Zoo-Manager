package model;

import org.json.JSONObject;

import persistence.Writable;

// Represents an animal that have a specie, name, age, health, hunger, happiness and an enclosure
public class Animal implements Writable {

    private static int DEFAULT_HEALTH = 100;
    private static int DEFAULT_HAPPINESS = 100;
    private static int DEFAULT_HUNGER = 100;
    private static int MAX_CONDITION = 150;

    String species;
    String name;
    int age;
    int health;
    int happiness;
    int hunger;
    Enclosure enclosure;
    

    /*
     * REQUIRES: species and name has a non-zero length.
     *           age >= 0
     * EFFECTS:  this.species is set to species; this.name is set to name
     *           this.age is set to age; health is set to DEFAULT_HEALTH.
     *           happiness is set to DEFAULT_HAPPINESS.
     *           hunger is set to DEFAULT_HUNGER
     */
    public Animal(String species, String name, int age) {
        this.species = species;
        this.name = name;
        this.age = age;
        this.health = DEFAULT_HEALTH;
        this.hunger = DEFAULT_HUNGER;
        this.happiness = DEFAULT_HAPPINESS;
    }


    // MODIFIES: this
    // EFFECTs: increase the animal's happiness by 15
    //          if happiness becomes greater than MAX_CONDITION, happiness = MAX_CONDITION.
    public void play() {
        this.happiness += 15;
        if (this.happiness > MAX_CONDITION) {
            this.happiness = MAX_CONDITION;
        }
    }

    // MODIFIES: this
    // EFFECTS: increase the animal's hunger by 10
    //          if hunger becomes greater than MAX_CONDITION, hunger = MAX_CONDITION.
    public void feed() {
        this.hunger += 10;
        if (this.hunger > MAX_CONDITION) {
            this.hunger = MAX_CONDITION;
        }
    }

    // MODIFIES: this
    // EFFECTS: increase the animal's health by 5
    //          if health becoms greater than MAX_CONDITION, health = MAX_CONDITION
    public void sleep() {
        this.health += 5;
        if (this.health > MAX_CONDITION) {
            this.health = MAX_CONDITION;
        }
    }

    // MODIFIES: this
    // EFFECTS: assign enclosure to animal, if animal already have enclosure, do nothing
    public void assign(Enclosure enclosure) {
        if (this.enclosure == null) {
            this.enclosure = enclosure;
        }
    }

    // REQUIRES: dest to exist
    // EFFECTS: swap animal's enclosure to dest
    //          if dest is the same as current enclosure,  do nothing
    public void changeEnclosure(Enclosure dest) {
        this.enclosure = dest;
    }

    // EFFECTS: restore the animal to full condition:
    //          set this.health = MAX_CONDITION 
    //          set this.hunger = MAX_CONDITION
    //          set this.happiness = MAX_CONDITION
    public void treatment() {
        this.health = MAX_CONDITION;
        this.hunger = MAX_CONDITION;
        this.happiness = MAX_CONDITION;
    }

    public int getHealth() {
        return this.health; 
    }

    public int getHappinness() {
        return this.happiness;
    }

    @Override
    public String toString() {
        return name + " (" + species + ") " 
            + " ❤️ " + getHealth() + "   "
            + " 😊 " + getHappinness() + "   "
            + " 🍖 " + getHunger();
    }

    public int getHunger() {
        return this.hunger;
    }

    public String getSpecie() {
        return this.species;
    }

    public String getName() {
        return this.name;
    }

    public Enclosure getEnclosure() {
        return this.enclosure;
    }

    public String getEnclosureName() {
        return this.enclosure.getName();
    }

    public int getAge() {
        return this.age;
    }

    public void setToNoEnclosure() {
        this.enclosure = null;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setHappiness(int happiness) {
        this.happiness = happiness;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public void setEnclosure(Enclosure e) {
        this.enclosure = e;
    }

    // EFFECTS: returns an animal as a JSON object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Species", this.species);
        json.put("Name", this.name);
        json.put("Age", this.age);
        json.put("Health", this.health);
        json.put("Happiness", this.happiness);
        json.put("Hunger", this.hunger);
        json.put("Enclosure", this.enclosure.getName());
        return json;
    }
}
