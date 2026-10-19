package eu.amadeos.games.bricks.domain;

/**
 * Something that happened during a step. The rules only report it; what it looks and sounds like (splinters,
 * a blip) is decided by the user interface, which reads the events after every step.
 */
// --8<-- [start:event]
public sealed interface Event {
    /** The ball hit a brick; {@code broken} if that was its last hit. */
    record BrickHit(Brick brick, boolean broken) implements Event {
    }

    /** The ball bounced off a wall or the paddle. */
    record Bounce(boolean paddle) implements Event {
    }

    record BallLost(int livesLeft) implements Event {
    }

    /** The last breakable brick of a level broke; {@code level} counts from 1. */
    record LevelCleared(int level) implements Event {
    }

    record GameOver(GameResult result) implements Event {
    }
}
// --8<-- [end:event]
