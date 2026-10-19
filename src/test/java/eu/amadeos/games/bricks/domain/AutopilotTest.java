package eu.amadeos.games.bricks.domain;

import eu.amadeos.games.bricks.repository.LevelFiles;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AutopilotTest {
    private static final double STEP = 1.0 / 120;

    // --8<-- [start:test-demo]
    /** Ten minutes of the title screen's demo on the real levels, in well under a second. */
    @Test
    void theDemoKeepsTheBallInTheFieldAndBreaksBricks() {
        List<Level> levels = LevelFiles.load();
        SplittableRandom random = new SplittableRandom(7);
        Game game = new Game(levels, random);
        Autopilot pilot = new Autopilot(random);
        int broken = 0;
        int lost = 0;
        int highestLevel = 1;
        for (int step = 0; step < 10 * 60 * 120; step++) {
            game.update(STEP, pilot.move(game), pilot.launch(game, STEP));
            for (Event e : game.events()) {
                if (e instanceof Event.BrickHit hit && hit.broken()) broken++;
                if (e instanceof Event.BallLost) lost++;
            }
            Ball b = game.ball();
            assertTrue(b.x() >= Field.LEFT - 1e-6 && b.x() + Field.BALL_SIZE <= Field.RIGHT + 1e-6,
                    "ball left the field at x " + b.x());
            assertTrue(b.y() >= Field.TOP - 1e-6, "ball went through the top wall: " + b.y());
            highestLevel = Math.max(highestLevel, game.level());
            if (game.phase() == Game.Phase.OVER) game = new Game(levels, random);
        }
        System.out.printf("demo: %d bricks broken, %d balls lost, up to level %d%n", broken, lost, highestLevel);
        assertTrue(broken > 200, "bricks broken: " + broken);
        assertTrue(highestLevel >= 2, "reached level " + highestLevel);
    }
    // --8<-- [end:test-demo]

    @Test
    void theArrivalIsFoldedAtTheWalls() {
        Ball ball = new Ball();
        // Falling to the right at 45°, 100 units above the paddle: 100 units further right, beyond the wall.
        ball.place(560, Field.PADDLE_Y - Field.BALL_SIZE - 100);
        ball.setVelocity(300, 300);
        double wall = Field.RIGHT - Field.BALL_SIZE / 2;
        double straight = ball.centerX() + 100;
        assertTrue(Math.abs(Autopilot.arrival(ball) - (2 * wall - straight)) < 1e-9);
    }
}
