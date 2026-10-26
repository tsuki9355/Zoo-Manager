package model;

// The outcome of a player action on the zoo, with a default explanation the UIs can show
public enum ActionResult {
    SUCCESS("Done!"),
    NO_SUCH_ANIMAL("There's no animal with that name."),
    NO_SUCH_ENCLOSURE("There's no enclosure with that name."),
    NAME_TAKEN("That name is already taken."),
    NOT_ENOUGH_MONEY("You can't afford that yet."),
    NOT_ENOUGH_FOOD("The food store is too low. Buy more food first."),
    LOCKED("Upgrade the zoo to unlock that."),
    ENCLOSURE_FULL("That enclosure is full."),
    WAITING_ROOM_FULL("The Waiting Room is full. Move some animals into enclosures first."),
    ALREADY_THERE("That animal already lives there."),
    ALREADY_DONE_TODAY("That's already been done today. Try again tomorrow."),
    IN_HOSPITAL("That animal is in the hospital right now."),
    HOSPITAL_FULL("Every hospital bed is taken. Upgrade the hospital for more beds."),
    MAX_LEVEL("That's already at the highest level."),
    NOTHING_TO_DO("There's nothing to do right now.");

    private final String message;

    ActionResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
