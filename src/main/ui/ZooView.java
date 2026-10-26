package ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.function.Predicate;

import model.Animal;
import model.DayReport;
import model.Enclosure;
import model.Event;
import model.EventLog;
import model.Hospital;
import model.Species;
import model.Zoo;

// Renders the zoo, its animals and enclosures as text for the console app
final class ZooView {

    static final int WIDTH = 64;

    private static final int BAR_WIDTH = 15;
    private static final int DIARY_ENTRIES = 10;

    private static final String[] BANNER = {
        " _____                 __  __",
        "|__  /___   ___       |  \\/  | __ _ _ __   __ _  __ _  ___ _ __",
        "  / // _ \\ / _ \\      | |\\/| |/ _` | '_ \\ / _` |/ _` |/ _ \\ '__|",
        " / /| (_) | (_) |     | |  | | (_| | | | | (_| | (_| |  __/ |",
        "/____\\___/ \\___/      |_|  |_|\\__,_|_| |_|\\__,_|\\__, |\\___|_|",
        "                                                |___/",
    };

    private static final String[] ELEPHANT = {
        "      __     __",
        "     /  \\~~~/  \\      Welcome, future zookeeper!",
        "    (    o o    )     Build enclosures, adopt animals",
        "     \\  (_ _)  /      and keep your visitors smiling.",
        "      |  |||  |",
        "      |__|||__|",
    };

    private ZooView() {
    }

    // EFFECTS: prints the title banner and mascot
    static void printWelcome() {
        System.out.println();
        for (String line : BANNER) {
            System.out.println("  " + Ansi.paint(line, Ansi.GREEN, Ansi.BOLD));
        }
        System.out.println();
        for (String line : ELEPHANT) {
            System.out.println("  " + Ansi.paint(line, Ansi.YELLOW));
        }
        System.out.println();
    }

    // EFFECTS: prints a heading followed by a rule, e.g. "== Animals ======="
    static void printHeading(String title) {
        String head = "== " + title + " ";
        System.out.println("  " + Ansi.paint(head + repeat('=', WIDTH - head.length()), Ansi.BLUE, Ansi.BOLD));
    }

    // EFFECTS: prints a one-glance summary of the zoo (day, level, money, food, rating, hospital, counts)
    //          followed by warnings about anything that needs attention
    static void printDashboard(Zoo zoo) {
        String rule = Ansi.paint(repeat('=', WIDTH), Ansi.GREEN);
        Hospital hospital = zoo.getHospital();
        System.out.println();
        System.out.println("  " + rule);
        System.out.println("   " + Ansi.paint(zoo.getName().toUpperCase(), Ansi.BOLD, Ansi.GREEN)
                + Ansi.paint("   Day " + zoo.getDay() + "  -  Zoo level " + zoo.getLevel() + "/" + Zoo.MAX_LEVEL,
                Ansi.DIM));
        System.out.println("   " + money(zoo.getMoney())
                + divider() + "Food " + Ansi.paint(String.valueOf(zoo.getFood()), Ansi.BOLD)
                + divider() + "Rating " + stars(zoo.getRating())
                + divider() + "Hospital " + hospital.getPatients().size() + "/" + hospital.getCapacity());
        System.out.println("   " + plural(zoo.getNumAnimals(), "animal")
                + divider() + plural(zoo.getNumEnclosures(), "enclosure")
                + divider() + "Tonight: +" + money(zoo.getTicketIncome()) + " tickets, -"
                + money(zoo.getUpkeep()) + " upkeep");
        System.out.println("  " + rule);
        for (String warning : warnings(zoo)) {
            System.out.println("   " + Ansi.paint("! " + warning, Ansi.YELLOW));
        }
    }

    // EFFECTS: returns short warnings about hungry, unhappy, sick or unsettled animals, dirty enclosures
    //          and a food store too small to feed everyone
    static List<String> warnings(Zoo zoo) {
        List<String> warnings = new ArrayList<>();
        addWarning(warnings, zoo.getAnimals(), Animal::isHungry, "hungry");
        addWarning(warnings, zoo.getAnimals(), Animal::isUnhappy, "unhappy");
        addWarning(warnings, zoo.getAnimals(), Animal::isSick, "sick (try the hospital)");
        addWarning(warnings, zoo.getAnimals(), a -> !a.isSettled(), "not in a proper enclosure (earning nothing)");
        List<String> dirty = new ArrayList<>();
        for (Enclosure e : zoo.getEnclosures()) {
            if (e.isDirty()) {
                dirty.add(e.getName());
            }
        }
        if (!dirty.isEmpty()) {
            warnings.add("Dirty: " + String.join(", ", dirty));
        }
        int mealsNeeded = 0;
        for (Animal a : zoo.getAnimals()) {
            mealsNeeded += a.isInHospital() ? 0 : zoo.getMealSize(a);
        }
        if (zoo.getFood() < mealsNeeded) {
            warnings.add("Not enough food for everyone (need " + mealsNeeded + ")");
        }
        return warnings;
    }

