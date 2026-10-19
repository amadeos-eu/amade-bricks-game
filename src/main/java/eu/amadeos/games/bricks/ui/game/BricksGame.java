package eu.amadeos.games.bricks.ui.game;

import com.google.inject.Inject;
import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.commons.swing.EdtExecutor;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Controls;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.loop.GameLoop;
import eu.amadeos.gameengine.scene.Scene;
import eu.amadeos.gameengine.scene.SceneStack;
import eu.amadeos.gameengine.swing.GameCanvas;
import eu.amadeos.games.bricks.domain.Autopilot;
import eu.amadeos.games.bricks.domain.Event;
import eu.amadeos.games.bricks.domain.Game;
import eu.amadeos.games.bricks.domain.GameResult;
import eu.amadeos.games.bricks.domain.Level;
import eu.amadeos.games.bricks.repository.BricksSettings;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

/**
 * The running game: the loop, the controls, the effects and the scenes (title with the computer playing,
 * a game, the pause screen, the end of a game). Everything here runs on the game loop thread, except the
 * {@link Arena} methods, which only post commands to it.
 */
public final class BricksGame implements Arena {
    /** The actions of the game; which keys trigger them comes from the settings. */
    public enum Action { LEFT, RIGHT, LAUNCH, PAUSE, START }

    /** Steps of the rules per second. */
    static final int UPDATES_PER_SECOND = 120;
    static final int FRAMES_PER_SECOND = 60;

    private final Keyboard keyboard;
    private final Mouse mouse;
    private final Controls<Action> controls;
    private final Messages msg;
    private final List<Level> levels;
    private final RandomGenerator random;
    private final Executor ui;
    private final Mixer mixer;
    private final FieldPainter field = new FieldPainter();
    private final Hud hud;
    private final Effects effects;
    private final SceneStack scenes = new SceneStack();
    private volatile Consumer<GameResult> onGameOver = result -> { };
    private GameLoop loop;

    @Inject
    public BricksGame(Keyboard keyboard, Mouse mouse, Messages msg, BricksSettings settings, List<Level> levels,
                      RandomGenerator random, Mixer mixer, EdtExecutor ui) {
        this(keyboard, mouse, msg, settings, levels, random, mixer, (Executor) ui);
    }

    BricksGame(Keyboard keyboard, Mouse mouse, Messages msg, BricksSettings settings, List<Level> levels,
               RandomGenerator random, Mixer mixer, Executor ui) {
        this.keyboard = keyboard;
        this.mouse = mouse;
        this.msg = msg;
        this.levels = levels;
        this.random = random;
        this.mixer = mixer;
        this.ui = ui;
        this.hud = new Hud(msg);
        this.effects = new Effects(mixer, random);
        controls = new Controls<>(Action.class, keyboard)
                .bind(Action.LEFT, settings.keys("left", KeyEvent.VK_LEFT, KeyEvent.VK_A))
                .bind(Action.RIGHT, settings.keys("right", KeyEvent.VK_RIGHT, KeyEvent.VK_D))
                .bind(Action.LAUNCH, settings.keys("launch", KeyEvent.VK_SPACE, KeyEvent.VK_UP))
                .bind(Action.PAUSE, settings.keys("pause", KeyEvent.VK_P, KeyEvent.VK_ESCAPE))
                .bind(Action.START, KeyEvent.VK_ENTER, KeyEvent.VK_SPACE);
        scenes.push(new TitleScene());
    }

    // --8<-- [start:start]
    /** Starts the loop; the canvas draws the scenes' worlds and then their overlays. */
    public void start(GameCanvas canvas) {
        loop = new GameLoop(UPDATES_PER_SECOND, FRAMES_PER_SECOND, this::update, canvas::render);
        loop.start();
    }
    // --8<-- [end:start]

    public void stop() {
        if (loop != null) loop.stop();
    }

    void update(double seconds) {
        keyboard.poll();
        mouse.poll();
        scenes.update(seconds);
    }

    /** Enter, Space or a click: on to the next screen. */
    private boolean proceed() {
        return controls.wasPressed(Action.START) || mouse.wasPressed(MouseEvent.BUTTON1);
    }

    /** The world of all scenes, in logical coordinates. */
    public void render(Graphics2D g, double alpha) {
        scenes.render(g, alpha);
    }

    /** Score and texts of all scenes, drawn after the world at the screen's full size. */
    public void renderOverlay(Graphics2D g, double alpha) {
        scenes.renderOverlay(g, alpha);
    }

    // ---- Arena: called on the Swing thread, carried out on the loop thread -----------------------

    @Override
    public void showTitle() {
        run(() -> scenes.replace(new TitleScene()));
    }

    @Override
    public void newGame() {
        run(() -> scenes.replace(new PlayScene()));
    }

    @Override
    public void pause() {
        run(() -> {
            if (scenes.top() instanceof PlayScene) scenes.push(new PauseScene());
        });
    }

    @Override
    public void setSound(boolean on) {
        mixer.setMuted(!on);
    }

    @Override
    public void onGameOver(Consumer<GameResult> listener) {
        onGameOver = listener;
    }

