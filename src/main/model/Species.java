package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// The species a zoo can adopt. Each has an adoption price, the zoo level needed to unlock it, how much food
// one meal uses, and how much ticket money it draws in per day while on display.
public enum Species {
    MEERKAT("Meerkat", 100, 1, 1, 20),
    CAPYBARA("Capybara", 120, 1, 1, 22),
    PENGUIN("Penguin", 150, 1, 1, 26),
    FLAMINGO("Flamingo", 180, 1, 1, 30),
    KANGAROO("Kangaroo", 350, 2, 2, 50),
    ZEBRA("Zebra", 400, 2, 2, 55),
    RED_PANDA("Red Panda", 500, 2, 1, 65),
    GIRAFFE("Giraffe", 600, 2, 3, 75),
    HIPPO("Hippo", 800, 3, 4, 95),
    LION("Lion", 900, 3, 3, 110),
    GORILLA("Gorilla", 950, 3, 2, 115),
    POLAR_BEAR("Polar Bear", 1000, 3, 3, 120),
    KOMODO_DRAGON("Komodo Dragon", 1600, 4, 2, 180),
    ELEPHANT("Elephant", 1800, 4, 5, 200),
    WHITE_TIGER("White Tiger", 2000, 4, 3, 220),
    GIANT_PANDA("Giant Panda", 2200, 4, 2, 250),
    // Used for animals from older saves whose species isn't in the catalog; it can never be adopted
    UNKNOWN("Unknown", 0, Integer.MAX_VALUE, 1, 20);

    private final String displayName;
    private final int price;
    private final int requiredLevel;
    private final int foodPerMeal;
    private final int appeal;

    Species(String displayName, int price, int requiredLevel, int foodPerMeal, int appeal) {
        this.displayName = displayName;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.foodPerMeal = foodPerMeal;
        this.appeal = appeal;
    }

    // EFFECTS: returns every species that can be adopted, cheapest tier first
    public static List<Species> catalog() {
        List<Species> catalog = new ArrayList<>(Arrays.asList(values()));
        catalog.remove(UNKNOWN);
        return catalog;
    }

    // EFFECTS: returns the catalog species with the given display name (ignoring case), or UNKNOWN
    public static Species fromName(String name) {
        for (Species species : values()) {
            if (species.displayName.equalsIgnoreCase(name)) {
                return species;
            }
        }
        return UNKNOWN;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPrice() {
        return price;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getFoodPerMeal() {
        return foodPerMeal;
    }

    public int getAppeal() {
        return appeal;
    }

    // EFFECTS: returns true if a zoo at the given level may adopt this species
    public boolean isUnlockedAt(int zooLevel) {
        return zooLevel >= requiredLevel;
    }
}
