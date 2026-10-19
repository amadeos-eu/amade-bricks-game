package eu.amadeos.games.bricks.domain;

import eu.amadeos.gameengine.collision.Box;

/** The player's paddle: it moves left and right between the walls. */
public final class Paddle {
    private double x = (Field.WIDTH - Field.PADDLE_WIDTH) / 2;
    private double previousX = x;

    public double x() {
        return x;
    }

    public double previousX() {
        return previousX;
    }

    public double centerX() {
        return x + Field.PADDLE_WIDTH / 2;
    }

    public Box box() {
        return new Box(x, Field.PADDLE_Y, Field.PADDLE_WIDTH, Field.PADDLE_HEIGHT);
    }

    /** Moves left ({@code -1}), right ({@code +1}) or not at all for one step. */
    void move(int direction, double seconds) {
        previousX = x;
        x = Math.clamp(x + direction * Field.PADDLE_SPEED * seconds, Field.LEFT, Field.RIGHT - Field.PADDLE_WIDTH);
    }
}