    /** On the loop thread when it runs; directly before the loop starts and in tests. */
    private void run(Runnable command) {
        if (loop != null && loop.running()) loop.post(command);
        else command.run();
    }

    // ---- for tests -------------------------------------------------------------------------------

    Scene top() {
        return scenes.top();
    }

    int sceneCount() {
        return scenes.size();
    }

    Effects effects() {
        return effects;
    }

    // ---- the scenes ------------------------------------------------------------------------------

    /** The computer plays, silently; Enter, Space or a click starts a game. */
    final class TitleScene implements Scene {
        private Game demo = new Game(levels, random);
        private final Autopilot pilot = new Autopilot(random);

        TitleScene() {
            effects.clear();
        }

        @Override
        public void update(double seconds) {
            if (proceed()) {
                scenes.replace(new PlayScene());
                return;
            }
            demo.update(seconds, pilot.move(demo), pilot.launch(demo, seconds));
            effects.react(demo.events(), false);
            effects.update(seconds);
            if (demo.phase() == Game.Phase.OVER) demo = new Game(levels, random);
        }

        @Override
        public void render(Graphics2D g, double alpha) {
            field.paint(g, demo, effects.particles(), alpha);
        }

        @Override
        public void renderOverlay(Graphics2D g, double alpha) {
            hud.veil(g);
            hud.text(g, msg.get("window.title"), 150, 72, true, Hud.GOLD);
            hud.text(g, msg.get("title.start"), 225, 18, false, Hud.CHALK);
            hud.text(g, msg.get("title.keys"), 300, 13, false, Hud.CHALK);
            hud.text(g, msg.get("title.screen"), 322, 13, false, Hud.CHALK);
        }

        Game demo() {
            return demo;
        }
    }

    // --8<-- [start:play]
    /** A game: the paddle follows the keys or the mouse, the events become splinters and sounds. */
    final class PlayScene implements Scene {
        private final Game game = new Game(levels, random);

        PlayScene() {
            effects.clear();
        }

        @Override
        public void update(double seconds) {
            if (controls.wasPressed(Action.PAUSE)) {
                scenes.push(new PauseScene());
                return;
            }
            if (mouse.wasMoved()) game.aim(mouse.x());
            boolean launch = controls.wasPressed(Action.LAUNCH) || mouse.wasPressed(MouseEvent.BUTTON1);
            game.update(seconds, controls.axis(Action.LEFT, Action.RIGHT), launch);
            effects.react(game.events(), true);
            effects.update(seconds);
            for (Event event : game.events()) {
                if (event instanceof Event.GameOver(GameResult result)) {
                    scenes.replace(new OverScene(game, result));
                    // The presenter lives on the Swing thread: hand the result over there.
                    Consumer<GameResult> listener = onGameOver;
                    ui.execute(() -> listener.accept(result));
                }
            }
        }

        @Override
        public void render(Graphics2D g, double alpha) {
            field.paint(g, game, effects.particles(), alpha);
        }

        @Override
        public void renderOverlay(Graphics2D g, double alpha) {
            hud.status(g, game);
            if (game.phase() == Game.Phase.SERVING) {
                hud.text(g, msg.format("play.level", game.level()), 300, 22, true, Hud.GOLD);
                hud.text(g, msg.get("play.launch"), 328, 14, false, Hud.CHALK);
            }
        }

        Game game() {
            return game;
        }
    }
    // --8<-- [end:play]

    /** Drawn over the frozen game until P, Esc, Enter, Space or a click. */
    final class PauseScene implements Scene {
        @Override
        public void update(double seconds) {
            if (controls.wasPressed(Action.PAUSE) || proceed()) scenes.pop();
        }

        @Override
        public void render(Graphics2D g, double alpha) {
        }

        @Override
        public void renderOverlay(Graphics2D g, double alpha) {
            hud.veil(g);
            hud.text(g, msg.get("pause.title"), 200, 40, true, Hud.CHALK);
            hud.text(g, msg.get("pause.continue"), 245, 18, false, Hud.CHALK);
        }
    }

    /** The end: the splinters settle, the score stays, Enter, Space or a click leads back to the title. */
    final class OverScene implements Scene {
        private final Game game;
        private final GameResult result;
        private double shown;

        OverScene(Game game, GameResult result) {
            this.game = game;
            this.result = result;
        }

        @Override
        public void update(double seconds) {
            effects.update(seconds);
            shown += seconds;
            // A short wait, so a Space or click meant for the ball does not skip the end.
            if (shown > 1 && proceed()) scenes.replace(new TitleScene());
        }

        @Override
        public void render(Graphics2D g, double alpha) {
            field.paint(g, game, effects.particles(), alpha);
        }

        @Override
        public void renderOverlay(Graphics2D g, double alpha) {
            hud.status(g, game);
            hud.veil(g);
            hud.text(g, msg.get(result.won() ? "over.won" : "over.lost"), 190, 40, true, Hud.GOLD);
            hud.text(g, msg.format("over.score", result.score()), 232, 20, false, Hud.CHALK);
            hud.text(g, msg.get("over.continue"), 270, 16, false, Hud.CHALK);
        }
    }
}
