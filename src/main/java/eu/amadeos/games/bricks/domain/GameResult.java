package eu.amadeos.games.bricks.domain;

/** How a game ended: the points, the level reached (from 1) and whether all levels were cleared. */
public record GameResult(int score, int level, boolean won) {
}