    // MODIFIES: warnings
    // EFFECTS: adds "<names> is/are <problem>" for the animals (not in hospital) that have the problem
    private static void addWarning(List<String> warnings, List<Animal> animals, Predicate<Animal> problem,
                                   String description) {
        List<String> names = new ArrayList<>();
        for (Animal a : animals) {
            if (!a.isInHospital() && problem.test(a)) {
                names.add(a.getName());
            }
        }
        if (!names.isEmpty()) {
            warnings.add(String.join(", ", names) + (names.size() == 1 ? " is " : " are ") + description);
        }
    }

    // EFFECTS: returns a shop line with the species' price, appetite and appeal, dimmed if it's locked
    static String shopLabel(Species species, int zooLevel) {
        String details = padRight(species.getDisplayName(), 15) + padRight("$" + species.getPrice(), 7)
                + "eats " + species.getFoodPerMeal() + "  draws $" + species.getAppeal() + "/day";
        if (!species.isUnlockedAt(zooLevel)) {
            return Ansi.paint(details + "   [zoo level " + species.getRequiredLevel() + "]", Ansi.DIM);
        }
        return details;
    }

    // EFFECTS: prints what happened overnight
    static void printDayReport(DayReport report, Zoo zoo) {
        System.out.println("  " + Ansi.paint("The sun sets on day " + report.getDay() + "...", Ansi.MAGENTA, Ansi.BOLD));
        System.out.println("    Ticket sales   " + Ansi.paint("+$" + report.getTicketIncome(), Ansi.GREEN));
        System.out.println("    Upkeep         " + Ansi.paint("-$" + report.getUpkeep(), Ansi.RED));
        for (String event : report.getEvents()) {
            System.out.println("    " + Ansi.paint("* " + event, Ansi.CYAN));
        }
        System.out.println("  " + Ansi.paint("Good morning! Day " + zoo.getDay() + " begins with ", Ansi.BOLD)
                + money(zoo.getMoney()) + Ansi.paint(" in the bank.", Ansi.BOLD));
    }

    // EFFECTS: prints the game over screen
    static void printGameOver(Zoo zoo) {
        System.out.println();
        System.out.println("  " + Ansi.paint(repeat('#', WIDTH), Ansi.RED));
        System.out.println("  " + Ansi.paint("  BANKRUPT!  " + zoo.getName() + " ran out of money on day "
                + (zoo.getDay() - 1) + ".", Ansi.RED, Ansi.BOLD));
        System.out.println("  " + Ansi.paint("  The animals have been moved to other zoos. Game over.", Ansi.RED));
        System.out.println("  " + Ansi.paint(repeat('#', WIDTH), Ansi.RED));
    }

    // EFFECTS: returns amount formatted like "$1,250", in yellow (red if negative)
    static String money(int amount) {
        String text = (amount < 0 ? "-$" : "$") + String.format("%,d", Math.abs(amount));
        return Ansi.paint(text, amount < 0 ? Ansi.RED : Ansi.YELLOW, Ansi.BOLD);
    }

    // EFFECTS: prints a card for each animal, or a hint if there are none
    static void printAnimals(List<Animal> animals) {
        if (animals.isEmpty()) {
            System.out.println("  " + Ansi.paint("No animals yet... the zoo is very quiet.", Ansi.DIM));
            return;
        }
        for (Animal animal : animals) {
            printAnimalCard(animal);
        }
    }

    // EFFECTS: prints the animal's name, details and a bar for each condition stat
    static void printAnimalCard(Animal animal) {
        String home = animal.hasEnclosure() ? "lives in " + animal.getEnclosureName() : "has no home!";
        System.out.println("  " + Ansi.paint(animal.getName(), Ansi.BOLD, Ansi.MAGENTA)
                + " the " + animal.getSpecies() + Ansi.paint(" (age " + animal.getAge() + ")", Ansi.DIM)
                + "  -  " + home + "  -  feeling " + mood(animal));
        printStat("health", animal.getHealth());
        printStat("happiness", animal.getHappiness());
        printStat("fullness", animal.getHunger());
        System.out.println("      " + Ansi.paint(todayLine(animal), Ansi.DIM));
        System.out.println();
    }

    // EFFECTS: returns a line saying what the animal has done today, or that it's in the hospital
    private static String todayLine(Animal animal) {
        if (animal.isInHospital()) {
            return "In the hospital: back in " + plural(animal.getHospitalNights(), "night");
        }
        return "Today: " + (animal.hasPlayedToday() ? "played" : "not played yet")
                + ", " + (animal.hasRestedToday() ? "rested" : "not rested yet")
                + "  -  eats " + animal.getSpeciesInfo().getFoodPerMeal() + " food per meal";
    }

