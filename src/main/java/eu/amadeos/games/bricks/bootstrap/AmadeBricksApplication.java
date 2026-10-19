package eu.amadeos.games.bricks.bootstrap;

import eu.amadeos.commons.app.AmadeApplication;
import eu.amadeos.commons.app.AppInfo;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.games.bricks.ui.game.BricksGame;
import eu.amadeos.games.bricks.ui.main.BricksPresenter;
import eu.amadeos.games.bricks.ui.main.BricksWindow;

public final class AmadeBricksApplication {
    public static final AppInfo APP = new AppInfo("amade-bricks-game", "Bricks", "bricks");

    private AmadeBricksApplication() {
    }

    /** The version from the jar's manifest; "dev" when run from the IDE. */
    public static String version() {
        String version = AmadeBricksApplication.class.getPackage().getImplementationVersion();
        return version == null ? "dev" : version;
    }

    // --8<-- [start:main]
    public static void main(String[] args) {
        AmadeApplication.launch(APP, injector -> {
            Mixer mixer = injector.getInstance(Mixer.class);
            mixer.start();
            BricksWindow window = injector.getInstance(BricksWindow.class);
            BricksPresenter presenter = injector.getInstance(BricksPresenter.class);
            BricksGame game = injector.getInstance(BricksGame.class);
            window.bind(presenter);
            presenter.start();
            window.whenClosed(() -> {
                game.stop();
                mixer.close();
                System.exit(0);
            });
            window.setLocationByPlatform(true);
            window.setVisible(true);
            game.start(window.canvas());
        }, new BricksModule());
    }
    // --8<-- [end:main]
}
