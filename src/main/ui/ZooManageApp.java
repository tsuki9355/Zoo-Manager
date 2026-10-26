package ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

import model.ActionResult;
import model.Animal;
import model.DayReport;
import model.Enclosure;
import model.Hospital;
import model.Species;
import model.Zoo;
import persistence.JsonReader;
import persistence.JsonWriter;

// The console version of the zoo managing game
public class ZooManageApp {

    private static final String JSON_STORE = "./data/Zoo.json";

    private static final String[] FEED_LINES = {"gobbles up the meal", "munches happily", "asks for seconds"};
    private static final String[] PLAY_LINES = {"chases the ball around", "does a little happy dance",
        "wants to play again"};
    private static final String[] REST_LINES = {"curls up for a nap", "snores softly", "dreams about snacks"};
    private static final String[] TIPS = {
        "Animals only earn ticket money once they're moved out of the Waiting Room.",
        "Every night animals get hungrier, sadder and a little less healthy. Keep an eye on them!",
        "Feeding uses food from your store. Big animals eat more, so keep it stocked.",
        "Each animal can play once and rest once per day.",
        "Dirty enclosures make animals sick. A sick animal can stay 2 nights in the hospital.",
        "If an animal's health hits 0, the welfare inspector takes it away.",
        "End the day with less than $0 and you're bankrupt. Watch your upkeep!",
    };

    // A numbered menu entry, grouped under a section heading
    private static final class MenuItem {
        final String section;
        final String label;
        final Runnable action;

        MenuItem(String section, String label, Runnable action) {
            this.section = section;
            this.label = label;
            this.action = action;
        }
    }

    private final ConsoleIO io;
    private final JsonWriter jsonWriter;
    private final JsonReader jsonReader;
    private final List<MenuItem> menu;
    private final Random random;

    private Zoo zoo;
    private boolean running;

    // EFFECTS: sets up the game; call run() to play
    public ZooManageApp() {
        io = new ConsoleIO(System.in);
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        random = new Random();
        menu = buildMenu();
    }

    // MODIFIES: this
    // EFFECTS: shows the welcome screen, then runs the game until the user leaves or input ends
    public void run() {
        ZooView.printWelcome();
        try {
            zoo = new Zoo(io.readNonEmpty("First things first: what's your zoo called?"));
            zoo.getOrCreateWaitingRoom();
            io.success("Spectacular! " + zoo.getName() + " is open for business with $" + zoo.getMoney()
                    + " and " + zoo.getFood() + " food.");
            ZooView.printHeading("How to play");
            for (String tip : TIPS) {
                io.say(Ansi.paint("- " + tip, Ansi.DIM));
            }
            running = true;
            while (running) {
                playTurn();
            }
        } catch (ConsoleIO.InputClosedException e) {
            io.blank();
        }
        io.say(Ansi.paint("Thanks for playing Zoo Manager. See you soon!", Ansi.GREEN, Ansi.BOLD));
    }

    // MODIFIES: this
    // EFFECTS: shows the dashboard and menu, then runs the option the user picks
    private void playTurn() {
        ZooView.printDashboard(zoo);
        printMenu();
        int choice = io.readInt("What would you like to do?", 0, menu.size());
        io.blank();
        if (choice == 0) {
            leave();
            return;
        }
        MenuItem item = menu.get(choice - 1);
        ZooView.printHeading(item.label);
        item.action.run();
        io.blank();
        if (running) {
            io.pause();
        }
    }

