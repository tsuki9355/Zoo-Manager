package ui;

import model.Animal;
import model.Enclosure;
import model.Event;
import model.EventLog;
import model.Zoo;
import persistence.JsonReader;
import persistence.JsonWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

public class ZooManageAppGUI extends JFrame {

    private static final String JSON_STORE = "./data/Zoo.json";
    private static final String DEFAULT_ZOO_NAME = "My Zoo";

    private final JsonWriter jsonWriter;
    private final JsonReader jsonReader;

    private Zoo zoo;

    private DefaultListModel<Enclosure> enclosureListModel;
    private JList<Enclosure> enclosureList;

    private DefaultListModel<Animal> animalListModel;
    private JList<Animal> animalList;

    private JLabel zooNameLabel;
    private JLabel statusLabel;

    private ImageIcon zooIcon;

    // Creates the Zoo managing gui game!
    public ZooManageAppGUI() {
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);
        init();
    }

    // MODIFIES: this, zoo
    // EFFECTS: initialize the zoo
    public void init() {
        zooIcon = new ImageIcon("./lib/cute.jpg");
        this.setIconImage(zooIcon.getImage());
        String name = ask("Enter a name for your zoo!");
        zoo = new Zoo(name == null ? DEFAULT_ZOO_NAME : name);
        zoo.getOrCreateWaitingRoom();
        initGUI(zoo.getName());
    }

    // MODIFIES: this
    // EFFECTS: creates the GUI of zoo managing game
    public void initGUI(String name) {
        JPanel root = windowLayoutMain(name);
        topPanel(name, root);
        center(root);
        bottomPanel(root);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                printEventLog();
            }
        });
    }

    // MODIFIES: this
    // EFFECTS: create the main window layout with the title name
    public JPanel windowLayoutMain(String name) {
        this.setTitle(name);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setSize(1100, 650);
        this.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(150, 255, 150));
        this.setContentPane(root);
        return root;
    }

    // MODIFIES: this
    // EFFECTS: create the top panel and add it to root
    public void topPanel(String name, JPanel root) {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        zooNameLabel = new JLabel(name);
        zooNameLabel.setFont(zooNameLabel.getFont().deriveFont(Font.BOLD, 20f));
        displayImage();
        JPanel zooNamePanel = new JPanel(new BorderLayout());
        zooNamePanel.setBorder(BorderFactory.createTitledBorder("Zoo Name"));
        zooNamePanel.add(zooNameLabel, BorderLayout.CENTER);
        zooNamePanel.setPreferredSize(new Dimension(180, 60));
        topPanel.add(zooNamePanel, BorderLayout.WEST);

        JPanel functionsPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        functionsPanel.setBorder(BorderFactory.createTitledBorder("List of functions to choose from"));
        functionsPanel.setOpaque(false);
        addFunctionButtons(functionsPanel);
        topPanel.add(functionsPanel, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);
    }

    // MODIFIES: this
    // EFFECTS: create the center panel and add it to root
    public void center(JPanel root) {
        enclosureListModel = new DefaultListModel<>();
        enclosureList = new JList<>(enclosureListModel);
        enclosureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane enclosureScroll = new JScrollPane(enclosureList);
        enclosureScroll.setBorder(BorderFactory.createTitledBorder("List of Enclosures"));

        animalListModel = new DefaultListModel<>();
        animalList = new JList<>(animalListModel);
        JScrollPane animalScroll = new JScrollPane(animalList);
        animalScroll.setBorder(BorderFactory.createTitledBorder("List of animals in the selected enclosure"));

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, enclosureScroll, animalScroll);
        centerSplit.setResizeWeight(0.3);
        root.add(centerSplit, BorderLayout.CENTER);

        enclosureList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateAnimalList();
            }
        });
    }

    // MODIFIES: this
    // EFFECTS: create the bottom panel and add it to root
    public void bottomPanel(JPanel root) {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setBorder(BorderFactory.createTitledBorder("Zoo status"));
        statusLabel = new JLabel();
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        root.add(statusPanel, BorderLayout.SOUTH);
        refreshAll();
        setVisible(true);
    }

    // MODIFIES: this
    // EFFECTS: add all the function buttons to the given panel
    private void addFunctionButtons(JPanel panel) {
        panel.add(makeButton("Add enclosure", e -> handleAddEnclosure()));
        panel.add(makeButton("Remove enclosure", e -> handleRemoveEnclosure()));
        panel.add(makeButton("Add animal", e -> handleAddAnimal()));
        panel.add(makeButton("Assign to enclosure", e -> handleAssignAnimalToEnclosure()));
        panel.add(makeButton("Remove from enclosure", e -> handleRemoveAnimalFromEnclosure()));
        panel.add(makeButton("Clean enclosures", e -> {
            zoo.cleanAllEnclosures();
            refreshAll();
            show("All enclosures have been cleaned!");
        }));
        panel.add(makeButton("Feed", e -> handleCareForSelected(Animal::feed, "feed")));
        panel.add(makeButton("Feed all", e -> handleCareForAll(zoo::feedAllAnimals, "All animals are fed!")));
        panel.add(makeButton("Play", e -> handleCareForSelected(Animal::play, "play with")));
        panel.add(makeButton("Play all", e -> handleCareForAll(zoo::playWithAllAnimals, "You played with everyone!")));
        panel.add(makeButton("Sleep", e -> handleCareForSelected(Animal::sleep, "put to sleep")));
        panel.add(makeButton("Sleep all", e -> handleCareForAll(zoo::sleepAllAnimals, "All animals have slept!")));
        panel.add(makeButton("Treat", e -> handleCareForSelected(Animal::treat, "treat")));
        panel.add(makeButton("Increase income", e -> {
            zoo.increaseIncome();
            updateStatus();
            show("Income increased! We now have $" + zoo.getMoney());
        }));
        panel.add(makeButton("Save", e -> handleSaveZoo()));
        panel.add(makeButton("Load", e -> handleLoadZoo()));
    }

    // EFFECTS: create a new button with given text and listener and returns it
    private JButton makeButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.addActionListener(listener);
        b.setFocusable(false);
        return b;
    }

    // MODIFIES: this, zoo
    // EFFECTS: add enclosure to the zoo and show it
    private void handleAddEnclosure() {
        String name = ask("Enter new enclosure name:");
        if (name == null) {
            return;
        }
        if (!zoo.addEnclosure(name)) {
            show("An enclosure called '" + name + "' already exists.");
            return;
        }
        refreshAll();
        show("Enclosure '" + name + "' added!");
    }

    // MODIFIES: this, zoo
    // EFFECTS: remove the selected enclosure
    private void handleRemoveEnclosure() {
        Enclosure selected = enclosureList.getSelectedValue();
        if (selected == null) {
            show("Select an enclosure to remove.");
        } else if (!selected.isEmpty()) {
            show("Cannot remove. Enclosure is not empty.");
        } else {
            zoo.removeEnclosure(selected.getName());
            refreshAll();
            show("Enclosure '" + selected.getName() + "' was removed!");
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: add an animal and assign it to Waiting Room
    private void handleAddAnimal() {
        if (zoo.getOrCreateWaitingRoom().isFull()) {
            show("Waiting Room capacity is max, please assign some animals to other enclosures.");
            return;
        }
        String species = ask("Enter animal's species:");
        String name = species == null ? null : ask("Enter animal's name:");
        String ageText = name == null ? null : ask("Enter animal's age:");
        if (ageText == null) {
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException ex) {
            show("Age must be an integer.");
            return;
        }
        if (!zoo.addAnimal(species, name, age)) {
            show("An animal called '" + name + "' already exists.");
            return;
        }
        zoo.assignAnimalToEnclosure(name, Zoo.WAITING_ROOM);
        refreshAll();
        show("Animal added to Waiting Room!");
    }

    // MODIFIES: this, zoo
    // EFFECTS: lets the user pick an animal (including ones without an enclosure) and an enclosure
    //          from drop-downs, then moves the animal there
    private void handleAssignAnimalToEnclosure() {
        if (zoo.getNumAnimals() == 0) {
            show("There are no animals yet.");
            return;
        }
        JComboBox<String> animalBox = new JComboBox<>();
        zoo.getAnimals().forEach(a -> animalBox.addItem(a.getName()));
        JComboBox<String> enclosureBox = new JComboBox<>();
        zoo.getEnclosures().forEach(e -> enclosureBox.addItem(e.getName()));
        Animal selected = animalList.getSelectedValue();
        if (selected != null) {
            animalBox.setSelectedItem(selected.getName());
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Animal:"));
        panel.add(animalBox);
        panel.add(new JLabel("Enclosure:"));
        panel.add(enclosureBox);
        int result = JOptionPane.showConfirmDialog(this, panel, "Assign animal to enclosure",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String animalName = (String) animalBox.getSelectedItem();
            String enclosureName = (String) enclosureBox.getSelectedItem();
            show(describe(zoo.assignAnimalToEnclosure(animalName, enclosureName), animalName, enclosureName));
            refreshAll();
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: takes the selected animal out of its enclosure, leaving it without one
    private void handleRemoveAnimalFromEnclosure() {
        Animal selected = animalList.getSelectedValue();
        if (selected == null) {
            show("Select an animal to remove from its enclosure.");
            return;
        }
        String enclosureName = selected.getEnclosureName();
        zoo.removeAnimalFromEnclosure(selected.getName(), enclosureName);
        refreshAll();
        show("Removed '" + selected.getName() + "' from '" + enclosureName
                + "'. Use 'Assign to enclosure' to give it a new home.");
    }

    // EFFECTS: returns a message describing the outcome of an assignment
    private String describe(Zoo.AssignResult result, String animalName, String enclosureName) {
        switch (result) {
            case ASSIGNED:
                return "Assigned '" + animalName + "' to enclosure '" + enclosureName + "'.";
            case NO_SUCH_ANIMAL:
                return "No such animal.";
            case NO_SUCH_ENCLOSURE:
                return "No such enclosure.";
            case ALREADY_THERE:
                return "This animal is already in that enclosure!";
            default:
                return "Enclosure is full already!";
        }
    }

    // MODIFIES: this, the selected animal
    // EFFECTS: applies care to the selected animal and reports its new condition
    private void handleCareForSelected(Consumer<Animal> care, String action) {
        Animal selected = animalList.getSelectedValue();
        if (selected == null) {
            show("Select an animal to " + action + ".");
            return;
        }
        care.accept(selected);
        refreshAll();
        show(selected.getName() + " is now at  health " + selected.getHealth()
                + ",  happiness " + selected.getHappiness() + ",  hunger " + selected.getHunger());
    }

    // MODIFIES: this, every animal
    // EFFECTS: applies care to every animal in the zoo and shows message
    private void handleCareForAll(Runnable care, String message) {
        if (zoo.getNumAnimals() == 0) {
            show("There are no animals yet.");
            return;
        }
        care.run();
        refreshAll();
        show(message);
    }

    // EFFECTS: save the current zoo as JSON files
    private void handleSaveZoo() {
        try {
            jsonWriter.save(zoo);
            show("Saved " + zoo.getName() + " to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            show("Unable to write to file: " + JSON_STORE);
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: read the JSON File saved file and recreate the saved zoo
    private void handleLoadZoo() {
        try {
            zoo = jsonReader.read();
            zoo.getOrCreateWaitingRoom();
            zooNameLabel.setText(zoo.getName());
            setTitle(zoo.getName());
            refreshAll();
            if (!enclosureListModel.isEmpty()) {
                enclosureList.setSelectedIndex(0);
            }
            show("Loaded " + zoo.getName() + " from " + JSON_STORE);
        } catch (IOException e) {
            show("Unable to read from file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: refresh the enclosure list (keeping the selection where possible), animal list and status
    private void refreshAll() {
        Enclosure selected = enclosureList.getSelectedValue();
        enclosureListModel.clear();
        for (Enclosure e : zoo.getEnclosures()) {
            enclosureListModel.addElement(e);
        }
        if (selected != null && enclosureListModel.contains(selected)) {
            enclosureList.setSelectedValue(selected, true);
        }
        updateAnimalList();
        updateStatus();
    }

    // MODIFIES: this
    // EFFECTS: update the animal list to show the selected enclosure's animals
    private void updateAnimalList() {
        animalListModel.clear();
        Enclosure selected = enclosureList.getSelectedValue();
        if (selected != null) {
            for (Animal a : selected.getAnimals()) {
                animalListModel.addElement(a);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: update the zoo's status
    private void updateStatus() {
        long homeless = zoo.getAnimals().stream().filter(a -> !a.hasEnclosure()).count();
        statusLabel.setText("Money: $" + zoo.getMoney()
                + " | Enclosures: " + zoo.getNumEnclosures()
                + " | Animals: " + zoo.getNumAnimals()
                + (homeless > 0 ? " (" + homeless + " without an enclosure)" : "")
                + " | Rating: " + zoo.getRating() + "/" + Zoo.MAX_RATING);
    }

    // EFFECTS: shows prompt in a dialog and returns the trimmed answer, or null if cancelled or blank
    private String ask(String prompt) {
        String answer = JOptionPane.showInputDialog(this, prompt);
        return answer == null || answer.trim().isEmpty() ? null : answer.trim();
    }

    // EFFECTS: shows message in a dialog
    private void show(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    // EFFECTS: display the image
    private void displayImage() {
        if (zooIcon != null) {
            Image scaled = zooIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            zooNameLabel.setIcon(new ImageIcon(scaled));
            zooNameLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            zooNameLabel.setIconTextGap(10);
        }
    }

    // EFFECTS: prints all events in the EventLog to the console
    private void printEventLog() {
        for (Event next : EventLog.getInstance()) {
            System.out.println(next.toString());
        }
    }
}
