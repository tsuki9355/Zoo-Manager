package ui;

import model.ActionResult;
import model.Animal;
import model.DayReport;
import model.Enclosure;
import model.Event;
import model.EventLog;
import model.Hospital;
import model.Species;
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
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
        this.setSize(1100, 720);
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

        JPanel functionsPanel = new JPanel(new GridLayout(0, 5, 5, 5));
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
        panel.add(makeButton("End the day", e -> handleEndDay()));
        panel.add(makeButton("Adopt animal", e -> handleAdoptAnimal()));
        panel.add(makeButton("Assign to enclosure", e -> handleAssignAnimalToEnclosure()));
        panel.add(makeButton("Evict from enclosure", e -> handleRemoveAnimalFromEnclosure()));
        panel.add(makeButton("Sell animal", e -> handleSellAnimal()));
        panel.add(makeButton("Feed", e -> handleCareForSelected(zoo::feedAnimal, "feed")));
        panel.add(makeButton("Feed all", e -> handleResult(zoo.feedAllAnimals(), "All animals are fed!")));
        panel.add(makeButton("Play", e -> handleCareForSelected(zoo::playAnimal, "play with")));
        panel.add(makeButton("Play all", e -> handleCount(zoo::playWithAllAnimals, "played")));
        panel.add(makeButton("Rest", e -> handleCareForSelected(zoo::restAnimal, "rest")));
        panel.add(makeButton("Rest all", e -> handleCount(zoo::restAllAnimals, "rested")));
        panel.add(makeButton("Send to hospital", e -> handleCareForSelected(zoo::sendToHospital, "send to hospital")));
        panel.add(makeButton("Build enclosure", e -> handleBuildEnclosure()));
        panel.add(makeButton("Expand enclosure", e -> handleExpandEnclosure()));
        panel.add(makeButton("Remove enclosure", e -> handleRemoveEnclosure()));
        panel.add(makeButton("Clean enclosures", e -> handleResult(zoo.cleanAllEnclosures(),
                "All enclosures have been cleaned!")));
        panel.add(makeButton("Buy food", e -> handleBuyFood()));
        panel.add(makeButton("Upgrade hospital", e -> handleUpgrade(zoo.getHospital().getUpgradeCost(),
                "Add a hospital bed", zoo::upgradeHospital)));
        panel.add(makeButton("Upgrade zoo", e -> handleUpgrade(zoo.getZooUpgradeCost(),
                "Upgrade the zoo to unlock new species", zoo::upgradeZoo)));
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

    // MODIFIES: this
    // EFFECTS: shows successMessage if result is a success, otherwise why it failed; then refreshes
    private void handleResult(ActionResult result, String successMessage) {
        refreshAll();
        show(result.isSuccess() ? successMessage : result.getMessage());
    }

    // MODIFIES: this, zoo
    // EFFECTS: ends the day, shows the overnight report, and handles bankruptcy
    private void handleEndDay() {
        DayReport report = zoo.endDay();
        StringBuilder text = new StringBuilder("Day " + report.getDay() + " is over.\n\n");
        text.append("Ticket sales: +$").append(report.getTicketIncome()).append('\n');
        text.append("Upkeep: -$").append(report.getUpkeep()).append('\n');
        for (String event : report.getEvents()) {
            text.append("\n* ").append(event);
        }
        text.append("\n\nDay ").append(zoo.getDay()).append(" begins with $").append(zoo.getMoney()).append('.');
        refreshAll();
        show(text.toString());
        if (zoo.isBankrupt()) {
            handleBankruptcy();
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: tells the user the zoo is bankrupt and lets them load their save or start a new zoo
    private void handleBankruptcy() {
        Object[] options = {"Load last save", "Start a new zoo"};
        int choice = JOptionPane.showOptionDialog(this, zoo.getName() + " has gone bankrupt. Game over!",
                "Bankrupt", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            handleLoadZoo();
        }
        if (zoo.isBankrupt()) {
            zoo = new Zoo(zoo.getName());
            zoo.getOrCreateWaitingRoom();
            refreshAll();
            show("A fresh start! You have $" + zoo.getMoney() + ".");
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: lets the user pick an unlocked species and a name, then adopts it into the Waiting Room
    private void handleAdoptAnimal() {
        JComboBox<Species> speciesBox = new JComboBox<>();
        for (Species s : Species.catalog()) {
            if (s.isUnlockedAt(zoo.getLevel())) {
                speciesBox.addItem(s);
            }
        }
        speciesBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                Species s = (Species) value;
                String text = s == null ? "" : s.getDisplayName() + "  -  $" + s.getPrice() + ", eats "
                        + s.getFoodPerMeal() + ", draws $" + s.getAppeal() + "/day";
                return super.getListCellRendererComponent(list, text, index, selected, focused);
            }
        });
        JTextField nameField = new JTextField(12);
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Species (more unlock with zoo upgrades):"));
        panel.add(speciesBox);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Adopt an animal ($" + zoo.getMoney() + " available)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        String name = nameField.getText().trim();
        if (result == JOptionPane.OK_OPTION && !name.isEmpty()) {
            handleResult(zoo.adoptAnimal((Species) speciesBox.getSelectedItem(), name),
                    name + " has arrived in the Waiting Room! Assign them to an enclosure so visitors can see them.");
        }
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
            handleResult(zoo.assignAnimalToEnclosure(animalName, enclosureName),
                    "Assigned '" + animalName + "' to enclosure '" + enclosureName + "'.");
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

    // MODIFIES: this, zoo
    // EFFECTS: after confirming, sells the selected animal for part of its price
    private void handleSellAnimal() {
        Animal selected = animalList.getSelectedValue();
        if (selected == null) {
            show("Select an animal to sell.");
            return;
        }
        int price = zoo.getSalePrice(selected);
        int answer = JOptionPane.showConfirmDialog(this, "Sell " + selected.getName() + " for $" + price + "?",
                "Sell animal", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            handleResult(zoo.sellAnimal(selected.getName()), "Goodbye, " + selected.getName() + "! +$" + price);
        }
    }

    // MODIFIES: this, the selected animal
    // EFFECTS: applies care to the selected animal and reports its new condition (or why it failed)
    private void handleCareForSelected(Function<String, ActionResult> care, String action) {
        Animal selected = animalList.getSelectedValue();
        if (selected == null) {
            show("Select an animal to " + action + ".");
            return;
        }
        handleResult(care.apply(selected.getName()), selected.getName() + " is now at  health "
                + selected.getHealth() + ",  happiness " + selected.getHappiness()
                + ",  hunger " + selected.getHunger() + (selected.isInHospital() ? "  (in hospital)" : ""));
    }

    // MODIFIES: this, every animal
    // EFFECTS: applies care to every animal that can have it today and says how many it helped
    private void handleCount(Supplier<Integer> care, String verb) {
        int count = care.get();
        refreshAll();
        show(count == 0 ? "Nobody needed that right now (once per animal per day)." : count + " animal(s) " + verb + ".");
    }

    // MODIFIES: this, zoo
    // EFFECTS: builds an enclosure with a name the user types
    private void handleBuildEnclosure() {
        String name = ask("A new enclosure costs $" + Zoo.ENCLOSURE_PRICE + " (+$" + Zoo.ENCLOSURE_UPKEEP
                + "/day upkeep).\nEnter new enclosure name:");
        if (name != null) {
            handleResult(zoo.buildEnclosure(name), "Enclosure '" + name + "' built!");
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: pays to expand the selected enclosure
    private void handleExpandEnclosure() {
        Enclosure selected = enclosureList.getSelectedValue();
        if (selected == null) {
            show("Select an enclosure to expand.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Add 2 spaces to '" + selected.getName() + "' for $"
                + selected.getExpansionCost() + "?", "Expand enclosure", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            handleResult(zoo.expandEnclosure(selected.getName()),
                    "'" + selected.getName() + "' now holds " + selected.getCapacity() + " animals.");
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: remove the selected enclosure
    private void handleRemoveEnclosure() {
        Enclosure selected = enclosureList.getSelectedValue();
        if (selected == null) {
            show("Select an enclosure to remove.");
        } else if (!selected.isEmpty()) {
            show("Cannot remove. Enclosure is not empty.");
        } else if (!zoo.removeEnclosure(selected.getName())) {
            show("The Waiting Room can't be removed.");
        } else {
            refreshAll();
            show("Enclosure '" + selected.getName() + "' was removed!");
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: asks how many packs of food to buy, then buys them
    private void handleBuyFood() {
        String answer = ask("Food store: " + zoo.getFood() + ". A pack of " + Zoo.FOOD_PACK_SIZE + " food costs $"
                + Zoo.FOOD_PACK_PRICE + ".\nHow many packs?");
        if (answer == null) {
            return;
        }
        try {
            int packs = Integer.parseInt(answer);
            handleResult(zoo.buyFood(packs), "Bought " + packs * Zoo.FOOD_PACK_SIZE + " food.");
        } catch (NumberFormatException e) {
            show("Please enter a whole number.");
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: confirms and performs an upgrade
    private void handleUpgrade(int cost, String description, Supplier<ActionResult> upgrade) {
        int answer = JOptionPane.showConfirmDialog(this, description + " for $" + cost + "?", "Upgrade",
                JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            handleResult(upgrade.get(), "Upgrade complete!");
        }
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
    // EFFECTS: update the zoo's status line and its warnings
    private void updateStatus() {
        Hospital hospital = zoo.getHospital();
        long homeless = zoo.getAnimals().stream().filter(a -> !a.hasEnclosure()).count();
        String status = "Day " + zoo.getDay() + " | Level " + zoo.getLevel() + "/" + Zoo.MAX_LEVEL
                + " | Money: $" + zoo.getMoney() + " | Food: " + zoo.getFood()
                + " | Animals: " + zoo.getNumAnimals()
                + (homeless > 0 ? " (" + homeless + " without an enclosure)" : "")
                + " | Hospital: " + hospital.getPatients().size() + "/" + hospital.getCapacity()
                + " | Rating: " + zoo.getRating() + "/" + Zoo.MAX_RATING
                + " | Tonight: +$" + zoo.getTicketIncome() + " / -$" + zoo.getUpkeep();
        List<String> warnings = ZooView.warnings(zoo);
        String warningText = warnings.isEmpty() ? "All good!" : String.join("  •  ", warnings);
        statusLabel.setText("<html>" + status + "<br><font color='#b04000'>" + escape(warningText) + "</font></html>");
    }

    // EFFECTS: returns text with HTML special characters escaped
    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
