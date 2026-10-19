package eu.amadeos.games.bricks.repository;

import eu.amadeos.games.bricks.domain.Level;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelFilesTest {
    @Test
    void allLevelFilesAreReadAndEachHasBricksToBreak() {
        List<Level> levels = LevelFiles.load();
        assertEquals(5, levels.size());
        for (Level level : levels) {
            assertTrue(level.bricks().stream().anyMatch(b -> b.type().breakable()));
        }
    }
}
