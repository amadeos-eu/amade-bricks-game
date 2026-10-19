package eu.amadeos.games.bricks.repository;

import eu.amadeos.games.bricks.domain.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The levels, as text files among the game's resources: {@code levels/01.txt}, {@code 02.txt} and so on,
 * read until a number is missing. A new level is a new file.
 */
// --8<-- [start:level-files]
public final class LevelFiles {
    private static final String FOLDER = "/eu/amadeos/games/bricks/levels/";

    private LevelFiles() {
    }

    public static List<Level> load() {
        List<Level> levels = new ArrayList<>();
        for (int n = 1; ; n++) {
            String name = FOLDER + String.format("%02d.txt", n);
            try (InputStream in = LevelFiles.class.getResourceAsStream(name)) {
                if (in == null) break;
                levels.add(Level.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new UncheckedIOException("cannot read " + name, e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(name + ": " + e.getMessage(), e);
            }
        }
        if (levels.isEmpty()) throw new IllegalStateException("no levels in " + FOLDER);
        return levels;
    }
}
// --8<-- [end:level-files]
