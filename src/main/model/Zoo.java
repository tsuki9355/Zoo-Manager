package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import persistence.Writable;

// Represents a zoo: its money, food store, level, hospital, enclosures and animals, and the passing days.
// Player actions cost money or food and report an ActionResult. Ending a day collects ticket money,
// charges upkeep, wears the animals down and may trigger a random event.
// Animal and enclosure names are unique within a zoo.
public class Zoo implements Writable {

    public static final String WAITING_ROOM = "Waiting Room";
    public static final int INITIAL_MONEY = 1000;
    public static final int INITIAL_FOOD = 20;
    public static final int MAX_RATING = 5;
    public static final int MAX_LEVEL = 4;

    public static final int FOOD_PACK_SIZE = 10;
    public static final int FOOD_PACK_PRICE = 40;
    public static final int ENCLOSURE_PRICE = 250;
    public static final int ENCLOSURE_UPKEEP = 10;
    public static final int CLEANING_PRICE = 5;
    public static final int SALE_REFUND_PERCENT = 50;
    public static final int BIRTHDAY_INTERVAL = 10;

    private static final int[] LEVEL_UPGRADE_COSTS = {1500, 4000, 9000};
    private static final double LEVEL_INCOME_BONUS = 0.1;
    private static final double DEFAULT_EVENT_CHANCE = 0.3;
    private static final int EVENT_KINDS = 5;

    private final String name;
    private final List<Enclosure> enclosures;
    private final List<Animal> animals;
    private final Hospital hospital;
    private final Random random;
    private int money;
    private int food;
    private int day;
    private int level;
    private double eventChance;

    // EFFECTS: creates a level-1 zoo on day 1 with the given name, INITIAL_MONEY, INITIAL_FOOD,
    //          an empty hospital and no enclosures or animals
    public Zoo(String name) {
        this(name, new Random());
    }

    // EFFECTS: same as Zoo(name), but random events are drawn from the given random number generator
    public Zoo(String name, Random random) {
        this.name = name;
        this.enclosures = new ArrayList<>();
        this.animals = new ArrayList<>();
        this.hospital = new Hospital();
        this.random = random;
        this.money = INITIAL_MONEY;
        this.food = INITIAL_FOOD;
        this.day = 1;
        this.level = 1;
        this.eventChance = DEFAULT_EVENT_CHANCE;
    }

    // ----- Bookkeeping (free; used when setting up or loading a zoo) -----

    // MODIFIES: this
    // EFFECTS: creates an enclosure with the given name and adds it to the zoo for free;
    //          returns false (and does nothing) if an enclosure with that name already exists
    public boolean addEnclosure(String enclosureName) {
        return addEnclosure(new Enclosure(enclosureName));
    }

