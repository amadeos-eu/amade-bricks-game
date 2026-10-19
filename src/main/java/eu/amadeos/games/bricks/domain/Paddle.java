package eu.amadeos.games.bricks.domain;

import eu.amadeos.gameengine.collision.Box;

/** The player's paddle: it moves left and right between the walls. */
public final class Paddle {
    private double x = (Field.WIDTH - Field.PADDLE_WIDTH) / 2;
    private double previousX = x;
    private double target = Double.NaN;   // where the mouse wants the paddle's centre; NaN: the keys steer

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

    /** The mouse: from now on the paddle's centre heads for {@code centerX}, until a key moves it. */
    void aim(double centerX) {
        target = centerX;
    }

    /** Moves left ({@code -1}), right ({@code +1}) or, without a key, towards the mouse's aim for one step. */
    void move(int direction, double seconds) {
        previousX = x;
        if (direction != 0) target = Double.NaN;
        double to = x + direction * Field.PADDLE_SPEED * seconds;
        if (!Double.isNaN(target)) {
            double step = Field.PADDLE_FOLLOW_SPEED * seconds;
            to = x + Math.clamp(target - Field.PADDLE_WIDTH / 2 - x, -step, step);
        }
        x = Math.clamp(to, Field.LEFT, Field.RIGHT - Field.PADDLE_WIDTH);
    }
}
