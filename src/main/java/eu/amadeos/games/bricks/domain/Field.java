package eu.amadeos.games.bricks.domain;

import eu.amadeos.gameengine.collision.Box;

/**
 * The playing field in logical units (640 × 400): a band for the score at the top, walls left, right and
 * at the top, the bricks, the paddle near the bottom. Speeds are in units per second.
 */
public final class Field {
    public static final double WIDTH = 640;
    public static final double HEIGHT = 400;
    /** The band at the top where score, level and lives are shown. */
    public static final double HUD = 24;
    public static final double WALL = 20;
    /** The inner edges of the walls. */
    public static final double LEFT = WALL;
    public static final double RIGHT = WIDTH - WALL;
    public static final double TOP = HUD + 16;

    public static final int COLUMNS = 15;
    public static final int MAX_ROWS = 12;
    public static final double BRICK_WIDTH = 40;
    public static final double BRICK_HEIGHT = 16;
    /** Where the first row of bricks begins; two empty rows above it leave room for the ball. */
    public static final double BRICKS_TOP = TOP + 24;

    public static final double PADDLE_WIDTH = 64;
    public static final double PADDLE_HEIGHT = 10;
    public static final double PADDLE_Y = 370;
    public static final double PADDLE_SPEED = 460;
    /** How fast the paddle follows the mouse: fast, but never a jump through a falling ball. */
    public static final double PADDLE_FOLLOW_SPEED = 1600;

    public static final double BALL_SIZE = 8;
    public static final double BALL_SPEED = 280;
    /** Every brick hit makes the ball this much faster, up to {@link #BALL_MAX_SPEED}. */
    public static final double BALL_SPEED_UP = 3;
    public static final double BALL_MAX_SPEED = 520;
    /** The steepest a ball leaves the paddle, from straight up: 60°. */
    public static final double MAX_ANGLE = Math.toRadians(60);

    /** The three walls as boxes; they reach beyond the field so nothing slips past a corner. */
    public static final Box LEFT_WALL = new Box(-100, 0, LEFT + 100, HEIGHT);
    public static final Box RIGHT_WALL = new Box(RIGHT, 0, 100 + WALL, HEIGHT);
    public static final Box TOP_WALL = new Box(-100, -100, WIDTH + 200, TOP + 100);

    private Field() {
    }
}
