package eu.amadeos.games.bricks.domain;

import eu.amadeos.gameengine.collision.Box;

/** One brick of the wall: where it is, what kind, and how many hits it still takes. */
public final class Brick {
    private final int column;
    private final int row;
    private final BrickType type;
    private final Box box;
    private int hitsLeft;

    public Brick(int column, int row, BrickType type) {
        this.column = column;
        this.row = row;
        this.type = type;
        this.box = new Box(Field.LEFT + column * Field.BRICK_WIDTH, Field.BRICKS_TOP + row * Field.BRICK_HEIGHT,
                Field.BRICK_WIDTH, Field.BRICK_HEIGHT);
        this.hitsLeft = type.hits();
    }

    public int column() {
        return column;
    }

    public int row() {
        return row;
    }

    public BrickType type() {
        return type;
    }

    public Box box() {
        return box;
    }

    public int hitsLeft() {
        return hitsLeft;
    }

    /** True once a breakable brick has taken all its hits. */
    public boolean broken() {
        return type.breakable() && hitsLeft == 0;
    }

    /** True if a silver brick has taken a hit but not broken yet. */
    public boolean cracked() {
        return type.breakable() && hitsLeft > 0 && hitsLeft < type.hits();
    }

    /** One hit; true if the brick broke. Gold takes no damage. */
    boolean hit() {
        if (!type.breakable() || hitsLeft == 0) return false;
        hitsLeft--;
        return hitsLeft == 0;
    }
}
