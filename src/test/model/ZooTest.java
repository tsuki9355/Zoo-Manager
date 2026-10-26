package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ZooTest {

    private Zoo zoo;

    // A random number generator that always triggers an event and always picks the given event kind
    private static final class FixedRandom extends Random {
        private final int pick;

        FixedRandom(int pick) {
            this.pick = pick;
        }

        @Override
        public double nextDouble() {
            return 0;
        }

        @Override
        public int nextInt(int bound) {
            return Math.min(pick, bound - 1);
        }
    }

    @BeforeEach
    void runBefore() {
        zoo = new Zoo("Jurrassic Park");
        zoo.setEventChance(0);
    }

    // EFFECTS: adds a penguin called name to the zoo for free and settles it in a free enclosure "Ice"
    private Animal settledPenguin(String name) {
        zoo.addEnclosure("Ice");
        zoo.addAnimal("Penguin", name, 2);
        zoo.assignAnimalToEnclosure(name, "Ice");
        return zoo.findAnimal(name);
    }

    @Test
    void testConstructor() {
        assertEquals("Jurrassic Park", zoo.getName());
        assertEquals(1000, zoo.getMoney());
        assertEquals(20, zoo.getFood());
        assertEquals(1, zoo.getDay());
        assertEquals(1, zoo.getLevel());
        assertEquals(1, zoo.getHospital().getCapacity());
        assertEquals(0, zoo.getNumEnclosures());
        assertEquals(0, zoo.getNumAnimals());
        assertFalse(zoo.isBankrupt());
    }

    // ----- bookkeeping -----

    @Test
    void testAddEnclosureMultiple() {
        assertTrue(zoo.addEnclosure("Orca's Enclosure"));
        assertTrue(zoo.addEnclosure("Penguin's Enclosure"));
        assertEquals("Penguin's Enclosure", zoo.getEnclosures().get(1).getName());
        assertFalse(zoo.addEnclosure("Orca's Enclosure"));
        assertEquals(2, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureByTypedName() {
        // names typed by a user are new String objects, not literals; lookups must use equals()
        zoo.addEnclosure("Water");
        assertTrue(zoo.removeEnclosure(new String("Water")));
        assertEquals(0, zoo.getNumEnclosures());
    }

    @Test
    void testRemoveEnclosureRefusals() {
        settledPenguin("pingu");
        assertFalse(zoo.removeEnclosure("Ice"));
        assertFalse(zoo.removeEnclosure("Nowhere"));
        zoo.getOrCreateWaitingRoom();
        assertFalse(zoo.removeEnclosure(Zoo.WAITING_ROOM));
        assertEquals(2, zoo.getNumEnclosures());
    }

    @Test
    void testGetOrCreateWaitingRoom() {
        Enclosure waitingRoom = zoo.getOrCreateWaitingRoom();
        assertEquals(Zoo.WAITING_ROOM, waitingRoom.getName());
        assertSame(waitingRoom, zoo.getOrCreateWaitingRoom());
        assertEquals(1, zoo.getNumEnclosures());
    }

    @Test
    void testAddAnimalMultiple() {
        assertTrue(zoo.addAnimal("Sloth", "Slothy", 3));
        assertTrue(zoo.addAnimal("Crocodile", "Crocy", 3));
        assertFalse(zoo.addAnimal(new Animal("Crocodile", "Slothy", 3)));
        assertEquals(2, zoo.getNumAnimals());
    }

    @Test
    void testFindAnimalAndEnclosure() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addEnclosure("Water");
        assertEquals("Orca", zoo.findAnimal("tuki").getSpecies());
        assertEquals("Water", zoo.findEnclosure("Water").getName());
        assertNull(zoo.findAnimal("Fire"));
        assertNull(zoo.findEnclosure("Fire"));
    }

    // ----- building -----

    @Test
    void testBuildEnclosure() {
        assertEquals(ActionResult.SUCCESS, zoo.buildEnclosure("Savanna"));
        assertEquals(1000 - Zoo.ENCLOSURE_PRICE, zoo.getMoney());
        assertEquals(ActionResult.NAME_TAKEN, zoo.buildEnclosure("Savanna"));
        zoo.setMoney(100);
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.buildEnclosure("Jungle"));
        assertEquals(1, zoo.getNumEnclosures());
    }

    @Test
    void testExpandEnclosure() {
        zoo.addEnclosure("Savanna");
        assertEquals(ActionResult.SUCCESS, zoo.expandEnclosure("Savanna"));
        assertEquals(750, zoo.getMoney());
        assertEquals(7, zoo.findEnclosure("Savanna").getCapacity());
        assertEquals(ActionResult.NO_SUCH_ENCLOSURE, zoo.expandEnclosure("Jungle"));
        zoo.setMoney(0);
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.expandEnclosure("Savanna"));
        zoo.findEnclosure("Savanna").setCapacity(Enclosure.MAX_CAPACITY);
        assertEquals(ActionResult.MAX_LEVEL, zoo.expandEnclosure("Savanna"));
    }

    @Test
    void testUpgradeHospital() {
        assertEquals(ActionResult.SUCCESS, zoo.upgradeHospital());
        assertEquals(600, zoo.getMoney());
        assertEquals(2, zoo.getHospital().getCapacity());
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.upgradeHospital());
        zoo.getHospital().setLevel(Hospital.MAX_LEVEL);
        assertEquals(ActionResult.MAX_LEVEL, zoo.upgradeHospital());
    }

    @Test
    void testUpgradeZooUnlocksSpecies() {
        zoo.setMoney(20000);
        assertEquals(ActionResult.LOCKED, zoo.adoptAnimal(Species.ZEBRA, "Stripes"));
        assertEquals(1500, zoo.getZooUpgradeCost());
        assertEquals(ActionResult.SUCCESS, zoo.upgradeZoo());
        assertEquals(2, zoo.getLevel());
        assertEquals(ActionResult.SUCCESS, zoo.adoptAnimal(Species.ZEBRA, "Stripes"));
        zoo.upgradeZoo();
        zoo.upgradeZoo();
        assertEquals(0, zoo.getZooUpgradeCost());
        assertEquals(ActionResult.MAX_LEVEL, zoo.upgradeZoo());
    }

    @Test
    void testUpgradeZooNotEnoughMoney() {
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.upgradeZoo());
        assertEquals(1, zoo.getLevel());
    }

    // ----- animals -----

    @Test
    void testAdoptAnimal() {
        assertEquals(ActionResult.SUCCESS, zoo.adoptAnimal(Species.PENGUIN, "pingu"));
        Animal pingu = zoo.findAnimal("pingu");
        assertEquals(1000 - Species.PENGUIN.getPrice(), zoo.getMoney());
        assertEquals("Penguin", pingu.getSpecies());
        assertEquals(1, pingu.getAge());
        assertEquals(Zoo.WAITING_ROOM, pingu.getEnclosureName());
        assertEquals(ActionResult.NAME_TAKEN, zoo.adoptAnimal(Species.MEERKAT, "pingu"));
    }

    @Test
    void testAdoptAnimalRefusals() {
        zoo.setMoney(50);
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.adoptAnimal(Species.MEERKAT, "Timon"));
        assertEquals(ActionResult.LOCKED, zoo.adoptAnimal(Species.ELEPHANT, "Dumbo"));
        zoo.setMoney(10000);
        for (int i = 0; i < Enclosure.DEFAULT_CAPACITY; i++) {
            zoo.adoptAnimal(Species.MEERKAT, "Timon" + i);
        }
        assertEquals(ActionResult.WAITING_ROOM_FULL, zoo.adoptAnimal(Species.MEERKAT, "Extra"));
    }

    @Test
    void testSellAnimal() {
        zoo.adoptAnimal(Species.PENGUIN, "pingu");
        assertEquals(75, zoo.getSalePrice(zoo.findAnimal("pingu")));
        assertEquals(ActionResult.SUCCESS, zoo.sellAnimal("pingu"));
        assertEquals(1000 - 150 + 75, zoo.getMoney());
        assertNull(zoo.findAnimal("pingu"));
        assertTrue(zoo.findEnclosure(Zoo.WAITING_ROOM).isEmpty());
        assertEquals(ActionResult.NO_SUCH_ANIMAL, zoo.sellAnimal("pingu"));
    }

    @Test
    void testSellAnimalInHospital() {
        zoo.addAnimal("Penguin", "pingu", 2);
        zoo.sendToHospital("pingu");
        assertEquals(ActionResult.IN_HOSPITAL, zoo.sellAnimal("pingu"));
    }

    @Test
    void testAssignAnimalMovesBetweenEnclosures() {
        zoo.addEnclosure("Water");
        zoo.addEnclosure("Fire");
        zoo.addAnimal("Orca", "tuki", 3);
        assertEquals(ActionResult.SUCCESS, zoo.assignAnimalToEnclosure("tuki", "Water"));
        assertEquals(ActionResult.SUCCESS, zoo.assignAnimalToEnclosure("tuki", "Fire"));
        assertEquals(0, zoo.findEnclosure("Water").getNumAnimals());
        assertEquals(1, zoo.findEnclosure("Fire").getNumAnimals());
        assertEquals("Fire", zoo.findAnimal("tuki").getEnclosureName());
    }

    @Test
    void testAssignAnimalToEnclosureFailures() {
        zoo.addEnclosure("Water");
        zoo.addAnimal("Orca", "tuki", 3);
        assertEquals(ActionResult.NO_SUCH_ANIMAL, zoo.assignAnimalToEnclosure("nobody", "Water"));
        assertEquals(ActionResult.NO_SUCH_ENCLOSURE, zoo.assignAnimalToEnclosure("tuki", "Nowhere"));
        zoo.assignAnimalToEnclosure("tuki", "Water");
        assertEquals(ActionResult.ALREADY_THERE, zoo.assignAnimalToEnclosure("tuki", "Water"));
    }

    @Test
    void testAssignAnimalToEnclosureFull() {
        zoo.addEnclosure("water");
        for (int i = 0; i <= Enclosure.DEFAULT_CAPACITY; i++) {
            zoo.addAnimal("Orca", "tuki" + i, 3);
        }
        for (int i = 0; i < Enclosure.DEFAULT_CAPACITY; i++) {
            assertEquals(ActionResult.SUCCESS, zoo.assignAnimalToEnclosure("tuki" + i, "water"));
        }
        assertEquals(ActionResult.ENCLOSURE_FULL, zoo.assignAnimalToEnclosure("tuki5", "water"));
        assertNull(zoo.findAnimal("tuki5").getEnclosure());
    }

    @Test
    void testRemoveAnimalFromEnclosure() {
        settledPenguin("pingu");
        assertTrue(zoo.removeAnimalFromEnclosure("pingu", "Ice"));
        assertNull(zoo.findAnimal("pingu").getEnclosure());
        assertFalse(zoo.removeAnimalFromEnclosure("pingu", "Ice"));
        assertFalse(zoo.removeAnimalFromEnclosure("nobody", "Ice"));
        assertFalse(zoo.removeAnimalFromEnclosure("pingu", "Nowhere"));
    }

    // ----- care -----

    @Test
    void testFeedAnimalUsesFood() {
        zoo.addAnimal("Elephant", "Dumbo", 5);
        assertEquals(ActionResult.SUCCESS, zoo.feedAnimal("Dumbo"));
        assertEquals(20 - Species.ELEPHANT.getFoodPerMeal(), zoo.getFood());
        assertEquals(130, zoo.findAnimal("Dumbo").getHunger());
        zoo.setFood(4);
        assertEquals(ActionResult.NOT_ENOUGH_FOOD, zoo.feedAnimal("Dumbo"));
        assertEquals(ActionResult.NO_SUCH_ANIMAL, zoo.feedAnimal("nobody"));
    }

    @Test
    void testFeedAllAnimals() {
        assertEquals(ActionResult.NOTHING_TO_DO, zoo.feedAllAnimals());
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.addAnimal("Giraffe", "longus", 5);
        assertEquals(ActionResult.SUCCESS, zoo.feedAllAnimals());
        assertEquals(20 - 1 - 3, zoo.getFood());
        assertEquals(130, zoo.findAnimal("longus").getHunger());
        zoo.setFood(3);
        assertEquals(ActionResult.NOT_ENOUGH_FOOD, zoo.feedAllAnimals());
        assertEquals(130, zoo.findAnimal("pingu").getHunger());
    }

    @Test
    void testPlayAndRestOncePerDay() {
        zoo.addAnimal("Orca", "tuki", 2);
        assertEquals(ActionResult.SUCCESS, zoo.playAnimal("tuki"));
        assertEquals(ActionResult.ALREADY_DONE_TODAY, zoo.playAnimal("tuki"));
        assertEquals(ActionResult.SUCCESS, zoo.restAnimal("tuki"));
        assertEquals(ActionResult.ALREADY_DONE_TODAY, zoo.restAnimal("tuki"));
        assertEquals(ActionResult.NO_SUCH_ANIMAL, zoo.playAnimal("nobody"));
        zoo.endDay();
        assertEquals(ActionResult.SUCCESS, zoo.playAnimal("tuki"));
    }

    @Test
    void testPlayAndRestEveryone() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        zoo.playAnimal("tuki");
        assertEquals(1, zoo.playWithAllAnimals());
        assertEquals(2, zoo.restAllAnimals());
        assertEquals(0, zoo.restAllAnimals());
        assertEquals(120, zoo.findAnimal("pingu").getHappiness());
        assertEquals(110, zoo.findAnimal("pingu").getHealth());
    }

    @Test
    void testSendToHospital() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.addAnimal("Penguin", "pingu", 5);
        assertEquals(ActionResult.SUCCESS, zoo.sendToHospital("tuki"));
        assertEquals(1000 - Hospital.ADMISSION_FEE, zoo.getMoney());
        assertEquals(ActionResult.IN_HOSPITAL, zoo.sendToHospital("tuki"));
        assertEquals(ActionResult.IN_HOSPITAL, zoo.feedAnimal("tuki"));
        assertEquals(ActionResult.IN_HOSPITAL, zoo.playAnimal("tuki"));
        assertEquals(ActionResult.HOSPITAL_FULL, zoo.sendToHospital("pingu"));
        assertEquals(ActionResult.NO_SUCH_ANIMAL, zoo.sendToHospital("nobody"));
    }

    @Test
    void testSendToHospitalNotEnoughMoney() {
        zoo.addAnimal("Orca", "tuki", 2);
        zoo.setMoney(10);
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.sendToHospital("tuki"));
        assertFalse(zoo.findAnimal("tuki").isInHospital());
    }

    @Test
    void testCleanAllEnclosures() {
        assertEquals(ActionResult.NOTHING_TO_DO, zoo.cleanAllEnclosures());
        zoo.addEnclosure("Orca's Enclosure");
        zoo.addEnclosure("Penguin's Enclosure");
        zoo.getEnclosures().get(0).setCleanliness(4);
        assertEquals(10, zoo.getCleaningCost());
        assertEquals(ActionResult.SUCCESS, zoo.cleanAllEnclosures());
        assertEquals(990, zoo.getMoney());
        assertEquals(9, zoo.getEnclosures().get(0).getCleanliness());
        zoo.setMoney(0);
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.cleanAllEnclosures());
    }

    @Test
    void testBuyFood() {
        assertEquals(ActionResult.SUCCESS, zoo.buyFood(2));
        assertEquals(20 + 2 * Zoo.FOOD_PACK_SIZE, zoo.getFood());
        assertEquals(1000 - 2 * Zoo.FOOD_PACK_PRICE, zoo.getMoney());
        assertEquals(ActionResult.NOTHING_TO_DO, zoo.buyFood(0));
        assertEquals(ActionResult.NOT_ENOUGH_MONEY, zoo.buyFood(1000));
    }

    // ----- the day cycle -----

    @Test
    void testEndDay() {
        Animal pingu = settledPenguin("pingu");
        assertEquals(3, zoo.getRating());
        DayReport report = zoo.endDay();
        assertEquals(1, report.getDay());
        assertEquals(Species.PENGUIN.getAppeal(), report.getTicketIncome());
        assertEquals(Zoo.ENCLOSURE_UPKEEP, report.getUpkeep());
        assertEquals(1000 + report.getProfit(), zoo.getMoney());
        assertTrue(report.getEvents().isEmpty());
        assertEquals(2, zoo.getDay());
        assertEquals(80, pingu.getHunger());
        assertEquals(9, zoo.findEnclosure("Ice").getCleanliness());
    }

    @Test
    void testWaitingRoomAnimalsEarnNothing() {
        zoo.adoptAnimal(Species.PENGUIN, "pingu");
        assertEquals(0, zoo.getTicketIncome());
        assertEquals(0, zoo.getUpkeep());
    }

    @Test
    void testTicketIncomeGrowsWithLevel() {
        settledPenguin("pingu");
        zoo.setLevel(2);
        assertEquals(29, zoo.getTicketIncome());
    }

    @Test
    void testEndDayDischargesPatients() {
        Animal pingu = settledPenguin("pingu");
        pingu.setHealth(30);
        zoo.sendToHospital("pingu");
        assertEquals(0, zoo.getTicketIncome());
        assertTrue(zoo.endDay().getEvents().isEmpty());
        DayReport report = zoo.endDay();
        assertEquals(1, report.getEvents().size());
        assertFalse(pingu.isInHospital());
        assertEquals(Animal.MAX_CONDITION, pingu.getHealth());
    }

    @Test
    void testEndDayInspectorTakesNeglectedAnimals() {
        Animal pingu = settledPenguin("pingu");
        pingu.setHealth(5);
        DayReport report = zoo.endDay();
        assertNull(zoo.findAnimal("pingu"));
        assertTrue(zoo.findEnclosure("Ice").isEmpty());
        assertTrue(report.getEvents().get(0).contains("welfare inspector"));
    }

    @Test
    void testBirthdays() {
        Animal pingu = settledPenguin("pingu");
        for (int i = 0; i < Zoo.BIRTHDAY_INTERVAL; i++) {
            pingu.treat();
            zoo.endDay();
        }
        assertEquals(Zoo.BIRTHDAY_INTERVAL + 1, zoo.getDay());
        assertEquals(3, pingu.getAge());
    }

    @Test
    void testBankruptcy() {
        zoo.addEnclosure("Empty");
        zoo.setMoney(5);
        zoo.endDay();
        assertEquals(-5, zoo.getMoney());
        assertTrue(zoo.isBankrupt());
    }

    // ----- random events -----

    // EFFECTS: returns a zoo whose nightly event is always the given kind, with a settled penguin "pingu"
    private Zoo zooWithEvent(int kind) {
        Zoo eventful = new Zoo("Eventful", new FixedRandom(kind));
        eventful.addEnclosure("Ice");
        eventful.addAnimal("Penguin", "pingu", 2);
        eventful.assignAnimalToEnclosure("pingu", "Ice");
        eventful.setEventChance(1);
        return eventful;
    }

    @Test
    void testEventSchoolTrip() {
        Zoo eventful = zooWithEvent(0);
        DayReport report = eventful.endDay();
        assertEquals(1000 + report.getProfit() + 50, eventful.getMoney());
        assertEquals(1, report.getEvents().size());
    }

    @Test
    void testEventRats() {
        Zoo eventful = zooWithEvent(1);
        eventful.endDay();
        assertEquals(15, eventful.getFood());
    }

    @Test
    void testEventStorm() {
        Zoo eventful = zooWithEvent(2);
        eventful.endDay();
        assertEquals(10 - 1 - 3, eventful.findEnclosure("Ice").getCleanliness());
    }

    @Test
    void testEventNewspaper() {
        Zoo eventful = zooWithEvent(3);
        eventful.endDay();
        assertEquals(100 - 10 + 25, eventful.findAnimal("pingu").getHappiness());
    }

    @Test
    void testEventDonation() {
        Zoo eventful = zooWithEvent(4);
        DayReport report = eventful.endDay();
        assertEquals(1000 + report.getProfit() + 150, eventful.getMoney());
    }

    @Test
    void testNoEventsWithoutAnimals() {
        Zoo empty = new Zoo("Empty", new FixedRandom(4));
        empty.setEventChance(1);
        assertTrue(empty.endDay().getEvents().isEmpty());
        assertEquals(1000, empty.getMoney());
    }

    // ----- rating -----

    @Test
    void testRatingEmptyZoo() {
        assertEquals(0, zoo.getRating());
    }

    @Test
    void testRatingBest() {
        Animal pingu = settledPenguin("pingu");
        pingu.treat();
        zoo.findEnclosure("Ice").setCleanliness(Enclosure.MAX_CLEANLINESS);
        assertEquals(Zoo.MAX_RATING, zoo.getRating());
    }

    @Test
    void testRatingIgnoresPatients() {
        settledPenguin("pingu");
        zoo.sendToHospital("pingu");
        assertEquals(0, zoo.getRating());
    }
}
