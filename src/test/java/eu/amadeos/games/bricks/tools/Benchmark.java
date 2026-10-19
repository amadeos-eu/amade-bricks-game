package eu.amadeos.games.bricks.tools;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.commons.swing.EdtExecutor;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.loop.FrameTimes;
import eu.amadeos.gameengine.score.HighScores;
import eu.amadeos.gameengine.score.PlayerName;
import eu.amadeos.gameengine.score.Ranking;
import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.bricks.bootstrap.BricksModule;
import eu.amadeos.games.bricks.repository.BricksSettings;
import eu.amadeos.games.bricks.repository.LevelFiles;
import eu.amadeos.games.bricks.ui.game.BricksGame;
import eu.amadeos.games.bricks.ui.main.BricksPresenter;
import eu.amadeos.games.bricks.ui.main.BricksWindow;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.SplittableRandom;

/**
 * Measures how long Bricks takes per frame in full screen, in every render mode, while the title screen's
 * demo plays: the numbers of chapter 21. Run it on the screen you want to measure:
 *
 * <pre>
 * ./gradlew benchmark                       # all modes, 10 seconds each
 * ./gradlew benchmark -Pmodes=pixel -Pseconds=20
 * </pre>
 *
 * It prints one line per mode: the screen, the mode, the average and the worst drawing time per frame and
 * the frames per second, each averaged over the measuring time.
 */
public final class Benchmark {
    private Benchmark() {
    }

    public static void main(String[] args) throws Exception {
        String[] modes = (args.length > 0 ? args[0] : "direct,pixel").split(",");
        int seconds = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        File shots = args.length > 2 ? new File(args[2]) : null;
        Path dir = Files.createTempDirectory("bricks-benchmark");
        Messages msg = Messages.load(BricksModule.class, BricksModule.MESSAGES, Locale.ENGLISH);
        BricksSettings settings = new BricksSettings(dir.resolve("bricks.properties"));
        HighScores scores = new HighScores(dir.resolve("scores"), Ranking.HIGHER_WINS);
        Keyboard keyboard = new Keyboard();
        BricksWindow[] window = new BricksWindow[1];
        BricksGame[] game = new BricksGame[1];
        SwingUtilities.invokeAndWait(() -> {
            game[0] = new BricksGame(keyboard, msg, settings, LevelFiles.load(), new SplittableRandom(21), new Mixer(),
                    new EdtExecutor());
            window[0] = new BricksWindow(msg, scores, new PlayerName(dir, "benchmark"), game[0], keyboard);
            BricksPresenter presenter = new BricksPresenter(window[0], game[0], msg, settings, scores);
            window[0].bind(presenter);
            presenter.start();
            window[0].setVisible(true);
            window[0].fullScreen().enter();
            window[0].canvas().setShowFrameTimes(true);
        });
        game[0].start(window[0].canvas());
        Thread.sleep(2000);
        for (String id : modes) {
            RenderMode mode = RenderMode.ofId(id.strip()).orElseThrow();
            window[0].canvas().setRenderMode(mode);
            Thread.sleep(2000);  // the sprites are scaled anew, the JIT warms up
            FrameTimes times = window[0].canvas().frameTimes();
            double average = 0;
            double worst = 0;
            double fps = 0;
            for (int s = 0; s < seconds; s++) {
                Thread.sleep(1000);
                average += times.averageMillis() / seconds;
                worst = Math.max(worst, times.maxMillis());
                fps += times.framesPerSecond() / (double) seconds;
            }
            Rectangle screen = window[0].getGraphicsConfiguration().getBounds();
            System.out.printf(Locale.ROOT, "| %d x %d | %s | %.1f ms | %.1f ms | %.0f |%n",
                    screen.width, screen.height, mode.id(), average, worst, fps);
            if (shots != null) {
                shots.mkdirs();
                ImageIO.write(new Robot().createScreenCapture(screen), "png",
                        new File(shots, "bricks-" + mode.id() + "-" + screen.width + "x" + screen.height + ".png"));
            }
        }
        game[0].stop();
        SwingUtilities.invokeAndWait(window[0]::dispose);
        System.exit(0);
    }
}