    // EFFECTS: prints each enclosure with its occupancy, cleanliness and residents
    static void printEnclosures(List<Enclosure> enclosures) {
        if (enclosures.isEmpty()) {
            System.out.println("  " + Ansi.paint("No enclosures yet. Time to build!", Ansi.DIM));
            return;
        }
        for (Enclosure e : enclosures) {
            String occupancy = e.getNumAnimals() + "/" + e.getCapacity() + " animals";
            System.out.println("  " + Ansi.paint(padRight(e.getName(), 20), Ansi.BOLD, Ansi.CYAN)
                    + padRight(occupancy, 13)
                    + "clean " + bar(e.getCleanliness(), Enclosure.MAX_CLEANLINESS)
                    + (e.isDirty() ? Ansi.paint("  DIRTY", Ansi.RED, Ansi.BOLD) : ""));
            System.out.println("      " + Ansi.paint(residents(e), Ansi.DIM));
        }
    }

    // EFFECTS: prints the happiest animal and the one most in need of care (or that they're all alike)
    static void printHighlights(Zoo zoo) {
        List<Animal> animals = new ArrayList<>(zoo.getAnimals());
        if (animals.isEmpty()) {
            return;
        }
        animals.sort(Comparator.comparingInt(Animal::getAverageCondition));
        Animal neediest = animals.get(0);
        Animal happiest = animals.get(animals.size() - 1);
        if (neediest.getAverageCondition() == happiest.getAverageCondition()) {
            System.out.println("  Everyone is feeling " + mood(happiest) + " today.");
            return;
        }
        System.out.println("  Star of the show:  " + Ansi.paint(happiest.getName(), Ansi.BOLD, Ansi.MAGENTA)
                + " (" + mood(happiest) + ")");
        System.out.println("  Needs some love:   " + Ansi.paint(neediest.getName(), Ansi.BOLD, Ansi.MAGENTA)
                + " (" + mood(neediest) + ")");
    }

    // EFFECTS: prints the most recent events from the event log, oldest first
    static void printDiary() {
        List<Event> events = new ArrayList<>();
        EventLog.getInstance().forEach(events::add);
        if (events.isEmpty()) {
            System.out.println("  " + Ansi.paint("Nothing has happened yet.", Ansi.DIM));
            return;
        }
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        for (Event event : events.subList(Math.max(0, events.size() - DIARY_ENTRIES), events.size())) {
            System.out.println("  " + Ansi.paint(time.format(event.getDate()), Ansi.DIM)
                    + "  " + event.getDescription());
        }
    }

    // EFFECTS: returns a one-word description of how the animal is feeling, coloured to match
    static String mood(Animal animal) {
        int condition = animal.getAverageCondition();
        if (condition >= 145) {
            return Ansi.paint("over the moon", Ansi.GREEN, Ansi.BOLD);
        } else if (condition >= 125) {
            return Ansi.paint("delighted", Ansi.GREEN);
        } else if (condition >= 100) {
            return Ansi.paint("content", Ansi.CYAN);
        } else if (condition >= 70) {
            return Ansi.paint("grumpy", Ansi.YELLOW);
        }
        return Ansi.paint("miserable", Ansi.RED);
    }

    // EFFECTS: returns the rating as filled and empty stars, e.g. "***.."
    static String stars(int rating) {
        return Ansi.paint(repeat('*', rating), Ansi.YELLOW, Ansi.BOLD)
                + Ansi.paint(repeat('.', Zoo.MAX_RATING - rating), Ansi.DIM);
    }

    // EFFECTS: returns a progress bar like "[#######.....]  70/150", green when high and red when low
    static String bar(int value, int max) {
        int filled = (int) Math.round((double) value / max * BAR_WIDTH);
        double ratio = (double) value / max;
        String colour = ratio >= 0.66 ? Ansi.GREEN : ratio >= 0.4 ? Ansi.YELLOW : Ansi.RED;
        return "[" + Ansi.paint(repeat('#', filled), colour) + Ansi.paint(repeat('.', BAR_WIDTH - filled), Ansi.DIM)
                + "] " + String.format("%3d/%d", value, max);
    }

    // EFFECTS: prints one labelled stat bar for an animal
    private static void printStat(String label, int value) {
        System.out.println("      " + padRight(label, 10) + bar(value, Animal.MAX_CONDITION));
    }

    // EFFECTS: returns a comma-separated list of the enclosure's residents
    private static String residents(Enclosure enclosure) {
        if (enclosure.isEmpty()) {
            return "(empty)";
        }
        List<String> names = new ArrayList<>();
        for (Animal a : enclosure.getAnimals()) {
            names.add(a.getName() + " the " + a.getSpecies());
        }
        return String.join(", ", names);
    }

    // EFFECTS: returns count followed by noun, pluralised when count isn't 1 (e.g. "1 animal", "3 animals")
    static String plural(int count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }

    private static String divider() {
        return Ansi.paint("   |   ", Ansi.DIM);
    }

    // EFFECTS: returns c repeated count times (empty if count <= 0)
    static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    // EFFECTS: returns text padded with spaces on the right to at least width characters
    static String padRight(String text, int width) {
        return text.length() >= width ? text + " " : text + repeat(' ', width - text.length());
    }
}
