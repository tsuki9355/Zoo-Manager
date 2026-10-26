package model;

import org.json.JSONObject;

import persistence.Writable;

// Represents an animal with a species, name, age, three condition stats (health, happiness, hunger),
// the enclosure it lives in (null if it has none), and any hospital stay in progress.
// Each day an animal can be played with once and rested once; every night its stats wear down.
public class Animal implements Writable {

    public static final int DEFAULT_CONDITION = 100;
    public static final int MAX_CONDITION = 150;
    public static final int LOW_CONDITION = 60;

    private static final int PLAY_BOOST = 20;
    private static final int FEED_BOOST = 30;
    private static final int REST_BOOST = 10;

    private static final int HUNGER_DECAY = 20;
    private static final int HAPPINESS_DECAY = 10;
    private static final int HEALTH_DECAY = 5;
    private static final int NEGLECT_PENALTY = 10;

    private final String species;
    private final String name;
    private int age;
    private int health;
    private int happiness;
    private int hunger;
    private Enclosure enclosure;
    private int hospitalNights;
    private boolean playedToday;
    private boolean restedToday;

    // REQUIRES: species and name have non-zero length, age >= 0
    // EFFECTS: creates an animal with the given species, name and age, every condition stat set to
    //          DEFAULT_CONDITION, no enclosure and nothing done yet today
    public Animal(String species, String name, int age) {
        this.species = species;
        this.name = name;
        this.age = age;
        this.health = DEFAULT_CONDITION;
        this.happiness = DEFAULT_CONDITION;
        this.hunger = DEFAULT_CONDITION;
    }

    // MODIFIES: this
    // EFFECTS: if not played with yet today, increases happiness by PLAY_BOOST (capped) and returns true;
    //          otherwise returns false
    boolean play() {
        if (playedToday) {
            return false;
        }
        happiness = clamp(happiness + PLAY_BOOST);
        playedToday = true;
        return true;
    }

    // MODIFIES: this
    // EFFECTS: if not rested yet today, increases health by REST_BOOST (capped) and returns true;
    //          otherwise returns false
    boolean rest() {
        if (restedToday) {
            return false;
        }
        health = clamp(health + REST_BOOST);
        restedToday = true;
        return true;
    }

    // MODIFIES: this
    // EFFECTS: increases hunger (fullness) by FEED_BOOST, capped at MAX_CONDITION
    void feed() {
        hunger = clamp(hunger + FEED_BOOST);
    }

    // MODIFIES: this
    // EFFECTS: restores every condition stat to MAX_CONDITION
    void treat() {
        health = MAX_CONDITION;
        happiness = MAX_CONDITION;
        hunger = MAX_CONDITION;
    }

    // MODIFIES: this
    // EFFECTS: wears the animal's stats down for one night (unless it is in the hospital, which looks after it):
    //          hunger and happiness drop; health drops, and drops more if the animal is hungry or its
    //          enclosure is dirty; happiness drops more if it isn't settled in a proper enclosure.
    //          The daily play and rest limits reset either way.
    void passNight() {
        playedToday = false;
        restedToday = false;
        if (isInHospital()) {
            return;
        }
        hunger = clamp(hunger - HUNGER_DECAY);
        happiness = clamp(happiness - HAPPINESS_DECAY - (isSettled() ? 0 : NEGLECT_PENALTY));
        int healthLoss = HEALTH_DECAY;
        if (isHungry()) {
            healthLoss += NEGLECT_PENALTY;
        }
        if (hasEnclosure() && enclosure.isDirty()) {
            healthLoss += NEGLECT_PENALTY;
        }
        health = clamp(health - healthLoss);
    }

    // MODIFIES: this
    // EFFECTS: adds one year to the animal's age
    void haveBirthday() {
        age++;
    }

    // EFFECTS: returns the average of health, happiness and hunger
    public int getAverageCondition() {
        return (health + happiness + hunger) / 3;
    }

    // EFFECTS: returns the catalog entry for this animal's species (UNKNOWN if it isn't in the catalog)
    public Species getSpeciesInfo() {
        return Species.fromName(species);
    }

    // EFFECTS: returns true if the animal lives in a proper enclosure (not the waiting room or nowhere)
    public boolean isSettled() {
        return hasEnclosure() && !enclosure.isWaitingRoom();
    }

    // EFFECTS: returns true if visitors can see this animal (settled and not in the hospital)
    public boolean isOnDisplay() {
        return isSettled() && !isInHospital();
    }

    public boolean isHungry() {
        return hunger < LOW_CONDITION;
    }

    public boolean isUnhappy() {
        return happiness < LOW_CONDITION;
    }

    public boolean isSick() {
        return health < LOW_CONDITION;
    }

    public boolean isInHospital() {
        return hospitalNights > 0;
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

    public int getHospitalNights() {
        return hospitalNights;
    }

    public boolean hasPlayedToday() {
        return playedToday;
    }

    public boolean hasRestedToday() {
        return restedToday;
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

    // EFFECTS: restores the daily flags (used when loading a saved zoo)
    public void setDoneToday(boolean playedToday, boolean restedToday) {
        this.playedToday = playedToday;
        this.restedToday = restedToday;
    }

    // Only Hospital changes this, so the hospital's patient list and the animal always agree
    void setHospitalNights(int nights) {
        this.hospitalNights = Math.max(0, nights);
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
                + " 🍖 " + hunger
                + (isInHospital() ? "   🏥 in hospital" : "");
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
        json.put("HospitalNights", hospitalNights);
        json.put("PlayedToday", playedToday);
        json.put("RestedToday", restedToday);
        return json;
    }
}
