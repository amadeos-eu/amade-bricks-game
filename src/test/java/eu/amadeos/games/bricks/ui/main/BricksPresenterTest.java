package eu.amadeos.games.bricks.ui.main;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.score.HighScores;
import eu.amadeos.gameengine.score.Ranking;
import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.bricks.bootstrap.BricksModule;
import eu.amadeos.games.bricks.domain.GameResult;
import eu.amadeos.games.bricks.repository.BricksSettings;
import eu.amadeos.games.bricks.ui.game.Arena;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BricksPresenterTest {
    @TempDir
    Path dir;

    private final List<String> log = new ArrayList<>();
    private Consumer<GameResult> gameOver;
    private BricksPresenter presenter;
    private HighScores scores;
    private BricksSettings settings;

    private final BricksView view = new BricksView() {
        @Override
        public void showOptions(boolean sound, RenderMode mode) {
            log.add("options " + sound + " " + mode);
        }

        @Override
        public void useRenderMode(RenderMode mode) {
            log.add("mode " + mode);
        }

        @Override
        public Optional<String> askName(String message) {
            log.add("ask " + message);
            return Optional.of("Ada");
        }

        @Override
        public void showScores(int highlight) {
            log.add("scores " + highlight);
        }

        @Override
        public void showAbout() {
        }
    };

    private final Arena arena = new Arena() {
        @Override
        public void showTitle() {
            log.add("title");
        }

        @Override
        public void newGame() {
            log.add("new");
        }

        @Override
        public void pause() {
            log.add("pause");
        }

        @Override
        public void setSound(boolean on) {
            log.add("sound " + on);
        }

        @Override
        public void onGameOver(Consumer<GameResult> listener) {
            gameOver = listener;
        }
    };

    @BeforeEach
    void create() {
        Messages msg = Messages.load(BricksModule.class, BricksModule.MESSAGES, Locale.ENGLISH);
        settings = new BricksSettings(dir.resolve("bricks.properties"));
        scores = new HighScores(dir.resolve("scores"), Ranking.HIGHER_WINS);
        presenter = new BricksPresenter(view, arena, msg, settings, scores, () -> Instant.EPOCH);
        presenter.start();
    }

    @Test
    void theSettingsAreAppliedAtStartAndKept() {
        assertEquals(List.of("options true DIRECT", "mode DIRECT", "sound true", "title"), log);
        log.clear();
        presenter.setRenderMode(RenderMode.PIXEL);
        presenter.setSound(false);
        assertEquals(List.of("mode PIXEL", "sound false"), log);
        assertEquals(RenderMode.PIXEL, settings.renderMode());
    }

    @Test
    void aGoodScoreEntersTheTable() {
        log.clear();
        gameOver.accept(new GameResult(1230, 2, false));
        assertEquals(List.of("ask 1,230 points, up to level 2.", "scores 0"), log);
        assertEquals("Ada", scores.list(BricksPresenter.TABLE).getFirst().name());
    }

    @Test
    void noPointsNoEntry() {
        log.clear();
        gameOver.accept(new GameResult(0, 1, false));
        assertEquals(List.of(), log);
    }
}