    // EFFECTS: returns every menu option in display order
    @SuppressWarnings("methodlength")
    private List<MenuItem> buildMenu() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem("Day", "End the day", this::endDay));
        items.add(new MenuItem("Day", "Zoo report", this::showReport));
        items.add(new MenuItem("Animals", "Adopt an animal", this::adoptAnimal));
        items.add(new MenuItem("Animals", "Meet the animals", () -> ZooView.printAnimals(zoo.getAnimals())));
        items.add(new MenuItem("Animals", "Move an animal", this::moveAnimal));
        items.add(new MenuItem("Animals", "Evict from enclosure", this::evictAnimal));
        items.add(new MenuItem("Animals", "Sell an animal", this::sellAnimal));
        items.add(new MenuItem("Care", "Feed an animal", () -> careForOne(zoo::feedAnimal, FEED_LINES)));
        items.add(new MenuItem("Care", "Feed everyone", this::feedEveryone));
        items.add(new MenuItem("Care", "Play with an animal", () -> careForOne(zoo::playAnimal, PLAY_LINES)));
        items.add(new MenuItem("Care", "Play with everyone", () -> careForAll(zoo.playWithAllAnimals(), "played")));
        items.add(new MenuItem("Care", "Rest an animal", () -> careForOne(zoo::restAnimal, REST_LINES)));
        items.add(new MenuItem("Care", "Rest everyone", () -> careForAll(zoo.restAllAnimals(), "rested")));
        items.add(new MenuItem("Care", "Send to hospital", this::sendToHospital));
        items.add(new MenuItem("Enclosures", "Build an enclosure", this::buildEnclosure));
        items.add(new MenuItem("Enclosures", "Expand an enclosure", this::expandEnclosure));
        items.add(new MenuItem("Enclosures", "Demolish an enclosure", this::demolishEnclosure));
        items.add(new MenuItem("Enclosures", "Tour the enclosures", () -> ZooView.printEnclosures(
                zoo.getEnclosures())));
        items.add(new MenuItem("Enclosures", "Clean every enclosure", this::cleanEnclosures));
        items.add(new MenuItem("Business", "Buy food", this::buyFood));
        items.add(new MenuItem("Business", "Upgrade the hospital", this::upgradeHospital));
        items.add(new MenuItem("Business", "Upgrade the zoo", this::upgradeZoo));
        items.add(new MenuItem("Business", "Zoo diary", ZooView::printDiary));
        items.add(new MenuItem("Business", "Save game", this::saveZoo));
        items.add(new MenuItem("Business", "Load game", this::loadZoo));
        return items;
    }

    // EFFECTS: prints the menu, two options per line under each section heading
    private void printMenu() {
        String section = null;
        int column = 0;
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            if (!item.section.equals(section)) {
                flushMenuLine(line);
                section = item.section;
                column = 0;
                io.say(Ansi.paint(section, Ansi.BOLD, Ansi.BLUE));
            }
            line.append(Ansi.paint(String.format("%4d) ", i + 1), Ansi.CYAN)).append(ZooView.padRight(item.label, 24));
            if (++column % 2 == 0) {
                flushMenuLine(line);
            }
        }
        flushMenuLine(line);
        io.say(Ansi.paint(String.format("%4d) ", 0), Ansi.CYAN) + "Leave the zoo");
    }

    // MODIFIES: line
    // EFFECTS: prints line if it has content, then empties it
    private void flushMenuLine(StringBuilder line) {
        if (line.length() > 0) {
            io.say(line.toString());
            line.setLength(0);
        }
    }

    // EFFECTS: tells the user that the action worked (with successMessage) or why it didn't;
    //          returns true if it worked
    private boolean report(ActionResult result, String successMessage) {
        if (result.isSuccess()) {
            io.success(successMessage);
        } else {
            io.warn(result.getMessage());
        }
        return result.isSuccess();
    }

    // ----- Day -----

    // MODIFIES: this
    // EFFECTS: ends the day and shows what happened overnight; on bankruptcy offers to reload the last save
    private void endDay() {
        DayReport dayReport = zoo.endDay();
        ZooView.printDayReport(dayReport, zoo);
        if (zoo.isBankrupt()) {
            ZooView.printGameOver(zoo);
            if (io.confirm("Load your last save and try again?")) {
                loadZoo();
            }
            running = !zoo.isBankrupt();
        }
    }

    // EFFECTS: prints a full report of the zoo
    private void showReport() {
        ZooView.printDashboard(zoo);
        io.blank();
        ZooView.printEnclosures(zoo.getEnclosures());
        io.blank();
        ZooView.printHighlights(zoo);
        String nextUpgrade = zoo.getLevel() >= Zoo.MAX_LEVEL ? "maxed out" : "$" + zoo.getZooUpgradeCost();
        io.say("Hospital beds in use: " + zoo.getHospital().getPatients().size() + "/"
                + zoo.getHospital().getCapacity() + ".  Next zoo upgrade: " + nextUpgrade + ".");
    }

    // ----- Animals -----

    // MODIFIES: this
    // EFFECTS: shows the species shop and adopts the chosen species under a name the user picks
    private void adoptAnimal() {
        io.say("You have " + ZooView.money(zoo.getMoney()) + ". Newcomers arrive in the " + Zoo.WAITING_ROOM + ".");
        Species species = io.choose("Who would you like to adopt?", Species.catalog(),
                s -> ZooView.shopLabel(s, zoo.getLevel()));
        if (species == null) {
            return;
        } else if (!species.isUnlockedAt(zoo.getLevel())) {
            io.warn("Reach zoo level " + species.getRequiredLevel() + " to adopt a " + species.getDisplayName() + ".");
            return;
        } else if (zoo.getMoney() < species.getPrice()) {
            io.warn(ActionResult.NOT_ENOUGH_MONEY.getMessage());
            return;
        }
        String name = io.readNonEmpty("What will you name your " + species.getDisplayName() + "?");
        while (zoo.findAnimal(name) != null) {
            name = io.readNonEmpty("There's already a " + name + ". Pick another name");
        }
        report(zoo.adoptAnimal(species, name), name + " the " + species.getDisplayName()
                + " has arrived! Move them into an enclosure so visitors can see them.");
    }

    // MODIFIES: this
    // EFFECTS: lets the user move an animal into a different enclosure
    private void moveAnimal() {
        Animal animal = chooseAnimal("Who's moving?", a -> true);
        if (animal == null) {
            return;
        }
        List<Enclosure> destinations = new ArrayList<>(zoo.getEnclosures());
        destinations.remove(animal.getEnclosure());
        Enclosure dest = io.choose("Where to?", destinations,
                e -> e.getName() + Ansi.paint("  (" + e.getNumAnimals() + "/" + e.getCapacity() + ")", Ansi.DIM));
        if (dest != null) {
            report(zoo.assignAnimalToEnclosure(animal.getName(), dest.getName()),
                    animal.getName() + " trots happily into '" + dest.getName() + "'.");
        }
    }

    // MODIFIES: this
    // EFFECTS: lets the user take an animal out of its enclosure, leaving it without a home
    private void evictAnimal() {
        Animal animal = chooseAnimal("Who's leaving their enclosure?", Animal::hasEnclosure);
        if (animal != null) {
            String from = animal.getEnclosureName();
            zoo.removeAnimalFromEnclosure(animal.getName(), from);
            io.warn(animal.getName() + " has left '" + from + "' and is now wandering the paths...");
        }
    }

    // MODIFIES: this
    // EFFECTS: lets the user sell an animal to another zoo for part of its price
    private void sellAnimal() {
        Animal animal = io.choose("Who's moving to another zoo?", zoo.getAnimals(),
                a -> a.getName() + Ansi.paint(" the " + a.getSpecies() + "  sells for $" + zoo.getSalePrice(a),
                        Ansi.DIM));
        if (animal != null && io.confirm("Really say goodbye to " + animal.getName() + "?")) {
            int price = zoo.getSalePrice(animal);
            report(zoo.sellAnimal(animal.getName()), "Goodbye, " + animal.getName() + "! You received $" + price + ".");
        }
    }

    // ----- Care -----

    // MODIFIES: the chosen animal
    // EFFECTS: lets the user pick an animal, applies care to it and shows a fun reaction
    private void careForOne(Function<String, ActionResult> care, String[] reactions) {
        Animal animal = chooseAnimal("Which animal?", a -> !a.isInHospital());
        if (animal != null && report(care.apply(animal.getName()), animal.getName() + " " + pick(reactions) + ".")) {
            ZooView.printAnimalCard(animal);
        }
    }

    // EFFECTS: announces how many animals were looked after
    private void careForAll(int count, String verb) {
        if (count == 0) {
            io.warn("Nobody needed that right now (each animal can only do it once a day).");
        } else {
            io.success(ZooView.plural(count, "animal") + " " + verb + ". Lovely!");
        }
    }

    // MODIFIES: this, every animal
    // EFFECTS: feeds every animal, if there's enough food
    private void feedEveryone() {
        int before = zoo.getFood();
        ActionResult result = zoo.feedAllAnimals();
        report(result, "Dinner time! That used " + (before - zoo.getFood()) + " food (" + zoo.getFood() + " left).");
    }

    // MODIFIES: this, the chosen animal
    // EFFECTS: lets the user check an animal into the hospital
    private void sendToHospital() {
        Hospital hospital = zoo.getHospital();
        io.say("Beds in use: " + hospital.getPatients().size() + "/" + hospital.getCapacity()
                + ".  A stay costs $" + Hospital.ADMISSION_FEE + " and lasts " + Hospital.STAY_NIGHTS + " nights.");
        Animal animal = io.choose("Who needs a check-up?", zoo.getAnimals(),
                a -> a.getName() + Ansi.paint(" the " + a.getSpecies() + "  health " + a.getHealth()
                        + (a.isInHospital() ? "  (already in hospital)" : ""), Ansi.DIM));
        if (animal != null) {
            report(zoo.sendToHospital(animal.getName()), animal.getName() + " is checked in. The vets will have "
                    + "them good as new in " + Hospital.STAY_NIGHTS + " nights.");
        }
    }

    // ----- Enclosures -----

    // MODIFIES: this
    // EFFECTS: asks for a name and builds a new enclosure with it
    private void buildEnclosure() {
        io.say("A new enclosure costs $" + Zoo.ENCLOSURE_PRICE + " plus $" + Zoo.ENCLOSURE_UPKEEP
                + " upkeep every day. You have " + ZooView.money(zoo.getMoney()) + ".");
        if (zoo.getMoney() < Zoo.ENCLOSURE_PRICE) {
            io.warn(ActionResult.NOT_ENOUGH_MONEY.getMessage());
            return;
        }
        String name = io.readNonEmpty("Name the new enclosure");
        report(zoo.buildEnclosure(name), "The builders are done! '" + name + "' is ready for residents.");
    }

    // MODIFIES: this
    // EFFECTS: lets the user pay to make an enclosure bigger
    private void expandEnclosure() {
        Enclosure e = io.choose("Which enclosure should grow?", zoo.getEnclosures(),
                en -> en.getName() + Ansi.paint("  " + en.getCapacity() + " spaces, "
                        + (en.canExpand() ? "+2 for $" + en.getExpansionCost() : "already as big as it gets"),
                        Ansi.DIM));
        if (e != null) {
            report(zoo.expandEnclosure(e.getName()), "'" + e.getName() + "' now has room for " + e.getCapacity()
                    + " animals.");
        }
    }

    // MODIFIES: this
    // EFFECTS: lets the user pick an empty enclosure (other than the waiting room) and removes it
    private void demolishEnclosure() {
        List<Enclosure> candidates = new ArrayList<>(zoo.getEnclosures());
        candidates.remove(zoo.findEnclosure(Zoo.WAITING_ROOM));
        Enclosure e = io.choose("Which enclosure should go?", candidates, Enclosure::getName);
        if (e == null) {
            return;
        }
        if (!e.isEmpty()) {
            io.warn("'" + e.getName() + "' still has " + e.getNumAnimals() + " resident(s). Move them out first.");
        } else if (zoo.removeEnclosure(e.getName())) {
            io.success("Bulldozed! '" + e.getName() + "' is gone, and so is its upkeep.");
        }
    }

    // MODIFIES: this
    // EFFECTS: pays to clean every enclosure and shows the results
    private void cleanEnclosures() {
        int cost = zoo.getCleaningCost();
        if (report(zoo.cleanAllEnclosures(), "Scrub scrub scrub... every enclosure is sparkling (-$" + cost + ").")) {
            ZooView.printEnclosures(zoo.getEnclosures());
        }
    }

    // ----- Business -----

    // MODIFIES: this
    // EFFECTS: lets the user buy packs of food
    private void buyFood() {
        io.say("Food store: " + zoo.getFood() + ".  A pack of " + Zoo.FOOD_PACK_SIZE + " food costs $"
                + Zoo.FOOD_PACK_PRICE + ". You have " + ZooView.money(zoo.getMoney()) + ".");
        int affordable = Math.max(0, zoo.getMoney() / Zoo.FOOD_PACK_PRICE);
        int packs = io.readInt("How many packs? (0 to cancel, up to " + affordable + ")", 0, affordable);
        if (packs > 0) {
            report(zoo.buyFood(packs), "The delivery truck drops off " + packs * Zoo.FOOD_PACK_SIZE
                    + " food. Store: " + zoo.getFood() + ".");
        }
    }

    // MODIFIES: this
    // EFFECTS: offers to add a hospital bed
    private void upgradeHospital() {
        Hospital hospital = zoo.getHospital();
        if (hospital.getLevel() >= Hospital.MAX_LEVEL) {
            io.warn(ActionResult.MAX_LEVEL.getMessage());
        } else if (io.confirm("Add a bed (" + hospital.getCapacity() + " -> " + (hospital.getCapacity() + 1)
                + ") for $" + hospital.getUpgradeCost() + "?")) {
            report(zoo.upgradeHospital(), "The new ward is open: " + hospital.getCapacity() + " beds.");
        }
    }

    // MODIFIES: this
    // EFFECTS: offers to raise the zoo level, showing what it would unlock
    private void upgradeZoo() {
        if (zoo.getLevel() >= Zoo.MAX_LEVEL) {
            io.warn("Your zoo is already world class (level " + Zoo.MAX_LEVEL + ")!");
            return;
        }
        int next = zoo.getLevel() + 1;
        List<String> unlocks = new ArrayList<>();
        for (Species s : Species.catalog()) {
            if (s.getRequiredLevel() == next) {
                unlocks.add(s.getDisplayName());
            }
        }
        io.say("Level " + next + " unlocks " + Ansi.paint(String.join(", ", unlocks), Ansi.MAGENTA)
                + " and boosts ticket sales by 10%.");
        if (io.confirm("Upgrade for $" + String.format("%,d", zoo.getZooUpgradeCost()) + "?")) {
            report(zoo.upgradeZoo(), "Ribbon cut! " + zoo.getName() + " is now a level " + zoo.getLevel() + " zoo.");
        }
    }

    // EFFECTS: saves the zoo to JSON_STORE
    private void saveZoo() {
        try {
            jsonWriter.save(zoo);
            io.success("Saved " + zoo.getName() + " (day " + zoo.getDay() + ") to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            io.error("Unable to write to file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: replaces the current zoo with the one saved in JSON_STORE (confirming first unless bankrupt)
    private void loadZoo() {
        if (!zoo.isBankrupt() && !io.confirm("Any unsaved progress will be lost. Load anyway?")) {
            return;
        }
        try {
            zoo = jsonReader.read();
            zoo.getOrCreateWaitingRoom();
            io.success("Welcome back to " + zoo.getName() + "! It's day " + zoo.getDay() + ".");
        } catch (IOException e) {
            io.error("Unable to read from file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: offers to save, then ends the game
    private void leave() {
        if (io.confirm("Save before you go?")) {
            saveZoo();
        }
        running = false;
    }

    // EFFECTS: lets the user pick one of the zoo's animals that passes filter (or cancel, returning null)
    private Animal chooseAnimal(String title, Predicate<Animal> filter) {
        List<Animal> options = new ArrayList<>();
        for (Animal a : zoo.getAnimals()) {
            if (filter.test(a)) {
                options.add(a);
            }
        }
        return io.choose(title, options, a -> a.getName() + Ansi.paint(" the " + a.getSpecies()
                + (a.hasEnclosure() ? ", in " + a.getEnclosureName() : ", no home")
                + (a.isInHospital() ? ", in hospital" : ""), Ansi.DIM));
    }

    // EFFECTS: returns a random element of options
    private String pick(String[] options) {
        return options[random.nextInt(options.length)];
    }
}
