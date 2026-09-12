package ui;

import model.*;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ZooManageApp {
    private static final String JSON_STORE = "./data/Zoo.json";

    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    private Scanner input;
    private Zoo zoo;
    private Enclosure defaultEnclosure;

    private boolean keepGoing;

    // EFFECTS: run the zoo managing game!
    public ZooManageApp() {
        defaultEnclosure = new Enclosure("Waiting Room");
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        run();
    }

    // MODIFIES: this
    // EFFECTS: display the intro messages and then continuosly runs the zoo managing game until the user quit
    public void run() {
        input = new Scanner(System.in);
        System.out.println("WELCOME TO ZOO MANAGER");
        System.out.println("I'm your imaginary guide that will guide you");

        String zooName = getInput("To get started, first enter a name for your zoo: ");
        this.zoo = new Zoo(zooName);
        System.out.println("Spectacular!");
        System.out.println("Now enjoy this small zoo managing game!");
        zoo.addEnclosure(defaultEnclosure);
        keepGoing = true;
        while (keepGoing) {
            showOptions();
            int choice = getInt("Choose an option!\n");
            if (choice < 10) {
                singleDigitChoice(choice);
                if (keepGoing == false) {
                    break;
                }
            } else if (choice >= 10) {
                doubleDigitChoice(choice);
            }
        }
        System.out.println("Goodbye!");
    }
    
    // MODIFIES: this
    // EFFECTS: run the single digit command from the menu based on the user's choice
    private void singleDigitChoice(int choice) {
        switch (choice) {
            case 0 : keepGoing = false;
                     break;
            case 1 : addEnclosure(); 
                     break;
            case 2 : removeEnclosure(); 
                     break;
            case 3 : listEnclosures(); 
                     break;
            case 4 : addAnimal(); 
                     break;
            case 5 : listAnimals(); 
                     break;
            case 6 : assignAnimalToEnclosure(); 
                     break;
            case 7 : removeAnimalFromEnclosure(); 
                     break;
            case 8 : feedOneAnimal(); 
                     break;
            case 9 : feedAllAnimals(); 
                     break;
        }
    }

    // MODIFIES: this
    // EFFECTS: run the double digit command from the menu based on the user's choice
    private void doubleDigitChoice(int choice) {
        switch (choice) {
            case 10 : playOneAnimal(); 
                      break;
            case 11 : playAllAnimals(); 
                      break;
            case 12 : sleepOneAnimal(); 
                      break;
            case 13 : sleepAllAnimals(); 
                      break;
            case 14 : treatAnimal(); 
                      break;
            case 15 : cleanAllEnclosure(); 
                      break;
            case 16 : increaseIncome(); 
                      break;
            case 17 : showZooStatus(); 
                      break;
            case 18 : saveZoo();
                      break;
            case 19 : loadZoo();
                      break;
        }
    }

    // EFFECTS: Shows a list of options that the user can choose to run
    private void showOptions() {
        String zooName = zoo.getZooName();
        System.out.println("----------------------" + zooName + "----------------------");
        System.out.println("1.  Add enclosure");
        System.out.println("2.  Remove enclosure");
        System.out.println("3.  List enclosures");
        System.out.println("4.  Add animal");
        System.out.println("5.  List animals");
        System.out.println("6.  Assign animal -> enclosure");
        System.out.println("7.  Remove animal from enclosure");
        System.out.println("8.  Feed one animal (+10 happiness)");
        System.out.println("9.  Feed all animals (+10 happiness on all animals)");
        System.out.println("10. Play one animal (+15 happiness)");
        System.out.println("11. Play all animal (+15 happiness on all animals)");
        System.out.println("12. Put one animal to sleep (+ 5 health)");
        System.out.println("13. Put all animals to sleep (+5 health on all animals)");
        System.out.println("14. Treat animal (restore all condition to full)");
        System.out.println("15. Clean all enclosures (+2 cleaness on all enclosures)");
        System.out.println("16. Increase income");   
        System.out.println("17. Show Zoo Status");
        System.out.println("18. Save Zoo Status");
        System.out.println("19. Load Zoo Status");
        System.out.println("0.  Quit the game :(((");
        System.out.println("----------------------------------------------------------");
    }

    // MODIFIES: this
    // EFFECTS: add an enclosure to the zoo
    private void addEnclosure() {
        String name = getInput("Enter new enclosure name: ");
        zoo.addEnclosure(name);
        System.out.println("Enclosure '" + name + "' added!");
    }

    // MODIFIES: this
    // EFFECTS: remove an enclosure from the zoo if the enclosure have no animals
    private void removeEnclosure() {
        String name = getInput("Enter enclosure name to remove: ");
        Enclosure e = findEnclosureByName(name);
        if (e == null) {
            System.out.println("Enclosure with this name does not exist!");
            return;
        }
        if (e.getNumAnimal() > 0) {
            System.out.println("Cannot remove. Enclosure is not empty.");
            return;
        }
        zoo.removeEnclosure(name);
        System.out.println("Enclosure '" + name + "' was removed!");
    }

    // EFFECTS: display all the encloures along with the name, number of animals and the cleaness
    private void listEnclosures() {
        ArrayList<Enclosure> list = zoo.getAllEnclosure();
        if (list.isEmpty()) {
            System.out.println("No enclosures yet!");
            return;
        }
        for (Enclosure e : list) {
            System.out.printf("name: %s, animals: %d, cleaness: %d\n",
                    e.getName(), e.getNumAnimal(), e.getCleaness());
        }
    }

    // MODIFIES: this
    // EFFECTS: add an animal to the zoo
    private void addAnimal() {
        if (defaultEnclosure.getNumAnimal() == defaultEnclosure.getCapacity()) {
            System.out.println("Waiting Room capacity is max, please assigned some animals to some enclosure");
            return;
        }
        String species = getInput("Enter animal's species: ");
        String name = getInput("Enter animal's name: ");
        int age = getInt("Enter animal's age: \n");
        zoo.addAnimal(species, name, age);
        zoo.assignAnimalToEnclosure(name, "Waiting Room");
        System.out.println("Animal added!");
    }

    // EFFECTS: display all the animals along with their species, name, age, health,
    //          happiness, and hunger
    private void listAnimals() {
        ArrayList<Animal> list = zoo.getAllAnimals();
        if (list.isEmpty()) {
            System.out.println("No animals yet!");
            return;
        }
        System.out.println("Showing animal list");
        for (Animal a : list) {
            System.out.printf("species: %s, name: %s, age: %d, health: %d,",
                    a.getSpecie(), a.getName(), a.getAge(), a.getHealth());
            System.out.printf(" happiness: %d, hunger: %d.",
                    a.getHappinness(), a.getHunger());
            System.out.printf("\n");
        }
    }
    

    // MODIFIES: this
    // EFFECTS: assign an animal to an enclosure if animal and enclosure both exists and if the enclosure have space
    @SuppressWarnings("methodlength")
    private void assignAnimalToEnclosure() {
        String animalName = getInput("Enter the animal's name: ");
        String enclosureName = getInput("Enter the enclosure's name: ");
        Animal a = findAnimalByName(animalName);
        if (a.getEnclosure() != null) {
            Enclosure e = a.getEnclosure();
            e.removeAnimal(a);
            a.setToNoEnclosure();
        }
        if (a == null) { 
            System.out.println("No such animal :("); 
            return; 
        }
        Enclosure e = findEnclosureByName(enclosureName);
        if (e == null) { 
            System.out.println("No such enclosure :("); 
            return; 
        }
        if (!e.canAcceptAnimal()) {
            System.out.println("Enclosure is full already!!!");
            return;
        }
        if (e.getExist(a)) {
            System.out.println("This animal is already in that enclosure!");
            return;
        }
        e.addAnimal(a);
        a.assign(e);
        System.out.printf("Assigned '%s' to enclosure '%s'.\n", a.getName(), e.getName());
    }

    // MODIFIES: this
    // EFFECTS: remove an animal from enclosure if animal and enclosure both exist
    private void removeAnimalFromEnclosure() {
        String animalName = getInput("Enter animal name: ");
        String enclosureName = getInput("Enter enclosure name: ");

        Animal a = findAnimalByName(animalName);
        if (a == null) { 
            System.out.println("No such animal D:");
            return; 
        }
        Enclosure e = findEnclosureByName(enclosureName);
        if (e == null) {
            System.out.println("No such enclosure .-."); 
            return; 
        }
        if (!e.getExist(a)) {
            System.out.println("That animal is not in this enclosure.");
            return;
        }

        e.removeAnimal(a);
        a.setToNoEnclosure();
        System.out.printf("Removed '%s' from enclosure '%s'.\n", a.getName(), e.getName());
    }

    // MODIFIES: this
    // EFFECTS: feed an animal
    private void feedOneAnimal() {
        String name = getInput("Enter the animal name you want to feed: ");
        Animal a = findAnimalByName(name);
        if (a == null) {
            System.out.println("No such animal :C");
            return;
        }
        a.feed();
        System.out.printf("Fed '%s', hunger is now: %d :3\n", a.getName(), a.getHunger());
    }

    // MODIFIES: this
    // EFFECTS: feed all the animal
    private void feedAllAnimals() {
        zoo.feedAllAnimals();
        System.out.println("All animals are fed!");
    }

    // MODIFIES: this
    // EFFECTS: play with an animal
    private void playOneAnimal() {
        String name = getInput("Enter the animal name you want to play with: ");
        Animal a = findAnimalByName(name);
        if (a == null) {
            System.out.println("No such animal manager :(");
            return;
        }
        a.play();
        System.out.printf("played with '%s', happiness is now %d.\n", a.getName(), a.getHappinness());
    }

    // MODIFIES: this
    // EFFECTS: play with all the animals
    private void playAllAnimals() {
        zoo.playWithAllAnimals();
        System.out.println("You played will all animals!");
    }

    // MODIFIES: this
    // EFFECTS: put an animal to sleep
    private void sleepOneAnimal() {
        String name = getInput("Enter the animal name you want to put to sleep: ");
        Animal a = findAnimalByName(name);
        if (a == null) {
            System.out.println("No such animal boss!");
            return;
        }
        a.sleep();
        System.out.printf("'%s' have slept!, Health is now %d.\n", a.getName(), a.getHealth());
    }

    // MODIFIES: this
    // EFFECTS: put all animals to sleep
    private void sleepAllAnimals() {
        zoo.makeAllAnimalSleep();
        System.out.println("All animals have slept!");
    }

    // MODIFIES: this
    // EFFECTS: treat an animal to restore to full conditions
    private void treatAnimal() {
        String name = getInput("Enter the animal name you want to treat");
        Animal a = findAnimalByName(name);
        if (a == null) {
            System.out.println("No such animal -.-");
            return;
        }
        a.treatment();
        System.out.printf("'%s' have been treated and now have full condition!\n", a.getName());
    }

    // MODIFIES: this
    // EFFECTS: clean all the enclosure
    private void cleanAllEnclosure() {
        zoo.cleanAllEnclosure();
        System.out.println("All enclosures have been cleaned!");
    }

    // MODIFIES: this
    // EFFECTS: increase the zoo's income
    private void increaseIncome() {
        zoo.increaseIncome();
        System.out.println("Income increased! We now have $" + zoo.getZooMoney());
    }

    // EFFECTS: show the current statuses of the zoo
    private void showZooStatus() {
        System.out.println("Zoo name: " + zoo.getZooName());
        System.out.println("Money: $" + zoo.getZooMoney());
        System.out.println("Enclosures: " + zoo.getNumEnclosures());
        System.out.println("Animals: " + zoo.getNumAnimals());
    }

    // EFFECTS: find and return the animal by the given name
    private Animal findAnimalByName(String name) {
        for (Animal a : zoo.getAllAnimals()) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    // EFFECTS: find and return the enclosure by the given name
    private Enclosure findEnclosureByName(String name) {
        for (Enclosure e : zoo.getAllEnclosure()) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    // EFFECTS: print out the prompt as well as returning the user input
    private String getInput(String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = this.input.nextLine();
            if (input != null) {
                return input;
            }
            System.out.println("Please enter a value");
        }
    }

    // EFFECTS: print out the prompt as well as returning the user input as integers
    private int getInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = this.input.nextLine();
            return Integer.parseInt(input);
        }
    }

    private void saveZoo() {
        try {
            jsonWriter.open();
            jsonWriter.write(zoo);
            jsonWriter.close();
            System.out.println("Saved " + zoo.getZooName() + " to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_STORE);
        }
    }

    private void loadZoo() {
        try {
            zoo = jsonReader.read();
            System.out.println("Loaded " + zoo.getZooName() + " from " + JSON_STORE);
        } catch (IOException e) {
            System.out.println("Unable to read from file: " + JSON_STORE);
        }
    }
}
