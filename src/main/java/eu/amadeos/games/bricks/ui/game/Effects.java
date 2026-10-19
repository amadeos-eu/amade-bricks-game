package eu.amadeos.games.bricks.ui.game;

import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.audio.Sound;
import eu.amadeos.gameengine.collision.Box;
import eu.amadeos.gameengine.fx.Particles;
import eu.amadeos.games.bricks.domain.BrickType;
import eu.amadeos.games.bricks.domain.Event;

import java.awt.Color;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * What the events of a step look and sound like: splinters and sparks as particles, blips from the mixer.
 * The rules know nothing of this; they only report what happened.
 */
final class Effects {
    /** The particle colour of each brick type, by its ordinal, and white sparks after them. */
    private static final Color[] PALETTE = {new Color(0xD83A3A), new Color(0xE8862A), new Color(0xE8D23A),
            new Color(0x4CB848), new Color(0x3A7AD8), new Color(0x9A4AD0), new Color(0xC0C8D0),
            new Color(0xD4A82A), Color.WHITE};
    private static final int SPARK = PALETTE.length - 1;
    /** Splinters fall: units per second². */
    private static final double GRAVITY = 600;

    private final Particles particles = new Particles(4000, PALETTE);
    private final SoundBank sounds = new SoundBank();
    private final Mixer mixer;
    private final RandomGenerator random;

    Effects(Mixer mixer, RandomGenerator random) {
        this.mixer = mixer;
        this.random = random;
    }

    Particles particles() {
        return particles;
    }

    void update(double seconds) {
        particles.update(seconds, GRAVITY);
    }

    void clear() {
        particles.clear();
    }

    // --8<-- [start:react]
    /** Shows and, if {@code audible}, plays what happened in the last step. */
    void react(List<Event> events, boolean audible) {
        for (Event event : events) {
            switch (event) {
                case Event.BrickHit(var brick, var broken) when broken -> {
                    splinters(brick.box(), brick.type().ordinal(), 40);
                    if (audible) play(brick.type() == BrickType.SILVER ? sounds.silver : sounds.brick);
                }
                case Event.BrickHit(var brick, var broken) -> {
                    sparks(brick.box());
                    if (audible) play(brick.type() == BrickType.GOLD ? sounds.gold : sounds.silver);
                }
                case Event.Bounce(var paddle) -> {
                    if (audible) play(paddle ? sounds.paddle : sounds.wall);
                }
                case Event.BallLost lost -> {
                    if (audible) play(lost.livesLeft() > 0 ? sounds.lost : sounds.crash);
                }
                case Event.LevelCleared cleared -> {
                    if (audible) play(sounds.cleared);
                }
                case Event.GameOver over -> {
                    if (audible) play(over.result().won() ? sounds.cleared : sounds.gameOver);
                }
            }
        }
    }
    // --8<-- [end:react]

    private void play(Sound sound) {
        mixer.play(sound);
    }

    // --8<-- [start:splinters]
    /** A broken brick bursts into {@code count} splinters of its colour that fly off and fall. */
    private void splinters(Box box, int colour, int count) {
        for (int i = 0; i < count; i++) {
            double x = box.x() + random.nextDouble() * box.width();
            double y = box.y() + random.nextDouble() * box.height();
            double vx = (x - box.centerX()) * 6 + (random.nextDouble() - 0.5) * 80;
            double vy = -60 - random.nextDouble() * 140;
            particles.spawn(x, y, vx, vy, 0.6 + random.nextDouble() * 0.7, 1.5 + random.nextDouble() * 2.5, colour);
        }
    }
    // --8<-- [end:splinters]

    /** A hit that does not break: a few white sparks. */
    private void sparks(Box box) {
        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 80 + random.nextDouble() * 120;
            particles.spawn(box.centerX() + Math.cos(angle) * box.width() / 3, box.centerY(),
                    Math.cos(angle) * speed, Math.sin(angle) * speed, 0.25 + random.nextDouble() * 0.2, 1.5, SPARK);
        }
    }
}
