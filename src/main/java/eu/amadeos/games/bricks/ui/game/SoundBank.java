package eu.amadeos.games.bricks.ui.game;

import eu.amadeos.gameengine.audio.Sound;
import eu.amadeos.gameengine.audio.Sound.Wave;

/** The sound effects of Bricks, made once at start: arcade tones, no recordings. */
// --8<-- [start:sounds]
final class SoundBank {
    final Sound brick = Sound.tone(Wave.SQUARE, 520, 760, 0.05, 0.22);
    final Sound silver = Sound.tone(Wave.TRIANGLE, 1400, 1300, 0.07, 0.35);
    final Sound gold = Sound.tone(Wave.SQUARE, 1800, 1700, 0.04, 0.15)
            .then(Sound.tone(Wave.TRIANGLE, 1800, 1750, 0.08, 0.25));
    final Sound paddle = Sound.tone(Wave.SQUARE, 330, 330, 0.05, 0.22);
    final Sound wall = Sound.tone(Wave.SQUARE, 220, 220, 0.03, 0.15);
    final Sound lost = Sound.tone(Wave.SQUARE, 440, 110, 0.6, 0.2);
    /** C, E, G, high C. */
    final Sound cleared = note(523, 0.1).then(note(659, 0.1)).then(note(784, 0.1)).then(note(1047, 0.3));
    /** G, E, C, falling. */
    final Sound gameOver = note(392, 0.2).then(note(330, 0.2)).then(Sound.tone(Wave.TRIANGLE, 262, 250, 0.5, 0.3));
    final Sound crash = Sound.tone(Wave.NOISE, 0, 0, 0.12, 0.12);

    private static Sound note(double hz, double seconds) {
        return Sound.tone(Wave.TRIANGLE, hz, hz, seconds, 0.3);
    }
}
// --8<-- [end:sounds]
