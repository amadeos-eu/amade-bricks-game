package eu.amadeos.games.bricks.domain;

import java.util.Optional;

/** The kinds of bricks, with the letter that stands for them in a level file. */
// --8<-- [start:brick-type]
public enum BrickType {
    RED('r', 1, 70), ORANGE('o', 1, 60), YELLOW('y', 1, 50), GREEN('g', 1, 40), BLUE('b', 1, 30),
    VIOLET('v', 1, 20),
    /** Takes two hits. */
    SILVER('s', 2, 100),
    /** Never breaks; a level is cleared without it. */
    GOLD('X', 0, 0);

    private final char letter;
    private final int hits;
    private final int points;

    BrickType(char letter, int hits, int points) {
        this.letter = letter;
        this.hits = hits;
        this.points = points;
    }
    // --8<-- [end:brick-type]

    public char letter() {
        return letter;
    }

    /** Hits it takes to break; 0 for a brick that never breaks. */
    public int hits() {
        return hits;
    }

    public int points() {
        return points;
    }

    public boolean breakable() {
        return hits > 0;
    }

    public static Optional<BrickType> ofLetter(char letter) {
        for (BrickType type : values()) if (type.letter == letter) return Optional.of(type);
        return Optional.empty();
    }
}
