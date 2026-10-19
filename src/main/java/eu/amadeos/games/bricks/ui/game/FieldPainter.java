package eu.amadeos.games.bricks.ui.game;

import eu.amadeos.gameengine.fx.Particles;
import eu.amadeos.gameengine.sprite.Sprite;
import eu.amadeos.games.bricks.domain.Ball;
import eu.amadeos.games.bricks.domain.Brick;
import eu.amadeos.games.bricks.domain.BrickType;
import eu.amadeos.games.bricks.domain.Field;
import eu.amadeos.games.bricks.domain.Game;
import eu.amadeos.games.bricks.domain.Paddle;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Draws the world in logical coordinates (640 × 400): the background with its walls, the bricks, the
 * paddle, the ball and the particles. Everything but the particles is a {@link Sprite}, so each picture is
 * scaled once for the screen and then only copied.
 */
final class FieldPainter {
    // --8<-- [start:sprites]
    private final Sprite background = Sprite.drawn(Field.WIDTH, Field.HEIGHT, FieldPainter::drawBackground);
    private final Map<BrickType, Sprite> bricks = new EnumMap<>(BrickType.class);
    private final Sprite cracked = load("brick-silver-cracked", Field.BRICK_WIDTH, Field.BRICK_HEIGHT);
    private final Sprite paddle = load("paddle", Field.PADDLE_WIDTH, Field.PADDLE_HEIGHT);
    private final Sprite ball = load("ball", Field.BALL_SIZE, Field.BALL_SIZE);

    FieldPainter() {
        for (BrickType type : BrickType.values()) {
            bricks.put(type, load("brick-" + type.name().toLowerCase(Locale.ROOT),
                    Field.BRICK_WIDTH, Field.BRICK_HEIGHT));
        }
    }

    /** A sprite from the game's images, drawn four times as large as its logical size. */
    static Sprite load(String name, double width, double height) {
        return Sprite.load(FieldPainter.class, "/eu/amadeos/games/bricks/sprites/" + name + ".png", width, height);
    }
    // --8<-- [end:sprites]

    // --8<-- [start:paint]
    void paint(Graphics2D g, Game game, Particles particles, double alpha) {
        background.draw(g, 0, 0);
        for (Brick brick : game.bricks()) {
            Sprite sprite = brick.cracked() ? cracked : bricks.get(brick.type());
            sprite.draw(g, brick.box().x(), brick.box().y());
        }
        Paddle p = game.paddle();
        paddle.draw(g, p.previousX() + (p.x() - p.previousX()) * alpha, Field.PADDLE_Y);
        if (game.phase() != Game.Phase.OVER) {
            Ball b = game.ball();
            ball.draw(g, b.previousX() + (b.x() - b.previousX()) * alpha,
                    b.previousY() + (b.y() - b.previousY()) * alpha);
        }
        particles.render(g, alpha);
    }
    // --8<-- [end:paint]

    /** The background, drawn in code: a dark field with a faint pattern, and metal walls. */
    private static void drawBackground(Graphics2D g, double width, double height) {
        g.setColor(Color.BLACK);
        g.fill(new Rectangle2D.Double(0, 0, width, Field.HUD));
        g.setPaint(new GradientPaint(0, (float) Field.TOP, new Color(0x16204A), 0, (float) height,
                new Color(0x080A18)));
        g.fill(new Rectangle2D.Double(Field.LEFT, Field.TOP, Field.RIGHT - Field.LEFT, height - Field.TOP));
        // A lattice of thin diagonal lines.
        g.setColor(new Color(255, 255, 255, 14));
        for (double d = -height; d < width; d += 24) {
            g.draw(new Line2D.Double(d, height, d + height, Field.TOP));
            g.draw(new Line2D.Double(d, Field.TOP, d + height, height));
        }
        // Walls: metal pipes with rivets.
        Color light = new Color(0xB8C0CC);
        Color dark = new Color(0x3A404C);
        g.setPaint(new GradientPaint(0, 0, dark, (float) Field.WALL / 2, 0, light, true));
        g.fill(new Rectangle2D.Double(0, Field.HUD, Field.LEFT, height - Field.HUD));
        g.setPaint(new GradientPaint((float) Field.RIGHT, 0, dark, (float) (Field.RIGHT + Field.WALL / 2), 0,
                light, true));
        g.fill(new Rectangle2D.Double(Field.RIGHT, Field.HUD, Field.WALL, height - Field.HUD));
        g.setPaint(new GradientPaint(0, (float) Field.HUD, dark, 0, (float) (Field.HUD + 8), light, true));
        g.fill(new Rectangle2D.Double(0, Field.HUD, width, Field.TOP - Field.HUD));
        g.setColor(new Color(0x20242C));
        for (double y = Field.TOP + 20; y < height; y += 40) {
            g.fill(new Ellipse2D.Double(Field.WALL / 2 - 2, y, 4, 4));
            g.fill(new Ellipse2D.Double(Field.RIGHT + Field.WALL / 2 - 2, y, 4, 4));
        }
        for (double x = 40; x < width - 20; x += 40) g.fill(new Ellipse2D.Double(x - 2, Field.HUD + 6, 4, 4));
    }
}
