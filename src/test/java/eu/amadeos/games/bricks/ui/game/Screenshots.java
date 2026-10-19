package eu.amadeos.games.bricks.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.games.bricks.bootstrap.BricksModule;
import eu.amadeos.games.bricks.domain.Field;
import eu.amadeos.games.bricks.domain.Game;
import eu.amadeos.games.bricks.repository.BricksSettings;
import eu.amadeos.games.bricks.repository.LevelFiles;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;
import java.util.SplittableRandom;

/**
 * The screenshots of chapter 21, drawn without a screen: the title, the demo in full swing, the start of
 * a game. {@code ./gradlew screenshots -Pshots=<folder>}.
 */
public final class Screenshots {
    private static final double STEP = 1.0 / BricksGame.UPDATES_PER_SECOND;

    private final long[] now = {0};
    private final Keyboard keyboard = new Keyboard(() -> now[0]);
    private final BricksGame game;
    private final Hud hud;

    private Screenshots() throws Exception {
        Messages msg = Messages.load(BricksModule.class, BricksModule.MESSAGES, Locale.ENGLISH);
        game = new BricksGame(keyboard, new Mouse(), msg,
                new BricksSettings(Files.createTempDirectory("bricks").resolve("bricks.properties")),
                LevelFiles.load(), new SplittableRandom(11), new Mixer(), Runnable::run);
        hud = new Hud(msg);
    }

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "screenshots");
        dir.mkdirs();
        Screenshots shots = new Screenshots();
        shots.steps(6 * 120);
        shots.save(new File(dir, "title.png"), true);
        // The demo, without the title's texts but with the score: a game in full swing.
        BricksGame.TitleScene title = (BricksGame.TitleScene) shots.game.top();
        for (int i = 0; i < 600 * 120; i++) {
            shots.steps(1);
            Game demo = title.demo();
            if (demo.level() == 1 && demo.bricks().size() < 50 && shots.game.effects().particles().count() > 70) break;
        }
        shots.save(new File(dir, "play.png"), false);
        shots.tap(KeyEvent.VK_ENTER);
        shots.save(new File(dir, "serve.png"), true);
        System.exit(0);
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

    /** Twice the logical size, as on a 1280 × 800 screen. */
    private void save(File file, boolean overlay) throws Exception {
        BufferedImage image = new BufferedImage(2 * (int) Field.WIDTH, 2 * (int) Field.HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.scale(2, 2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        game.render(g, 0);
        if (overlay) {
            game.renderOverlay(g, 0);
        } else if (game.top() instanceof BricksGame.TitleScene title) {
            hud.status(g, title.demo());
        }
        g.dispose();
        ImageIO.write(image, "png", file);
    }
}
