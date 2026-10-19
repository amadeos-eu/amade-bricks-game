package eu.amadeos.games.bricks.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTest {
    // --8<-- [start:test-level]
    @Test
    void aLevelIsReadFromText() {
        Level level = Level.parse("""
                # comment
                r.............X
                ..s............
                """);
        List<Brick> bricks = level.bricks();
        assertEquals(3, bricks.size());
        assertEquals(BrickType.RED, bricks.get(0).type());
        assertEquals(Field.LEFT, bricks.get(0).box().x());
        assertEquals(BrickType.GOLD, bricks.get(1).type());
        assertEquals(Field.LEFT + 14 * Field.BRICK_WIDTH, bricks.get(1).box().x());
        assertEquals(BrickType.SILVER, bricks.get(2).type());
        assertEquals(Field.BRICKS_TOP + Field.BRICK_HEIGHT, bricks.get(2).box().y());
    }

    @Test
    void mistakesInALevelAreNamed() {
        var e = assertThrows(IllegalArgumentException.class, () -> Level.parse("rrr"));
        assertTrue(e.getMessage().contains("row 1 has 3 bricks instead of 15"), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> Level.parse("rrrrrrrrrrrrrrq"));
        assertTrue(e.getMessage().contains("unknown brick 'q'"), e.getMessage());
    }
    // --8<-- [end:test-level]

    @Test
    void theRightmostBrickTouchesTheRightWall() {
        Brick last = Level.parse("..............r").bricks().getFirst();
        assertEquals(Field.RIGHT, last.box().right(), 1e-9);
    }
}
