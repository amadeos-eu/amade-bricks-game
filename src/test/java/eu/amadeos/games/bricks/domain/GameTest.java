package eu.amadeos.games.bricks.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {
    private static final double STEP = 1.0 / 120;

    /** A level with one row: the given letters from the left, the rest gaps. */
    private static Level row(String bricks) {
        return new Level(List.of(bricks + ".".repeat(Field.COLUMNS - bricks.length())));
    }

    private static Game game(Level... levels) {
        return new Game(List.of(levels), new SplittableRandom(3));
    }

    /** Launches, then puts the ball where the test wants it. */
    private static void fly(Game game, double x, double y, double vx, double vy) {
        game.update(0, 0, true);
        game.ball().place(x, y);
        game.ball().setVelocity(vx, vy);
    }

    private static List<Event> run(Game game, double seconds, double step) {
        List<Event> events = new ArrayList<>();
        for (double t = 0; t < seconds; t += step) {
            game.update(step, 0, false);
            events.addAll(game.events());
        }
        return events;
    }

    @Test
    void theBallWaitsOnThePaddleUntilLaunched() {
        Game game = game(row("r"));
        assertEquals(Game.Phase.SERVING, game.phase());
        game.update(0.5, 1, false);  // the paddle moves, the ball goes with it
        assertEquals(game.paddle().centerX(), game.ball().centerX(), 1e-9);
        game.update(STEP, 0, true);
        assertEquals(Game.Phase.PLAYING, game.phase());
        assertTrue(game.ball().vy() < 0, "flies up");
        assertEquals(Field.BALL_SPEED, Math.hypot(game.ball().vx(), game.ball().vy()), 1e-9);
    }

    // --8<-- [start:test-brick]
    @Test
    void aBrickBreaksScoresAndSendsTheBallBack() {
        Game game = game(row("rr"));
        // Straight up under the first brick (x 20 to 60, bottom at y 80).
        fly(game, 36, 120, 0, -300);
        List<Event> events = run(game, 0.2, STEP);
        Event.BrickHit hit = assertInstanceOf(Event.BrickHit.class, events.getFirst());
        assertEquals(0, hit.brick().column());
        assertTrue(hit.broken());
        assertEquals(70, game.score());
        assertEquals(1, game.bricks().size());
        assertTrue(game.ball().vy() > 0, "bounced down");
    }
    // --8<-- [end:test-brick]

    // --8<-- [start:test-big-step]
    @Test
    void evenAHugeStepCannotCarryTheBallThroughABrick() {
        Game game = game(row("rr"));
        // One step of a tenth of a second: 40 units, more than the brick is high.
        fly(game, 36, 90, 0, -400);
        game.update(0.1, 0, false);
        assertInstanceOf(Event.BrickHit.class, game.events().getFirst());
        // 10 units up to the brick, the rest of the step back down.
        assertTrue(game.ball().y() > 90, "below where it started: " + game.ball().y());
        assertTrue(game.ball().vy() > 0);
    }
    // --8<-- [end:test-big-step]

    @Test
    void silverTakesTwoHitsAndGoldNone() {
        Game game = game(row("sX"));
        Brick silver = game.bricks().get(0);
        Brick gold = game.bricks().get(1);
        fly(game, 36, 120, 0, -300);
        run(game, 0.2, STEP);
        assertTrue(silver.cracked());
        assertEquals(0, game.score());
        fly(game, 36, 120, 0, -300);
        run(game, 0.2, STEP);
        assertTrue(silver.broken());
        assertEquals(100, game.score());
        // Gold only: the level counts as cleared, but this was the last level.
        assertEquals(Game.Phase.OVER, game.phase());
        assertFalse(gold.broken());
    }

    @Test
    void wallsMirrorTheBall() {
        Game game = game(row("r"));
        fly(game, 300, 200, -300, -100);
        List<Event> events = run(game, 1.2, STEP);
        assertTrue(events.contains(new Event.Bounce(false)), "bounced off a wall");
        assertTrue(game.ball().x() >= Field.LEFT, "stayed in the field");
    }

    // --8<-- [start:test-angle]
    @Test
    void theEdgeOfThePaddleSendsTheBallSteeply() {
        Game game = game(row("r"));
        double right = game.paddle().x() + Field.PADDLE_WIDTH - 2;
        fly(game, right, Field.PADDLE_Y - 30, 0, 300);
        run(game, 0.15, STEP);
        assertTrue(game.ball().vy() < 0, "back up");
        assertTrue(game.ball().vx() > 0.6 * Math.hypot(game.ball().vx(), game.ball().vy()), "steeply right");
    }
    // --8<-- [end:test-angle]

    @Test
    void threeLostBallsEndTheGame() {
        Game game = game(row("r"));
        List<Event> events = new ArrayList<>();
        for (int life = 0; life < Game.LIVES; life++) {
            fly(game, 300, 300, 0, 400);  // straight down, beside the paddle
            game.paddle().move(-1, 1);
            events.addAll(run(game, 0.5, STEP));
        }
        assertEquals(Game.Phase.OVER, game.phase());
        assertEquals(new Event.BallLost(0), events.get(events.size() - 2));
        Event.GameOver over = assertInstanceOf(Event.GameOver.class, events.getLast());
        assertEquals(new GameResult(0, 1, false), over.result());
    }

    @Test
    void theLastBrickLeadsToTheNextLevel() {
        Game game = game(row("r"), row("oo"));
        fly(game, 36, 120, 0, -300);
        List<Event> events = run(game, 0.2, STEP);
        assertTrue(events.contains(new Event.LevelCleared(1)));
        assertEquals(2, game.level());
        assertEquals(2, game.bricks().size());
        assertEquals(Game.Phase.SERVING, game.phase());
    }
}