    // MODIFIES: this
    // EFFECTS: adds enclosure to the zoo for free; returns false (and does nothing) if its name is taken
    public boolean addEnclosure(Enclosure enclosure) {
        if (findEnclosure(enclosure.getName()) != null) {
            return false;
        }
        enclosures.add(enclosure);
        log(enclosure.getName() + " added to " + name + ".");
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
    // EFFECTS: adds a new animal with the given details for free;
    //          returns false (and does nothing) if an animal with that name already exists
    public boolean addAnimal(String species, String animalName, int age) {
        return addAnimal(new Animal(species, animalName, age));
    }

    // MODIFIES: this
    // EFFECTS: adds animal to the zoo for free; returns false (and does nothing) if its name is taken
    public boolean addAnimal(Animal animal) {
        if (findAnimal(animal.getName()) != null) {
            return false;
        }
        animals.add(animal);
        log(animal.getName() + " added to " + name + ".");
        return true;
    }

    // MODIFIES: this, animal
    // EFFECTS: puts animal back into the hospital for the given number of remaining nights (used when loading)
    public void readmitPatient(Animal animal, int nights) {
        animal.setHospitalNights(nights);
        if (animal.isInHospital()) {
            hospital.readmit(animal);
        }
    }

    // ----- Building -----

    // MODIFIES: this
    // EFFECTS: pays ENCLOSURE_PRICE to build a new enclosure with the given name
    public ActionResult buildEnclosure(String enclosureName) {
        if (findEnclosure(enclosureName) != null) {
            return ActionResult.NAME_TAKEN;
        } else if (!spend(ENCLOSURE_PRICE)) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        addEnclosure(enclosureName);
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this, the enclosure
    // EFFECTS: pays the enclosure's expansion cost to give it more space
    public ActionResult expandEnclosure(String enclosureName) {
        Enclosure enclosure = findEnclosure(enclosureName);
        if (enclosure == null) {
            return ActionResult.NO_SUCH_ENCLOSURE;
        } else if (!enclosure.canExpand()) {
            return ActionResult.MAX_LEVEL;
        } else if (!spend(enclosure.getExpansionCost())) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        enclosure.expand();
        log(enclosureName + " expanded to hold " + enclosure.getCapacity() + " animals.");
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this
    // EFFECTS: removes the enclosure with the given name if it exists, is empty and isn't the waiting room,
    //          and returns true; otherwise returns false
    public boolean removeEnclosure(String enclosureName) {
        Enclosure enclosure = findEnclosure(enclosureName);
        if (enclosure == null || !enclosure.isEmpty() || enclosure.isWaitingRoom()) {
            return false;
        }
        enclosures.remove(enclosure);
        log(enclosureName + " removed from " + name + ".");
        return true;
    }

    // MODIFIES: this
    // EFFECTS: pays to add a hospital bed
    public ActionResult upgradeHospital() {
        if (hospital.getLevel() >= Hospital.MAX_LEVEL) {
            return ActionResult.MAX_LEVEL;
        } else if (!spend(hospital.getUpgradeCost())) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        hospital.upgrade();
        log("Hospital upgraded to " + hospital.getCapacity() + " beds.");
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this
    // EFFECTS: pays to raise the zoo's level, unlocking the next tier of species and boosting ticket income
    public ActionResult upgradeZoo() {
        if (level >= MAX_LEVEL) {
            return ActionResult.MAX_LEVEL;
        } else if (!spend(getZooUpgradeCost())) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        level++;
        log(name + " upgraded to level " + level + ".");
        return ActionResult.SUCCESS;
    }

    // ----- Animals -----

    // MODIFIES: this
    // EFFECTS: pays species' price to adopt a new animal with the given name; it arrives (age 1)
    //          in the waiting room
    public ActionResult adoptAnimal(Species species, String animalName) {
        Enclosure waitingRoom = getOrCreateWaitingRoom();
        if (!species.isUnlockedAt(level)) {
            return ActionResult.LOCKED;
        } else if (findAnimal(animalName) != null) {
            return ActionResult.NAME_TAKEN;
        } else if (waitingRoom.isFull()) {
            return ActionResult.WAITING_ROOM_FULL;
        } else if (!spend(species.getPrice())) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        Animal animal = new Animal(species.getDisplayName(), animalName, 1);
        addAnimal(animal);
        waitingRoom.addAnimal(animal);
        log(animalName + " added to " + WAITING_ROOM + ".");
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this
    // EFFECTS: sends the animal to another zoo, refunding getSalePrice(animal)
    public ActionResult sellAnimal(String animalName) {
        Animal animal = findAnimal(animalName);
        if (animal == null) {
            return ActionResult.NO_SUCH_ANIMAL;
        } else if (animal.isInHospital()) {
            return ActionResult.IN_HOSPITAL;
        }
        money += getSalePrice(animal);
        removeAnimal(animal);
        log(animalName + " was sold to another zoo.");
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this, the animal, the enclosures involved
    // EFFECTS: moves the animal with animalName into the enclosure with enclosureName
    //          (out of its current enclosure, if any) and reports the outcome
    public ActionResult assignAnimalToEnclosure(String animalName, String enclosureName) {
        Animal animal = findAnimal(animalName);
        Enclosure enclosure = findEnclosure(enclosureName);
        if (animal == null) {
            return ActionResult.NO_SUCH_ANIMAL;
        } else if (enclosure == null) {
            return ActionResult.NO_SUCH_ENCLOSURE;
        } else if (enclosure.contains(animal)) {
            return ActionResult.ALREADY_THERE;
        } else if (enclosure.isFull()) {
            return ActionResult.ENCLOSURE_FULL;
        }
        enclosure.addAnimal(animal);
        log(animalName + " added to " + enclosureName + ".");
        return ActionResult.SUCCESS;
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

    // ----- Care -----

    // MODIFIES: this, the animal
    // EFFECTS: feeds the animal one meal, using its species' portion from the food store
    public ActionResult feedAnimal(String animalName) {
        Animal animal = findAnimal(animalName);
        if (animal == null) {
            return ActionResult.NO_SUCH_ANIMAL;
        } else if (animal.isInHospital()) {
            return ActionResult.IN_HOSPITAL;
        } else if (food < getMealSize(animal)) {
            return ActionResult.NOT_ENOUGH_FOOD;
        }
        food -= getMealSize(animal);
        animal.feed();
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this, every animal not in the hospital
    // EFFECTS: feeds every animal not in the hospital one meal, if the food store has enough for all of them
    public ActionResult feedAllAnimals() {
        List<Animal> hungryMouths = animalsMatching(a -> !a.isInHospital());
        int needed = 0;
        for (Animal animal : hungryMouths) {
            needed += getMealSize(animal);
        }
        if (hungryMouths.isEmpty()) {
            return ActionResult.NOTHING_TO_DO;
        } else if (food < needed) {
            return ActionResult.NOT_ENOUGH_FOOD;
        }
        food -= needed;
        hungryMouths.forEach(Animal::feed);
        return ActionResult.SUCCESS;
    }

    // MODIFIES: the animal
    // EFFECTS: plays with the animal (once per day)
    public ActionResult playAnimal(String animalName) {
        return careFor(animalName, Animal::play);
    }

    // MODIFIES: the animal
    // EFFECTS: lets the animal rest to recover some health (once per day)
    public ActionResult restAnimal(String animalName) {
        return careFor(animalName, Animal::rest);
    }

    // MODIFIES: every animal
    // EFFECTS: plays with every animal that isn't in the hospital and hasn't played today; returns how many
    public int playWithAllAnimals() {
        return careForAll(Animal::play);
    }

    // MODIFIES: every animal
    // EFFECTS: rests every animal that isn't in the hospital and hasn't rested today; returns how many
    public int restAllAnimals() {
        return careForAll(Animal::rest);
    }

    // MODIFIES: this, the animal
    // EFFECTS: pays Hospital.ADMISSION_FEE to admit the animal for a full treatment lasting
    //          Hospital.STAY_NIGHTS nights
    public ActionResult sendToHospital(String animalName) {
        Animal animal = findAnimal(animalName);
        if (animal == null) {
            return ActionResult.NO_SUCH_ANIMAL;
        } else if (animal.isInHospital()) {
            return ActionResult.IN_HOSPITAL;
        } else if (hospital.isFull()) {
            return ActionResult.HOSPITAL_FULL;
        } else if (!spend(Hospital.ADMISSION_FEE)) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        hospital.admit(animal);
        log(animalName + " was admitted to the hospital.");
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this, every enclosure
    // EFFECTS: pays CLEANING_PRICE per enclosure to clean every enclosure
    public ActionResult cleanAllEnclosures() {
        if (enclosures.isEmpty()) {
            return ActionResult.NOTHING_TO_DO;
        } else if (!spend(getCleaningCost())) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        enclosures.forEach(Enclosure::clean);
        return ActionResult.SUCCESS;
    }

    // MODIFIES: this
    // EFFECTS: pays FOOD_PACK_PRICE per pack to add packs * FOOD_PACK_SIZE food to the store
    public ActionResult buyFood(int packs) {
        if (packs <= 0) {
            return ActionResult.NOTHING_TO_DO;
        } else if (!spend(packs * FOOD_PACK_PRICE)) {
            return ActionResult.NOT_ENOUGH_MONEY;
        }
        food += packs * FOOD_PACK_SIZE;
        return ActionResult.SUCCESS;
    }

    // ----- The day cycle -----

    // MODIFIES: this, every animal, every enclosure
    // EFFECTS: ends the current day: collects ticket income, pays upkeep, wears down animals and
    //          enclosures overnight, discharges treated patients, lets the welfare inspector take away any
    //          animal whose health reached 0, celebrates birthdays, maybe triggers a random event,
    //          and starts the next day. Returns a report of what happened.
    public DayReport endDay() {
        DayReport report = new DayReport(day, getTicketIncome(), getUpkeep());
        money += report.getProfit();
        animals.forEach(Animal::passNight);
        enclosures.forEach(Enclosure::passNight);
        for (Animal patient : hospital.passNight()) {
            report.addEvent(patient.getName() + " was discharged from the hospital, fit as a fiddle!");
        }
        for (Animal neglected : animalsMatching(a -> a.getHealth() == 0 && !a.isInHospital())) {
            removeAnimal(neglected);
            report.addEvent("The welfare inspector took " + neglected.getName()
                    + " to a sanctuary. Look after your animals!");
        }
        day++;
        if ((day - 1) % BIRTHDAY_INTERVAL == 0 && !animals.isEmpty()) {
            animals.forEach(Animal::haveBirthday);
            report.addEvent("Birthday party! Every animal is a year older.");
        }
        maybeTriggerEvent(report);
        log("Day " + report.getDay() + " ended with $" + money + " in the bank.");
        return report;
    }

    // EFFECTS: returns today's ticket income: the appeal of every animal on display, scaled by the
    //          visitor rating and the zoo's level
    public int getTicketIncome() {
        int appeal = 0;
        for (Animal animal : animalsMatching(Animal::isOnDisplay)) {
            appeal += animal.getSpeciesInfo().getAppeal();
        }
        double ratingFactor = (getRating() + 2) / 5.0;
        double levelFactor = 1 + LEVEL_INCOME_BONUS * (level - 1);
        return (int) Math.round(appeal * ratingFactor * levelFactor);
    }

    // EFFECTS: returns the daily upkeep: ENCLOSURE_UPKEEP for every enclosure except the waiting room
    public int getUpkeep() {
        int upkeep = 0;
        for (Enclosure enclosure : enclosures) {
            if (!enclosure.isWaitingRoom()) {
                upkeep += ENCLOSURE_UPKEEP;
            }
        }
        return upkeep;
    }

    // EFFECTS: returns true if the zoo has run out of money
    public boolean isBankrupt() {
        return money < 0;
    }

    // EFFECTS: returns a visitor rating from 0 to MAX_RATING stars, based mostly on how well the animals at
    //          the zoo (not in the hospital) are doing and partly on how clean the enclosures are;
    //          a zoo with no animals to see scores 0
    public int getRating() {
        List<Animal> onSite = animalsMatching(a -> !a.isInHospital());
        if (onSite.isEmpty()) {
            return 0;
        }
        double condition = onSite.stream().mapToInt(Animal::getAverageCondition).average().orElse(0);
        double cleanliness = enclosures.stream().mapToInt(Enclosure::getCleanliness).average().orElse(0);
        double score = 0.7 * condition / Animal.MAX_CONDITION + 0.3 * cleanliness / Enclosure.MAX_CLEANLINESS;
        return (int) Math.round(score * MAX_RATING);
    }

    // ----- Random events -----

    // MODIFIES: this
    // EFFECTS: with probability eventChance (and only once the zoo has animals), applies one random event
    private void maybeTriggerEvent(DayReport report) {
        if (animals.isEmpty() || random.nextDouble() >= eventChance) {
            return;
        }
        switch (random.nextInt(EVENT_KINDS)) {
            case 0:
                report.addEvent(schoolTrip());
                break;
            case 1:
                report.addEvent(ratsInTheFoodStore());
                break;
            case 2:
                report.addEvent(storm());
                break;
            case 3:
                report.addEvent(newspaperStory());
                break;
            default:
                report.addEvent(donation());
        }
    }

    private String schoolTrip() {
        int bonus = 50 * level;
        money += bonus;
        return "A school trip visited! +$" + bonus + " in extra tickets.";
    }

    private String ratsInTheFoodStore() {
        int lost = food / 4;
        food -= lost;
        return "Rats got into the food store! You lost " + lost + " food.";
    }

    private String storm() {
        enclosures.forEach(e -> e.setCleanliness(e.getCleanliness() - 3));
        return "A storm blew mud and leaves into every enclosure.";
    }

    private String newspaperStory() {
        Animal star = animals.get(random.nextInt(animals.size()));
        star.setHappiness(star.getHappiness() + 25);
        return "The local paper ran a story about " + star.getName() + ". " + star.getName() + " is thrilled!";
    }

    private String donation() {
        int gift = 100 + 50 * level;
        money += gift;
        return "A generous visitor donated $" + gift + ".";
    }

    // ----- Helpers -----

    // MODIFIES: this
    // EFFECTS: if the zoo can afford cost, deducts it and returns true; otherwise returns false
    private boolean spend(int cost) {
        if (money < cost) {
            return false;
        }
        money -= cost;
        return true;
    }

    // MODIFIES: the animal
    // EFFECTS: applies care to the named animal if it's here and not in the hospital; care returns false if
    //          it was already done today
    private ActionResult careFor(String animalName, Predicate<Animal> care) {
        Animal animal = findAnimal(animalName);
        if (animal == null) {
            return ActionResult.NO_SUCH_ANIMAL;
        } else if (animal.isInHospital()) {
            return ActionResult.IN_HOSPITAL;
        }
        return care.test(animal) ? ActionResult.SUCCESS : ActionResult.ALREADY_DONE_TODAY;
    }

    // MODIFIES: every animal
    // EFFECTS: applies care to every animal not in the hospital; returns how many it worked on
    private int careForAll(Predicate<Animal> care) {
        int count = 0;
        for (Animal animal : animalsMatching(a -> !a.isInHospital())) {
            if (care.test(animal)) {
                count++;
            }
        }
        return count;
    }

    // MODIFIES: this
    // EFFECTS: takes animal out of its enclosure and the zoo
    private void removeAnimal(Animal animal) {
        if (animal.hasEnclosure()) {
            animal.getEnclosure().removeAnimal(animal);
        }
        animals.remove(animal);
    }

    // EFFECTS: returns the animals for which test is true, in zoo order
    private List<Animal> animalsMatching(Predicate<Animal> test) {
        List<Animal> matching = new ArrayList<>();
        for (Animal animal : animals) {
            if (test.test(animal)) {
                matching.add(animal);
            }
        }
        return matching;
    }

    // EFFECTS: records description in the event log
    private void log(String description) {
        EventLog.getInstance().logEvent(new Event(description));
    }

    // ----- Queries -----

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

    // EFFECTS: returns how much food one meal for animal uses
    public int getMealSize(Animal animal) {
        return animal.getSpeciesInfo().getFoodPerMeal();
    }

    // EFFECTS: returns how much selling animal would bring in
    public int getSalePrice(Animal animal) {
        return animal.getSpeciesInfo().getPrice() * SALE_REFUND_PERCENT / 100;
    }

    // EFFECTS: returns the cost of cleaning every enclosure once
    public int getCleaningCost() {
        return CLEANING_PRICE * enclosures.size();
    }

    // EFFECTS: returns the cost of the next zoo upgrade, or 0 if the zoo is already at MAX_LEVEL
    public int getZooUpgradeCost() {
        return level >= MAX_LEVEL ? 0 : LEVEL_UPGRADE_COSTS[level - 1];
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

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = Math.max(0, food);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = Math.max(1, day);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(level, MAX_LEVEL));
    }

    // EFFECTS: sets the chance (0 to 1) that a random event happens each night
    public void setEventChance(double eventChance) {
        this.eventChance = eventChance;
    }

    public Hospital getHospital() {
        return hospital;
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

    // EFFECTS: returns this zoo as a JSON object
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("Name", name);
        json.put("Money", money);
        json.put("Food", food);
        json.put("Day", day);
        json.put("Level", level);
        json.put("HospitalLevel", hospital.getLevel());
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
