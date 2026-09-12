package persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Animal;
import model.Enclosure;
import model.Zoo;

// Represents a reader that reads a zoo from JSON data stored in a file
public class JsonReader {
    private String source;

    // EFFECTS: constructs reader to read from source file
    public JsonReader(String source) {
        this.source = source;
    }

    // EFFECTS: read zoo from file and returns it
    // thorws IOException if an error occurs reading from file
    public Zoo read() throws IOException {
        String jsonData = readFile(source);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseZoo(jsonObject);
    }

    // EFFECTS: read source file as string and returns it
    private String readFile(String source) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(source), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s));
        }

        return contentBuilder.toString();
    }

    // EFFECTS: parses zoo from JSON object and returns it
    private Zoo parseZoo(JSONObject jsonObject) {
        String name = jsonObject.getString("Name");
        Zoo zoo = new Zoo(name);
        int money = jsonObject.getInt("Money");
        zoo.setMoney(money);
        addAnimals(zoo, jsonObject);
        addEnclosures(zoo, jsonObject);
        return zoo;
    }

    // MODIFIES: zoo
    // EFFECTS: parses animals from JSON object and adds them to zoo
    private void addAnimals(Zoo zoo, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("Animals");
        for (Object json : jsonArray) {
            JSONObject nextAnimal = (JSONObject) json;
            addAnimal(zoo, nextAnimal);
        }
    }

    // MODIFIES: zoo
    // EFFECTS: parses animal from JSON object and adds it to zoo
    private void addAnimal(Zoo zoo, JSONObject jsonObject) {
        String species = jsonObject.getString("Species");
        String name = jsonObject.getString("Name");
        int age = jsonObject.getInt("Age");
        int health = jsonObject.getInt("Health");
        int happiness = jsonObject.getInt("Happiness");
        int hunger = jsonObject.getInt("Hunger");
        Animal a =  new Animal(species, name, age);
        String enclosureName = jsonObject.getString("Enclosure");
        zoo.setEnclosureByName(a, enclosureName);
        a.setHappiness(happiness);
        a.setHealth(health);
        a.setHunger(hunger);
        zoo.addAnimal(a);
    }

    // MODIFIES: zoo
    // EFFECTS: parses enclosures from JSON object and adds them to zoo
    private void addEnclosures(Zoo zoo, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("Enclosures");
        for (Object json : jsonArray) {
            JSONObject nextEnclosure = (JSONObject) json;
            addEnclosure(zoo, nextEnclosure);
        }
    }

    // MODIFIES: zoo
    // EFFECTS: parses enclosure from JSON obejct and adds it to zoo
    public void addEnclosure(Zoo zoo, JSONObject jsonObject) {
        int numAnimals = jsonObject.getInt("numAnimals");
        int cleaness = jsonObject.getInt("Cleaness");
        String name = jsonObject.getString("Name");
        int capacity = jsonObject.getInt("Capacity");
        List<Animal> animals = getAnimalList(zoo, jsonObject);
        Enclosure e = new Enclosure(name);
        e.setCapacity(capacity);
        e.setCleaness(cleaness);
        e.setNumAnimals(numAnimals);
        e.setAnimals((ArrayList<Animal>) animals);
        zoo.addEnclosure(e);
    }

    // EFFECTS: parses a list of animals from JSON object and returns it
    public ArrayList<Animal> getAnimalList(Zoo zoo, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("Animals");
        ArrayList<Animal> animals = new ArrayList<>();
        for (Object json :  jsonArray) {
            JSONObject nextAnimal = (JSONObject) json;
            Animal a = returnAnimalJson(zoo, nextAnimal);
            animals.add(a);
        }
        return animals;
    }

    // EFFECTS: parses an animal from JSON object and returns it
    private Animal returnAnimalJson(Zoo zoo, JSONObject jsonObject) {
        String species = jsonObject.getString("Species");
        String name = jsonObject.getString("Name");
        int age = jsonObject.getInt("Age");
        int health = jsonObject.getInt("Health");
        int happiness = jsonObject.getInt("Happiness");
        int hunger = jsonObject.getInt("Hunger");
        Animal a =  new Animal(species, name, age);
        String enclosureName = jsonObject.getString("Enclosure");
        zoo.setEnclosureByName(a, enclosureName);
        a.setHappiness(happiness);
        a.setHealth(health);
        a.setHunger(hunger);
        return a;
    }
}
