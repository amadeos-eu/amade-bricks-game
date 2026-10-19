package eu.amadeos.games.bricks.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.games.bricks.bootstrap.BricksModule;
import eu.amadeos.games.bricks.domain.Field;
import eu.amadeos.games.bricks.domain.Game;
import eu.amadeos.games.bricks.domain.GameResult;
import eu.amadeos.games.bricks.repository.BricksSettings;
import eu.amadeos.games.bricks.repository.LevelFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The game without a window and without the loop thread: the test presses keys on the keyboard and calls
 * the steps itself, so everything happens in order on the test thread.
 */
class BricksGameTest {
    private static final double STEP = 1.0 / BricksGame.UPDATES_PER_SECOND;

    @TempDir
    Path dir;

    private final long[] now = {0};
    private final Keyboard keyboard = new Keyboard(() -> now[0]);
    private final Mouse mouse = new Mouse();
    private final List<GameResult> results = new ArrayList<>();
    private BricksGame game;

    @BeforeEach
    void create() {
        Messages msg = Messages.load(BricksModule.class, BricksModule.MESSAGES, Locale.ENGLISH);
        game = new BricksGame(keyboard, mouse, msg, new BricksSettings(dir.resolve("bricks.properties")), LevelFiles.load(),
                new SplittableRandom(4), new Mixer(), Runnable::run);
        game.onGameOver(results::add);
    }

    private void tap(int key) {
        keyboard.pressed(key, now[0]);
        keyboard.released(key, now[0] + 1_000_000);
        steps(4);
    }

    private void steps(int n) {
        for (int i = 0; i < n; i++) {
            now[0] += (long) (STEP * 1e9);
            game.update(STEP);
        }
    }

    @Test
    void enterStartsAGameSpaceLaunchesTheArrowsMove() {
        assertInstanceOf(BricksGame.TitleScene.class, game.top());
        tap(KeyEvent.VK_ENTER);
        BricksGame.PlayScene play = assertInstanceOf(BricksGame.PlayScene.class, game.top());
        assertEquals(Game.Phase.SERVING, play.game().phase());
        double x = play.game().paddle().x();
        keyboard.pressed(KeyEvent.VK_RIGHT, now[0]);
        steps(30);
        keyboard.released(KeyEvent.VK_RIGHT, now[0]);
        steps(4);
        assertTrue(play.game().paddle().x() > x + 50, "moved right");
        tap(KeyEvent.VK_SPACE);
        assertEquals(Game.Phase.PLAYING, play.game().phase());
    }

    // --8<-- [start:test-mouse]
    @Test
    void aClickStartsAndLaunchesThePaddleFollowsTheMouse() {
        mouse.pressed(MouseEvent.BUTTON1);
        steps(1);
        BricksGame.PlayScene play = assertInstanceOf(BricksGame.PlayScene.class, game.top());
        mouse.moved(100, 300);
        steps(60);
        assertEquals(100, play.game().paddle().centerX(), 1e-9);
        // The keys take over until the mouse moves again.
        keyboard.pressed(KeyEvent.VK_RIGHT, now[0]);
        steps(30);
        keyboard.released(KeyEvent.VK_RIGHT, now[0]);
        steps(4);
        assertTrue(play.game().paddle().centerX() > 150, "the keys moved it");
        mouse.pressed(MouseEvent.BUTTON1);
        steps(1);
        assertEquals(Game.Phase.PLAYING, play.game().phase());
    }
    // --8<-- [end:test-mouse]

    @Test
    void pFreezesTheGameAndContinuesIt() {
        tap(KeyEvent.VK_ENTER);
        tap(KeyEvent.VK_SPACE);
        BricksGame.PlayScene play = assertInstanceOf(BricksGame.PlayScene.class, game.top());
        tap(KeyEvent.VK_P);
        assertEquals(2, game.sceneCount());
        double y = play.game().ball().y();
        steps(60);
        assertEquals(y, play.game().ball().y());
        tap(KeyEvent.VK_P);
        steps(10);
        assertTrue(play.game().ball().y() < y, "flies on");
    }

    @Test
    void theDemoBreaksBricksAndMakesSplinters() {
        boolean splinters = false;
        for (int i = 0; i < 20 * 120 && !splinters; i++) {
            steps(1);
            splinters = game.effects().particles().count() > 0;
        }
        assertTrue(splinters, "a brick broke into splinters");
    }

    @Test
    void everySceneDrawsWorldAndOverlay() {
        BufferedImage image = new BufferedImage((int) Field.WIDTH, (int) Field.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        steps(240);
        game.render(g, 0.5);
        game.renderOverlay(g, 0.5);
        tap(KeyEvent.VK_ENTER);
        tap(KeyEvent.VK_P);
        game.render(g, 0.5);
        game.renderOverlay(g, 0.5);
        g.dispose();
        // The brick wall's left wall pipe is not black.
        assertTrue((image.getRGB(10, 200) & 0xFFFFFF) != 0);
    }
}
