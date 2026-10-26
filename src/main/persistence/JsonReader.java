package persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

import model.Animal;
import model.Enclosure;
import model.Zoo;

// Represents a reader that reads a zoo from JSON data stored in a file
public class JsonReader {
    private final String source;

    // EFFECTS: constructs reader to read from source file
    public JsonReader(String source) {
        this.source = source;
    }

    // EFFECTS: reads zoo from file and returns it;
    //          throws IOException if the file can't be read or doesn't contain a valid zoo
    public Zoo read() throws IOException {
        String jsonData = new String(Files.readAllBytes(Paths.get(source)), StandardCharsets.UTF_8);
        try {
            return parseZoo(new JSONObject(jsonData));
        } catch (JSONException e) {
            throw new IOException("Invalid zoo data in " + source, e);
        }
    }

    // EFFECTS: parses zoo from JSON object and returns it; enclosures are rebuilt first so each
    //          animal can then be placed into the enclosure it names. Fields added in later versions
    //          fall back to a new zoo's values, so older save files still load.
    private Zoo parseZoo(JSONObject json) {
        Zoo zoo = new Zoo(json.getString("Name"));
        zoo.setMoney(json.getInt("Money"));
        zoo.setFood(json.optInt("Food", Zoo.INITIAL_FOOD));
        zoo.setDay(json.optInt("Day", 1));
        zoo.setLevel(json.optInt("Level", 1));
        zoo.getHospital().setLevel(json.optInt("HospitalLevel", 1));
        for (Object enclosureJson : json.getJSONArray("Enclosures")) {
            zoo.addEnclosure(parseEnclosure((JSONObject) enclosureJson));
        }
        for (Object animalJson : json.getJSONArray("Animals")) {
            addAnimal(zoo, (JSONObject) animalJson);
        }
        return zoo;
    }

    // EFFECTS: parses an (empty) enclosure from JSON object and returns it
    private Enclosure parseEnclosure(JSONObject json) {
        Enclosure enclosure = new Enclosure(json.getString("Name"));
        enclosure.setCapacity(json.getInt("Capacity"));
        enclosure.setCleanliness(json.getInt("Cleaness"));
        return enclosure;
    }

    // MODIFIES: zoo
    // EFFECTS: parses an animal from JSON object, adds it to zoo and places it in its enclosure (if any)
    private void addAnimal(Zoo zoo, JSONObject json) {
        Animal animal = new Animal(json.getString("Species"), json.getString("Name"), json.getInt("Age"));
        animal.setHealth(json.getInt("Health"));
        animal.setHappiness(json.getInt("Happiness"));
        animal.setHunger(json.getInt("Hunger"));
        animal.setDoneToday(json.optBoolean("PlayedToday"), json.optBoolean("RestedToday"));
        zoo.addAnimal(animal);

        Enclosure enclosure = zoo.findEnclosure(json.optString("Enclosure", ""));
        if (enclosure != null) {
            enclosure.addAnimal(animal);
        }
        zoo.readmitPatient(animal, json.optInt("HospitalNights", 0));
    }
}
