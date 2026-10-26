package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A summary of what happened overnight when a zoo day ended: money in, money out, and notable events
public class DayReport {

    private final int day;
    private final int ticketIncome;
    private final int upkeep;
    private final List<String> events;

    // EFFECTS: creates a report for the given day with no events yet
    public DayReport(int day, int ticketIncome, int upkeep) {
        this.day = day;
        this.ticketIncome = ticketIncome;
        this.upkeep = upkeep;
        this.events = new ArrayList<>();
    }

    // MODIFIES: this
    // EFFECTS: records something that happened overnight
    void addEvent(String event) {
        events.add(event);
    }

    // EFFECTS: returns the number of the day that just ended
    public int getDay() {
        return day;
    }

    public int getTicketIncome() {
        return ticketIncome;
    }

    public int getUpkeep() {
        return upkeep;
    }

    // EFFECTS: returns ticket income minus upkeep (event bonuses and losses are reported separately)
    public int getProfit() {
        return ticketIncome - upkeep;
    }

    public List<String> getEvents() {
        return Collections.unmodifiableList(events);
    }
}
