package ui;

import model.Animal;
import model.Enclosure;
import model.Zoo;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import model.Event;
import model.EventLog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ZooManageAppGUI extends JFrame {

    private static final String JSON_STORE = "./data/Zoo.json";

    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    private Zoo zoo;
    private Enclosure defaultEnclosure;

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
        String name = JOptionPane.showInputDialog(
                    this,
                    "Enter a name for your zoo!",
                    "Create Zoo",
                    JOptionPane.QUESTION_MESSAGE
        );
        zoo = new Zoo(name);
        defaultEnclosure = new Enclosure("Waiting Room");
        zoo.addEnclosure(defaultEnclosure);
        initGUI(name);
    }

    // MODIFIES: this
    // EFFECTS: creates the GUI of zoo managing game 
    public void initGUI(String name) {
        JPanel root = windowLayoutMain(name);
        root = topPanel(name, root);
        root = center(root);
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
        this.setSize(900, 600);
        this.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(150, 255, 150));
        this.setContentPane(root);
        return root;
    }

    // MODIFIES: this
    // EFFECTS: create the top panel and add it to root
    public JPanel topPanel(String name, JPanel root) {
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

        JPanel functionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        functionsPanel.setBorder(BorderFactory.createTitledBorder("List of functions to choose from"));
        functionsPanel.setOpaque(false);
        functionsPanel.setPreferredSize(new Dimension(20, 100));
        addFunctionButtons(functionsPanel);
        topPanel.add(functionsPanel, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);
        return root;
    }

    // MODIFIES: this
    // EFFECTS: create the center panel and add it to root
    public JPanel center(JPanel root) {
        enclosureListModel = new DefaultListModel<Enclosure>();
        enclosureList = new JList<Enclosure>(enclosureListModel);
        enclosureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane enclosureScroll = new JScrollPane(enclosureList);
        enclosureScroll.setBorder(
                BorderFactory.createTitledBorder("List of Enclosures"));

        animalListModel = new DefaultListModel<Animal>();
        animalList = new JList<Animal>(animalListModel);

        JScrollPane animalScroll = new JScrollPane(animalList);
        animalScroll.setBorder(
                BorderFactory.createTitledBorder("List of animals in the selected enclosure"));
        JSplitPane centerSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, enclosureScroll, animalScroll);
        centerSplit.setResizeWeight(0.3);
        root.add(centerSplit, BorderLayout.CENTER);

        enclosureList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateAnimalList();
                }
            }
        });
        return root;
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

        refreshEnclosureList();
        updateStatus();

        setVisible(true);
    }

    // MODIFIES: this
    // EFFECTS: add all the function buttons to the given panel
    @SuppressWarnings("methodlength")
    private void addFunctionButtons(JPanel panel) {
        panel.add(makeButton("Add enclosure", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleAddEnclosure();
            }
        }));

        panel.add(makeButton("Remove enclosure", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleRemoveEnclosure();
            }
        }));

        panel.add(makeButton("Add animal", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleAddAnimal();
            }
        }));

        panel.add(makeButton("Assign animal -> enclosure", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleAssignAnimalToEnclosure();
            }
        }));

        panel.add(makeButton("Feed one", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleFeedOneAnimal();
            }
        }));

        panel.add(makeButton("Play one", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handlePlayOneAnimal();
            }
        }));

        panel.add(makeButton("Clean enclosures", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoo.cleanAllEnclosure();
                updateStatus();
                JOptionPane.showMessageDialog(ZooManageAppGUI.this,
                        "All enclosures have been cleaned!");
            }
        }));

        panel.add(makeButton("Increase income", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoo.increaseIncome();
                updateStatus();
                JOptionPane.showMessageDialog(ZooManageAppGUI.this,
                        "Income increased! We now have $" + zoo.getZooMoney());
            }
        }));

        panel.add(makeButton("Save", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleSaveZoo();
            }
        }));

        panel.add(makeButton("Load", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLoadZoo();
            }
        }));
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
        String name = JOptionPane.showInputDialog(this, "Enter new enclosure name:");
        zoo.addEnclosure(name.trim());
        refreshEnclosureList();
        updateStatus();
        JOptionPane.showMessageDialog(this, "Enclosure '" + name + "' added!");
    }

    // MODIFIES: this, zoo
    // EFFECTS: remove the selected enclosure
    private void handleRemoveEnclosure() {
        Enclosure selected = (Enclosure) enclosureList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an enclosure to remove.");
            return;
        }
        if (selected.getNumAnimal() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Cannot remove. Enclosure is not empty.");
            return;
        }
        zoo.removeEnclosure(selected.getName());
        refreshEnclosureList();
        updateStatus();
        JOptionPane.showMessageDialog(this,
                "Enclosure '" + selected.getName() + "' was removed!");
    }

    // MODIFIES: this, zoo
    // EFFECTS: add an animal and assign it to Waiting Room
    private void handleAddAnimal() {
        if (defaultEnclosure.getNumAnimal() == defaultEnclosure.getCapacity()) {
            JOptionPane.showMessageDialog(this,
                    "Waiting Room capacity is max, please assign some animals to other enclosures.");
            return;
        }
        String species = JOptionPane.showInputDialog(this, "Enter animal's species:");
        String name = JOptionPane.showInputDialog(this, "Enter animal's name:");
        String ageStr = JOptionPane.showInputDialog(this, "Enter animal's age:");
        int age;
        try {
            age = Integer.parseInt(ageStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be an integer.");
            return;
        }

        zoo.addAnimal(species.trim(), name.trim(), age);
        zoo.assignAnimalToEnclosure(name.trim(), "Waiting Room");
        refreshEnclosureList();
        updateStatus();
        updateAnimalList();
        JOptionPane.showMessageDialog(this, "Animal added to Waiting Room!");
    }

    // MODIFIES: this, zoo
    // EFFECTS: assign animal to an enclosure
    @SuppressWarnings("methodlength")
    private void handleAssignAnimalToEnclosure() {
        JTextField animalField = new JTextField(10);
        JTextField enclosureField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Animal name:"));
        panel.add(animalField);
        panel.add(new JLabel("Enclosure name:"));
        panel.add(enclosureField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Assign animal to enclosure",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String animalName = animalField.getText().trim();
        String enclosureName = enclosureField.getText().trim();

        if (animalName.length() == 0 || enclosureName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Both names are required.");
            return;
        }

        Animal a = findAnimalByName(animalName);
        if (a == null) {
            JOptionPane.showMessageDialog(this, "No such animal.");
            return;
        }

        Enclosure e = findEnclosureByName(enclosureName);
        if (e == null) {
            JOptionPane.showMessageDialog(this, "No such enclosure.");
            return;
        }

        if (!e.canAcceptAnimal()) {
            JOptionPane.showMessageDialog(this, "Enclosure is full already!");
            return;
        }

        if (e.getExist(a)) {
            JOptionPane.showMessageDialog(this, "This animal is already in that enclosure!");
            return;
        }

        Enclosure current = a.getEnclosure();
        if (current != null) {
            current.removeAnimal(a);
            a.setToNoEnclosure();
        }

        e.addAnimal(a);
        a.assign(e);

        refreshEnclosureList();
        updateAnimalList();
        updateStatus();

        JOptionPane.showMessageDialog(this,
                "Assigned '" + a.getName() + "' to enclosure '" + e.getName() + "'.");
    }

    // MODIFIES: this, zoo
    // EFFECTS: Feed an the selected animal
    private void handleFeedOneAnimal() {
        Animal selected = (Animal) animalList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an animal to feed.");
            return;
        }
        selected.feed();
        updateAnimalList();
        updateStatus();
        JOptionPane.showMessageDialog(this,
                "Fed '" + selected.getName()
                        + "', hunger is now: " + selected.getHunger());
    }

    // MODIFIES: this, zoo
    // EFFECTS: play with the selected animal
    private void handlePlayOneAnimal() {
        Animal selected = (Animal) animalList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an animal to play with.");
            return;
        }
        selected.play();
        updateAnimalList();
        updateStatus();
        JOptionPane.showMessageDialog(this,
                "Played with '" + selected.getName()
                        + "', happiness is now " + selected.getHappinness());
    }

    // EFFECTS: save the current zoo as JSON files
    private void handleSaveZoo() {
        try {
            jsonWriter.open();
            jsonWriter.write(zoo);
            jsonWriter.close();
            JOptionPane.showMessageDialog(this,
                    "Saved " + zoo.getZooName() + " to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to write to file: " + JSON_STORE);
        }
    }

    // MODIFIES: this, zoo
    // EFFECTS: read the JSON File saved file and recreate the saved zoo
    private void handleLoadZoo() {
        try {
            zoo = jsonReader.read();
            defaultEnclosure = findEnclosureByName("Waiting Room");
            if (defaultEnclosure == null) {
                defaultEnclosure = new Enclosure("Waiting Room");
                zoo.addEnclosure(defaultEnclosure);
            }

            zooNameLabel.setText(zoo.getZooName());
            setTitle(zoo.getZooName());
            refreshEnclosureList();
            if (!enclosureListModel.isEmpty()) {
                enclosureList.setSelectedIndex(0);
            }
            updateAnimalList();
            updateStatus();
            JOptionPane.showMessageDialog(this,
                    "Loaded " + zoo.getZooName() + " from " + JSON_STORE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to read from file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: refresh the enclosure list
    private void refreshEnclosureList() {
        enclosureListModel.clear();
        ArrayList<Enclosure> list = zoo.getAllEnclosure();
        for (Enclosure e : list) {
            enclosureListModel.addElement(e);
        }
    }

    // MODIFIES: this
    // EFFECTS: update the animal list 
    private void updateAnimalList() {
        animalListModel.clear();

        Enclosure selected = enclosureList.getSelectedValue();
        if (selected == null) {
            return;
        }
        ArrayList<Animal> animalsInThisEnclosure = selected.getAllAnimals();
        for (Animal a : animalsInThisEnclosure) {
            animalListModel.addElement(a);
        }
    }

    // MODIFIES: this
    // EFFECTS: update the zoo's status
    private void updateStatus() {
        statusLabel.setText("Money: $" + zoo.getZooMoney()
                + " | Enclosures: " + zoo.getNumEnclosures()
                + " | Animals: " + zoo.getNumAnimals());
    }

    // EFFECTS: find and returns the animal with the given name
    private Animal findAnimalByName(String name) {
        ArrayList<Animal> list = zoo.getAllAnimals();
        for (Animal a : list) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    // EFEFCTS: find and returns the enclsure with the given name
    private Enclosure findEnclosureByName(String name) {
        ArrayList<Enclosure> list = zoo.getAllEnclosure();
        for (Enclosure e : list) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    // EFFECTS: display the image
    private void displayImage() {
        if (zooIcon != null) {
            Image scaled = zooIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            ImageIcon smallIcon = new ImageIcon(scaled);

            zooNameLabel.setIcon(smallIcon);
            zooNameLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            zooNameLabel.setIconTextGap(10);
        }
    }

    // EFFECTS: prints all events in the EventLog to the console
    private void printEventLog() {
    EventLog log = EventLog.getInstance();
    for (Event next : log) {
        System.out.println(next.toString());
    }
}
}
