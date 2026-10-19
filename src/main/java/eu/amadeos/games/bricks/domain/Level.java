package eu.amadeos.games.bricks.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A wall of bricks, read from a level file: one line per row, one letter per brick (see {@link BrickType}),
 * a dot for a gap. Lines beginning with {@code #} are comments.
 *
 * <pre>
 * # Level 1
 * rrrrrrrrrrrrrrr
 * ooooooooooooooo
 * ...s.......s...
 * </pre>
 *
 * <p>A level is data, not code: a new one needs no new program, only a new file.
 */
// --8<-- [start:level]
public record Level(List<String> rows) {
    public Level {
        rows = List.copyOf(rows);
        if (rows.isEmpty() || rows.size() > Field.MAX_ROWS) {
            throw new IllegalArgumentException("a level has 1 to " + Field.MAX_ROWS + " rows, not " + rows.size());
        }
        for (int r = 0; r < rows.size(); r++) {
            String row = rows.get(r);
            if (row.length() != Field.COLUMNS) {
                throw new IllegalArgumentException("row " + (r + 1) + " has " + row.length()
                        + " bricks instead of " + Field.COLUMNS);
            }
            for (char c : row.toCharArray()) {
                if (c != '.' && BrickType.ofLetter(c).isEmpty()) {
                    throw new IllegalArgumentException("row " + (r + 1) + ": unknown brick '" + c + "'");
                }
            }
        }
    }

    /** Reads a level file's text. */
    public static Level parse(String text) {
        List<String> rows = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String row = line.strip();
            if (row.isEmpty() || row.startsWith("#")) continue;
            rows.add(row);
        }
        return new Level(rows);
    }

    /** New bricks for a game of this level. */
    public List<Brick> bricks() {
        List<Brick> bricks = new ArrayList<>();
        for (int r = 0; r < rows.size(); r++) {
            for (int c = 0; c < Field.COLUMNS; c++) {
                int column = c;
                int row = r;
                BrickType.ofLetter(rows.get(r).charAt(c)).ifPresent(type -> bricks.add(new Brick(column, row, type)));
            }
        }
        return bricks;
    }
}
// --8<-- [end:level]
