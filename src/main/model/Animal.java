package model;

import org.json.JSONObject;

import persistence.Writable;

// Represents an animal with a species, name, age, three condition stats (health, happiness, hunger)
// and the enclosure it currently lives in (null if it has none)
public class Animal implements Writable {

    public static final int DEFAULT_CONDITION = 100;
    public static final int MAX_CONDITION = 150;

    private static final int PLAY_BOOST = 15;
    private static final int FEED_BOOST = 10;
    private static final int SLEEP_BOOST = 5;

    private final String species;
    private final String name;
    private final int age;
    private int health;
    private int happiness;
    private int hunger;
    private Enclosure enclosure;

    // REQUIRES: species and name have non-zero length, age >= 0
    // EFFECTS: creates an animal with the given species, name and age, every condition stat set to
    //          DEFAULT_CONDITION and no enclosure
    public Animal(String species, String name, int age) {
        this.species = species;
        this.name = name;
        this.age = age;
        this.health = DEFAULT_CONDITION;
        this.happiness = DEFAULT_CONDITION;
        this.hunger = DEFAULT_CONDITION;
    }

    // MODIFIES: this
    // EFFECTS: increases happiness by PLAY_BOOST, capped at MAX_CONDITION
    public void play() {
        happiness = clamp(happiness + PLAY_BOOST);
    }

    // MODIFIES: this
    // EFFECTS: increases hunger (fullness) by FEED_BOOST, capped at MAX_CONDITION
    public void feed() {
        hunger = clamp(hunger + FEED_BOOST);
    }

    // MODIFIES: this
    // EFFECTS: increases health by SLEEP_BOOST, capped at MAX_CONDITION
    public void sleep() {
        health = clamp(health + SLEEP_BOOST);
    }

    // MODIFIES: this
    // EFFECTS: restores every condition stat to MAX_CONDITION
    public void treat() {
        health = MAX_CONDITION;
        happiness = MAX_CONDITION;
        hunger = MAX_CONDITION;
    }

    // EFFECTS: returns the average of health, happiness and hunger
    public int getAverageCondition() {
        return (health + happiness + hunger) / 3;
    }

    // EFFECTS: returns value limited to the range [0, MAX_CONDITION]
    private static int clamp(int value) {
        return Math.max(0, Math.min(value, MAX_CONDITION));
    }

    public String getSpecies() {
        return species;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getHealth() {
        return health;
    }

    public int getHappiness() {
        return happiness;
    }

    public int getHunger() {
        return hunger;
    }

    public Enclosure getEnclosure() {
        return enclosure;
    }

    public boolean hasEnclosure() {
        return enclosure != null;
    }

    // EFFECTS: returns the name of this animal's enclosure, or null if it has none
    public String getEnclosureName() {
        return hasEnclosure() ? enclosure.getName() : null;
    }

    public void setHealth(int health) {
        this.health = clamp(health);
    }

    public void setHappiness(int happiness) {
        this.happiness = clamp(happiness);
    }

    public void setHunger(int hunger) {
        this.hunger = clamp(hunger);
    }

    // Only Enclosure calls this, so an animal and its enclosure always agree on where it lives
    void setEnclosure(Enclosure enclosure) {
        this.enclosure = enclosure;
    }

    @Override
    public String toString() {
        return name + " (" + species + ") "
                + " ❤️ " + health + "   "
                + " 😊 " + happiness + "   "
                + " 🍖 " + hunger;
    }

    // EFFECTS: returns this animal as a JSON object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Species", species);
        json.put("Name", name);
        json.put("Age", age);
        json.put("Health", health);
        json.put("Happiness", happiness);
        json.put("Hunger", hunger);
        json.put("Enclosure", hasEnclosure() ? enclosure.getName() : "");
        return json;
    }
}
