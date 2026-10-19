package eu.amadeos.games.bricks.domain;

import eu.amadeos.gameengine.collision.Box;
import eu.amadeos.gameengine.collision.Sweep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * A game of Bricks: the levels one after the other, the paddle, the ball, the score and the lives. Plain
 * Java without a clock: {@link #update} advances by one step, and the {@link #events()} tell what happened
 * in it.
 */
public final class Game {
    public static final int LIVES = 3;

    /** Ball on the paddle, waiting for the launch; ball flying; game over. */
    public enum Phase { SERVING, PLAYING, OVER }

    private final List<Level> levels;
    private final RandomGenerator random;
    private final Paddle paddle = new Paddle();
    private final Ball ball = new Ball();
    private final List<Event> events = new ArrayList<>();
    private List<Brick> bricks;
    private int level;
    private int score;
    private int lives = LIVES;
    private double speed;
    private Phase phase;

    public Game(List<Level> levels, RandomGenerator random) {
        if (levels.isEmpty()) throw new IllegalArgumentException("no levels");
        this.levels = List.copyOf(levels);
        this.random = random;
        startLevel(0);
    }

    private void startLevel(int index) {
        level = index;
        bricks = levels.get(index).bricks();
        serve();
    }

    private void serve() {
        phase = Phase.SERVING;
        speed = Field.BALL_SPEED;
        ball.setVelocity(0, 0);
        placeOnPaddle();
        ball.remember();
    }

    private void placeOnPaddle() {
        ball.place(paddle.centerX() - Field.BALL_SIZE / 2, Field.PADDLE_Y - Field.BALL_SIZE);
    }

    public Paddle paddle() {
        return paddle;
    }

    public Ball ball() {
        return ball;
    }

    /** The bricks still standing, gold ones included. */
    public List<Brick> bricks() {
        return Collections.unmodifiableList(bricks);
    }

    public int score() {
        return score;
    }

    public int lives() {
        return lives;
    }

    /** The current level, counted from 1. */
    public int level() {
        return level + 1;
    }

    public int levelCount() {
        return levels.size();
    }

    public Phase phase() {
        return phase;
    }

    /** What happened in the last step. */
    public List<Event> events() {
        return Collections.unmodifiableList(events);
    }

    // --8<-- [start:update]
    /**
     * One step: the paddle moves ({@code -1} left, {@code +1} right, 0 not), a ball on the paddle is launched
     * if {@code launch}, a flying ball moves and bounces.
     */
    public void update(double seconds, int move, boolean launch) {
        events.clear();
        if (phase == Phase.OVER) return;
        ball.remember();
        paddle.move(move, seconds);
        if (phase == Phase.SERVING) {
            placeOnPaddle();
            if (launch) launch();
            return;
        }
        moveBall(seconds);
        if (phase == Phase.PLAYING && ball.y() > Field.HEIGHT) loseBall();
    }
    // --8<-- [end:update]

    private void launch() {
        // Up, a little to one side, never the same twice.
        double angle = (random.nextBoolean() ? 1 : -1) * Math.toRadians(10 + random.nextDouble() * 20);
        ball.setVelocity(speed * Math.sin(angle), -speed * Math.cos(angle));
        phase = Phase.PLAYING;
    }

    /** What the ball touches first: a wall, a brick or the paddle. */
    private record Contact(Sweep.Hit hit, Brick brick, boolean paddle) {
    }

    // --8<-- [start:move-ball]
    /**
     * Moves the ball by one step. It may touch something on its way: then it moves only as far as the
     * contact, bounces, and goes on with the rest of the step, so a ball can bounce twice in one step and
     * never passes through anything.
     */
    private void moveBall(double seconds) {
        catchOverlap();
        double rest = 1;  // the share of the step still to go
        for (int bounces = 0; bounces < 4 && rest > 0; bounces++) {
            double dx = ball.vx() * seconds * rest;
            double dy = ball.vy() * seconds * rest;
            Contact first = firstContact(ball.box(), dx, dy);
            if (first == null) {
                ball.moveBy(dx, dy);
                return;
            }
            double t = first.hit().time();
            ball.moveBy(dx * t, dy * t);
            rest *= 1 - t;
            bounce(first);
            if (phase != Phase.PLAYING) return;  // the last brick broke: the next level waits
        }
    }

    private Contact firstContact(Box box, double dx, double dy) {
        Contact first = null;
        for (Box wall : List.of(Field.LEFT_WALL, Field.RIGHT_WALL, Field.TOP_WALL)) {
            first = earlier(first, Sweep.sweep(box, dx, dy, wall), null, false);
        }
        for (Brick brick : bricks) first = earlier(first, Sweep.sweep(box, dx, dy, brick.box()), brick, false);
        // The paddle only catches a falling ball.
        if (dy > 0) first = earlier(first, Sweep.sweep(box, dx, dy, paddle.box()), null, true);
        return first;
    }
    // --8<-- [end:move-ball]

    private static Contact earlier(Contact current, Sweep.Hit hit, Brick brick, boolean paddle) {
        if (hit == null || (current != null && current.hit().time() <= hit.time())) return current;
        return new Contact(hit, brick, paddle);
    }

    /**
     * The paddle moved sideways into a falling ball: they overlap, which the sweep cannot see. Count it as
     * a catch, as a player would expect.
     */
    private void catchOverlap() {
        if (ball.vy() > 0 && ball.box().overlaps(paddle.box())
                && ball.y() + Field.BALL_SIZE / 2 < Field.PADDLE_Y + Field.PADDLE_HEIGHT / 2) {
            ball.place(ball.x(), Field.PADDLE_Y - Field.BALL_SIZE);
            bounceOffPaddle();
        }
    }

    // --8<-- [start:bounce]
    private void bounce(Contact contact) {
        Sweep.Hit hit = contact.hit();
        if (contact.paddle() && hit.normalY() < 0) {
            bounceOffPaddle();
            return;
        }
        // Mirror the velocity at the face that was hit.
        double vx = hit.normalX() != 0 ? Math.abs(ball.vx()) * hit.normalX() : ball.vx();
        double vy = hit.normalY() != 0 ? Math.abs(ball.vy()) * hit.normalY() : ball.vy();
        ball.setVelocity(vx, vy);
        if (contact.brick() != null) hitBrick(contact.brick());
        else events.add(new Event.Bounce(contact.paddle()));
    }

    /** As in Paddle: where the ball meets the paddle decides the angle, the middle straight up. */
    private void bounceOffPaddle() {
        double reach = (Field.PADDLE_WIDTH + Field.BALL_SIZE) / 2;
        double offset = Math.clamp((ball.centerX() - paddle.centerX()) / reach, -1, 1);
        double angle = offset * Field.MAX_ANGLE;
        ball.setVelocity(speed * Math.sin(angle), -speed * Math.cos(angle));
        events.add(new Event.Bounce(true));
    }
    // --8<-- [end:bounce]

    // --8<-- [start:hit-brick]
    private void hitBrick(Brick brick) {
        boolean broken = brick.hit();
        events.add(new Event.BrickHit(brick, broken));
        speed = Math.min(Field.BALL_MAX_SPEED, speed + Field.BALL_SPEED_UP);
        ball.setSpeed(speed);
        if (!broken) return;
        score += brick.type().points();
        bricks.remove(brick);
        if (bricks.stream().noneMatch(b -> b.type().breakable())) clearLevel();
    }
    // --8<-- [end:hit-brick]

    private void clearLevel() {
        events.add(new Event.LevelCleared(level()));
        if (level + 1 < levels.size()) {
            startLevel(level + 1);
        } else {
            phase = Phase.OVER;
            events.add(new Event.GameOver(new GameResult(score, level(), true)));
        }
    }

    private void loseBall() {
        lives--;
        events.add(new Event.BallLost(lives));
        if (lives > 0) {
            serve();
        } else {
            phase = Phase.OVER;
            events.add(new Event.GameOver(new GameResult(score, level(), false)));
        }
    }
}
