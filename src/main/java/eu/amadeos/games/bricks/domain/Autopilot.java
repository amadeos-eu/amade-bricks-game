package eu.amadeos.games.bricks.domain;

import java.util.random.RandomGenerator;

/**
 * Plays the title screen's demo: it looks where the falling ball will reach the paddle and goes there,
 * meeting the ball a little off the middle, chosen anew for every ball, so the shots go to all sides.
 */
public final class Autopilot {
    private final RandomGenerator random;
    private double aim;
    private boolean wasFalling;
    private double waited;

    public Autopilot(RandomGenerator random) {
        this.random = random;
    }

    /** Where to move the paddle in this step: -1, 0 or +1. */
    public int move(Game game) {
        Ball ball = game.ball();
        boolean falling = ball.vy() > 0;
        if (falling && !wasFalling) aim = (random.nextDouble() * 2 - 1) * 0.6 * Field.PADDLE_WIDTH / 2;
        wasFalling = falling;
        double target = (falling ? arrival(ball) : ball.centerX()) - aim;
        double gap = target - game.paddle().centerX();
        if (Math.abs(gap) < 3) return 0;
        return gap < 0 ? -1 : 1;
    }

    /** Launches half a second after the ball lies on the paddle. */
    public boolean launch(Game game, double seconds) {
        if (game.phase() != Game.Phase.SERVING) {
            waited = 0;
            return false;
        }
        waited += seconds;
        return waited > 0.5;
    }

    /** Where the ball's centre reaches the paddle, folded back at the walls as between two mirrors. */
    static double arrival(Ball ball) {
        double time = (Field.PADDLE_Y - ball.y() - Field.BALL_SIZE) / ball.vy();
        double min = Field.LEFT + Field.BALL_SIZE / 2;
        double width = Field.RIGHT - Field.LEFT - Field.BALL_SIZE;
        double x = ball.centerX() + ball.vx() * time - min;
        double folded = Math.abs(((x % (2 * width)) + 2 * width) % (2 * width));
        return min + (folded > width ? 2 * width - folded : folded);
    }
}
