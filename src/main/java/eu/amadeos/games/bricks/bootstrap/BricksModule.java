package eu.amadeos.games.bricks.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import eu.amadeos.commons.app.AppInfo;
import eu.amadeos.commons.config.AppDirectories;
import eu.amadeos.commons.config.XdgDirectories;
import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.score.HighScores;
import eu.amadeos.gameengine.score.PlayerName;
import eu.amadeos.gameengine.score.Ranking;
import eu.amadeos.games.bricks.domain.Level;
import eu.amadeos.games.bricks.repository.BricksSettings;
import eu.amadeos.games.bricks.repository.LevelFiles;
import eu.amadeos.games.bricks.ui.game.Arena;
import eu.amadeos.games.bricks.ui.game.BricksGame;
import eu.amadeos.games.bricks.ui.main.BricksPresenter;
import eu.amadeos.games.bricks.ui.main.BricksView;
import eu.amadeos.games.bricks.ui.main.BricksWindow;

import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;

/**
 * amade-bricks-game bindings. Theme, language and the XDG directories come from
 * {@link eu.amadeos.commons.app.CommonsModule}.
 */
public final class BricksModule extends AbstractModule {
    /** The texts, {@code messages.properties} and its translations. */
    public static final String MESSAGES = "eu.amadeos.games.bricks.messages";

    @Override
    protected void configure() {
        bind(BricksSettings.class).in(Scopes.SINGLETON);
        bind(Keyboard.class).in(Scopes.SINGLETON);
        bind(Mixer.class).in(Scopes.SINGLETON);
        bind(BricksGame.class).in(Scopes.SINGLETON);
        bind(Arena.class).to(BricksGame.class);
        bind(BricksWindow.class).in(Scopes.SINGLETON);
        bind(BricksView.class).to(BricksWindow.class);
        bind(BricksPresenter.class).in(Scopes.SINGLETON);
    }

    /** The best scores in {@code ~/.local/share/amadeos/games/bricks}. */
    @Provides
    @Singleton
    HighScores highScores(XdgDirectories xdg, AppInfo app) {
        return HighScores.of(xdg, app.configName(), Ranking.HIGHER_WINS);
    }

    @Provides
    @Singleton
    PlayerName playerName(AppDirectories directories) {
        return new PlayerName(directories.config(), System.getProperty("user.name"));
    }

    @Provides
    @Singleton
    List<Level> levels() {
        return LevelFiles.load();
    }

    @Provides
    RandomGenerator random() {
        return RandomGenerator.getDefault();
    }

    /** The texts in the language {@code AmadeApplication} chose. */
    @Provides
    @Singleton
    Messages messages(Locale locale) {
        return Messages.load(BricksModule.class, MESSAGES, locale);
    }
}
