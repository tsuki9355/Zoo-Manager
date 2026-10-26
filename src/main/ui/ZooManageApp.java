package ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import model.Animal;
import model.Enclosure;
import model.Zoo;
import persistence.JsonReader;
import persistence.JsonWriter;

// The console version of the zoo managing game
public class ZooManageApp {

    private static final String JSON_STORE = "./data/Zoo.json";
    private static final int MAX_AGE = 200;

    private static final String[] FEED_LINES = {"gobbles up the snack", "munches happily", "asks for seconds"};
    private static final String[] PLAY_LINES = {"chases the ball around", "does a little happy dance",
        "wants to play again"};
    private static final String[] SLEEP_LINES = {"curls up for a nap", "snores softly", "dreams about snacks"};
    private static final String[] HAPPY_VISITORS = {"\"Best day ever!\"", "\"The animals look so happy!\"",
        "\"We're coming back next week!\""};
    private static final String[] OK_VISITORS = {"\"Nice zoo, could be cleaner.\"", "\"Pretty good!\"",
        "\"The gift shop was fun.\""};
    private static final String[] SAD_VISITORS = {"\"Where are all the animals?\"", "\"Kind of sad, honestly.\"",
        "\"I want a refund...\""};

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
            io.success("Spectacular! " + zoo.getName() + " is open for business.");
            io.say(Ansi.paint("New animals arrive in the " + Zoo.WAITING_ROOM
                    + ", then you can move them into their own enclosures.", Ansi.DIM));
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
        io.pause();
    }

    // EFFECTS: returns every menu option in display order
    @SuppressWarnings("methodlength")
    private List<MenuItem> buildMenu() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem("Enclosures", "Build an enclosure", this::buildEnclosure));
        items.add(new MenuItem("Enclosures", "Demolish an enclosure", this::demolishEnclosure));
        items.add(new MenuItem("Enclosures", "Tour the enclosures", () -> ZooView.printEnclosures(
                zoo.getEnclosures())));
        items.add(new MenuItem("Enclosures", "Clean every enclosure", this::cleanEnclosures));
        items.add(new MenuItem("Animals", "Adopt an animal", this::adoptAnimal));
        items.add(new MenuItem("Animals", "Meet the animals", () -> ZooView.printAnimals(zoo.getAnimals())));
        items.add(new MenuItem("Animals", "Move an animal", this::moveAnimal));
        items.add(new MenuItem("Animals", "Evict from enclosure", this::evictAnimal));
        items.add(new MenuItem("Care", "Feed an animal", () -> careForOne(Animal::feed, FEED_LINES)));
        items.add(new MenuItem("Care", "Feed everyone", () -> careForAll(zoo::feedAllAnimals, "Snack time")));
        items.add(new MenuItem("Care", "Play with an animal", () -> careForOne(Animal::play, PLAY_LINES)));
        items.add(new MenuItem("Care", "Play with everyone", () -> careForAll(zoo::playWithAllAnimals, "Party")));
        items.add(new MenuItem("Care", "Nap time (one)", () -> careForOne(Animal::sleep, SLEEP_LINES)));
        items.add(new MenuItem("Care", "Nap time (everyone)", () -> careForAll(zoo::sleepAllAnimals, "Lights out")));
        items.add(new MenuItem("Care", "Call the vet", this::callVet));
        items.add(new MenuItem("Business", "Sell tickets", this::sellTickets));
        items.add(new MenuItem("Business", "Zoo report", this::showReport));
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

    // MODIFIES: this
    // EFFECTS: asks for a name and builds a new enclosure with it
    private void buildEnclosure() {
        String name = io.readNonEmpty("Name the new enclosure");
        if (zoo.addEnclosure(name)) {
            io.success("The builders are done! '" + name + "' is ready for residents.");
        } else {
            io.error("You already have an enclosure called '" + name + "'.");
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
            io.success("Bulldozed! '" + e.getName() + "' is gone.");
        }
    }

    // MODIFIES: this
    // EFFECTS: cleans every enclosure and shows the results
    private void cleanEnclosures() {
        zoo.cleanAllEnclosures();
        io.success("Scrub scrub scrub... every enclosure is sparkling.");
        ZooView.printEnclosures(zoo.getEnclosures());
    }

    // MODIFIES: this
    // EFFECTS: asks for a new animal's details and places it in the waiting room
    private void adoptAnimal() {
        Enclosure waitingRoom = zoo.getOrCreateWaitingRoom();
        if (waitingRoom.isFull()) {
            io.warn("The " + Zoo.WAITING_ROOM + " is full! Move some animals into enclosures first.");
            return;
        }
        String species = io.readNonEmpty("What species is it?");
        String name = io.readNonEmpty("What's its name?");
        while (zoo.findAnimal(name) != null) {
            io.warn("There's already an animal called " + name + ".");
            name = io.readNonEmpty("Pick another name");
        }
        int age = io.readInt("How old is " + name + "?", 0, MAX_AGE);
        zoo.addAnimal(species, name, age);
        zoo.assignAnimalToEnclosure(name, Zoo.WAITING_ROOM);
        io.success(name + " the " + species + " has arrived and is settling into the " + Zoo.WAITING_ROOM + "!");
    }

    // MODIFIES: this
    // EFFECTS: lets the user move an animal into a different enclosure
    private void moveAnimal() {
        Animal animal = chooseAnimal("Who's moving?", zoo.getAnimals());
        if (animal == null) {
            return;
        }
        List<Enclosure> destinations = new ArrayList<>(zoo.getEnclosures());
        destinations.remove(animal.getEnclosure());
        Enclosure dest = io.choose("Where to?", destinations,
                e -> e.getName() + Ansi.paint("  (" + e.getNumAnimals() + "/" + e.getCapacity() + ")", Ansi.DIM));
        if (dest == null) {
            return;
        }
        reportAssignment(zoo.assignAnimalToEnclosure(animal.getName(), dest.getName()), animal, dest);
    }

    // EFFECTS: tells the user how moving animal into dest went
    private void reportAssignment(Zoo.AssignResult result, Animal animal, Enclosure dest) {
        switch (result) {
            case ASSIGNED:
                io.success(animal.getName() + " trots happily into '" + dest.getName() + "'.");
                break;
            case ENCLOSURE_FULL:
                io.warn("'" + dest.getName() + "' is full! Try somewhere roomier.");
                break;
            case ALREADY_THERE:
                io.warn(animal.getName() + " already lives there.");
                break;
            default:
                io.error("Something went wrong moving " + animal.getName() + ".");
        }
    }

    // MODIFIES: this
    // EFFECTS: lets the user take an animal out of its enclosure, leaving it without a home
    private void evictAnimal() {
        List<Animal> housed = new ArrayList<>();
        for (Animal a : zoo.getAnimals()) {
            if (a.hasEnclosure()) {
                housed.add(a);
            }
        }
        Animal animal = chooseAnimal("Who's leaving their enclosure?", housed);
        if (animal != null) {
            String from = animal.getEnclosureName();
            zoo.removeAnimalFromEnclosure(animal.getName(), from);
            io.warn(animal.getName() + " has left '" + from + "' and is now wandering the paths...");
        }
    }

    // MODIFIES: the chosen animal
    // EFFECTS: lets the user pick an animal, applies care to it and shows a fun reaction
    private void careForOne(Consumer<Animal> care, String[] reactions) {
        Animal animal = chooseAnimal("Which animal?", zoo.getAnimals());
        if (animal == null) {
            return;
        }
        int before = animal.getAverageCondition();
        care.accept(animal);
        if (animal.getAverageCondition() == before) {
            io.warn(animal.getName() + " is already maxed out on that. Try something else!");
        } else {
            io.success(animal.getName() + " " + pick(reactions) + ".");
        }
        ZooView.printAnimalCard(animal);
    }

    // MODIFIES: every animal
    // EFFECTS: applies care to every animal and announces it
    private void careForAll(Runnable care, String announcement) {
        if (zoo.getNumAnimals() == 0) {
            io.warn("There are no animals to look after yet.");
            return;
        }
        care.run();
        io.success(announcement + "! " + ZooView.plural(zoo.getNumAnimals(), "animal") + " looked after.");
    }

    // MODIFIES: the chosen animal
    // EFFECTS: lets the user pick an animal and fully restores its condition
    private void callVet() {
        Animal animal = chooseAnimal("Who needs a check-up?", zoo.getAnimals());
        if (animal != null) {
            animal.treat();
            io.success("The vet gives " + animal.getName() + " a clean bill of health. Good as new!");
            ZooView.printAnimalCard(animal);
        }
    }

    // MODIFIES: this
    // EFFECTS: collects ticket income and shares what a visitor thought
    private void sellTickets() {
        int before = zoo.getMoney();
        zoo.increaseIncome();
        io.success("Ticket sales: +$" + (zoo.getMoney() - before) + "   (balance $"
                + String.format("%,d", zoo.getMoney()) + ")");
        int rating = zoo.getRating();
        String[] quotes = rating >= 4 ? HAPPY_VISITORS : rating >= 2 ? OK_VISITORS : SAD_VISITORS;
        io.say("A visitor says: " + Ansi.paint(pick(quotes), Ansi.MAGENTA));
    }

    // EFFECTS: prints a full report of the zoo
    private void showReport() {
        ZooView.printDashboard(zoo);
        io.blank();
        ZooView.printEnclosures(zoo.getEnclosures());
        io.blank();
        ZooView.printHighlights(zoo);
    }

    // EFFECTS: saves the zoo to JSON_STORE
    private void saveZoo() {
        try {
            jsonWriter.save(zoo);
            io.success("Saved " + zoo.getName() + " to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            io.error("Unable to write to file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: replaces the current zoo with the one saved in JSON_STORE, after confirming
    private void loadZoo() {
        if (!io.confirm("Any unsaved progress will be lost. Load anyway?")) {
            return;
        }
        try {
            zoo = jsonReader.read();
            zoo.getOrCreateWaitingRoom();
            io.success("Welcome back to " + zoo.getName() + "!");
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

    // EFFECTS: lets the user pick one of animals (or cancel, returning null)
    private Animal chooseAnimal(String title, List<Animal> animals) {
        return io.choose(title, animals, a -> a.getName() + Ansi.paint(" the " + a.getSpecies()
                + (a.hasEnclosure() ? ", in " + a.getEnclosureName() : ", no home"), Ansi.DIM));
    }

    // EFFECTS: returns a random element of options
    private String pick(String[] options) {
        return options[random.nextInt(options.length)];
    }
}
