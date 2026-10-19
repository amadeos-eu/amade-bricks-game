package eu.amadeos.games.bricks.domain;

import eu.amadeos.gameengine.collision.Box;

/** The ball: where it is (top left corner), where it was a step ago, and its velocity. */
public final class Ball {
    private double x;
    private double y;
    private double previousX;
    private double previousY;
    private double vx;
    private double vy;

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double previousX() {
        return previousX;
    }

    public double previousY() {
        return previousY;
    }

    public double vx() {
        return vx;
    }

    public double vy() {
        return vy;
    }

    public double centerX() {
        return x + Field.BALL_SIZE / 2;
    }

    public Box box() {
        return new Box(x, y, Field.BALL_SIZE, Field.BALL_SIZE);
    }

    /** Remembers the position for drawing between steps; call once at the start of every step. */
    void remember() {
        previousX = x;
        previousY = y;
    }

    void place(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void moveBy(double dx, double dy) {
        x += dx;
        y += dy;
    }

    void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    /** The same direction at a new speed. */
    void setSpeed(double speed) {
        double current = Math.hypot(vx, vy);
        if (current == 0) return;
        vx *= speed / current;
        vy *= speed / current;
    }
}
