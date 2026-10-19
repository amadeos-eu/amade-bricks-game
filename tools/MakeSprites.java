import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Draws the sprites of Bricks as PNG files, four times as large as they appear in the game's logical
 * coordinates (a brick of 40 × 16 is 160 × 64 pixels), so they stay sharp up to a 4K screen.
 *
 * <pre>
 * java tools/MakeSprites.java src/main/resources/eu/amadeos/games/bricks/sprites src/main/resources/icons
 * </pre>
 *
 * The files are part of the repository; run this again only to change the pictures.
 */
public class MakeSprites {
    static final int SCALE = 4;

    public static void main(String[] args) throws IOException {
        File dir = new File(args.length > 0 ? args[0] : "sprites");
        dir.mkdirs();
        String[][] bricks = {{"red", "D83A3A"}, {"orange", "E8862A"}, {"yellow", "E8D23A"}, {"green", "4CB848"},
                {"blue", "3A7AD8"}, {"violet", "9A4AD0"}};
        for (String[] b : bricks) write(brick(new Color(Integer.parseInt(b[1], 16)), false, false), dir, "brick-" + b[0]);
        write(brick(new Color(0xC0C8D0), true, false), dir, "brick-silver");
        write(brick(new Color(0xC0C8D0), true, true), dir, "brick-silver-cracked");
        write(brick(new Color(0xD4A82A), true, false), dir, "brick-gold");
        write(paddle(), dir, "paddle");
        write(ball(), dir, "ball");
        if (args.length > 1) write(icon(), new File(args[1]), "amade-bricks-game");
    }

    /** The program icon, 48 × 48: three rows of bricks, the ball and the paddle. */
    static BufferedImage icon() {
        BufferedImage image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(new Color(0x16204A));
        g.fill(new RoundRectangle2D.Double(1, 1, 46, 46, 8, 8));
        String[] colours = {"D83A3A", "E8862A", "4CB848"};
        for (int row = 0; row < 3; row++) {
            BufferedImage brick = brick(new Color(Integer.parseInt(colours[row], 16)), false, false);
            for (int col = 0; col < 3; col++) g.drawImage(brick, 4 + col * 14, 6 + row * 6, 13, 5, null);
        }
        g.drawImage(ball(), 26, 27, 6, 6, null);
        g.drawImage(paddle(), 12, 38, 22, 4, null);
        g.dispose();
        return image;
    }

    static void write(BufferedImage image, File dir, String name) throws IOException {
        ImageIO.write(image, "png", new File(dir, name + ".png"));
    }

    static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(SCALE, SCALE);
        return g;
    }

    /** A brick of 40 × 16: bevelled edges, light from the top left; metal ones shine. */
    static BufferedImage brick(Color base, boolean metal, boolean cracked) {
        BufferedImage image = new BufferedImage(40 * SCALE, 16 * SCALE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        g.setColor(base.darker().darker());
        g.fill(new RoundRectangle2D.Double(0.5, 0.5, 39, 15, 2, 2));
        g.setPaint(new GradientPaint(0, 1, base.brighter(), 0, 15, base.darker()));
        g.fill(new RoundRectangle2D.Double(1, 1, 38, 14, 2, 2));
        // Bevel: a light top and left edge, a dark bottom and right edge.
        Path2D light = new Path2D.Double();
        light.moveTo(1, 15); light.lineTo(1, 1); light.lineTo(39, 1); light.lineTo(37, 3); light.lineTo(3, 3);
        light.lineTo(3, 13); light.closePath();
        g.setColor(new Color(255, 255, 255, 110));
        g.fill(light);
        Path2D dark = new Path2D.Double();
        dark.moveTo(39, 1); dark.lineTo(39, 15); dark.lineTo(1, 15); dark.lineTo(3, 13); dark.lineTo(37, 13);
        dark.lineTo(37, 3); dark.closePath();
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(dark);
        g.setPaint(new GradientPaint(3, 3, new Color(255, 255, 255, 40), 3, 13, new Color(0, 0, 0, 30)));
        g.fill(new Rectangle2D.Double(3, 3, 34, 10));
        if (metal) {
            g.setColor(new Color(255, 255, 255, 150));
            Path2D shine = new Path2D.Double();
            shine.moveTo(8, 3); shine.lineTo(13, 3); shine.lineTo(9, 13); shine.lineTo(4, 13); shine.closePath();
            g.fill(shine);
            shine = new Path2D.Double();
            shine.moveTo(15, 3); shine.lineTo(17, 3); shine.lineTo(13, 13); shine.lineTo(11, 13); shine.closePath();
            g.fill(shine);
        }
        if (cracked) {
            g.setColor(new Color(40, 40, 50, 220));
            g.setStroke(new BasicStroke(0.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D crack = new Path2D.Double();
            crack.moveTo(22, 1.5); crack.lineTo(20, 5); crack.lineTo(24, 8); crack.lineTo(21, 11); crack.lineTo(23, 14.5);
            crack.moveTo(24, 8); crack.lineTo(29, 7); crack.lineTo(32, 10);
            crack.moveTo(20, 5); crack.lineTo(15, 6);
            g.draw(crack);
        }
        g.dispose();
        return image;
    }

    /** The paddle, 64 × 10: a silver capsule with red ends. */
    static BufferedImage paddle() {
        BufferedImage image = new BufferedImage(64 * SCALE, 10 * SCALE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        RoundRectangle2D body = new RoundRectangle2D.Double(0.5, 0.5, 63, 9, 9, 9);
        g.setPaint(new GradientPaint(0, 0, new Color(0xF4F6F8), 0, 10, new Color(0x60687A)));
        g.fill(body);
        g.setClip(body);
        for (double x : new double[]{0, 54}) {
            g.setPaint(new GradientPaint(0, 0, new Color(0xFF7060), 0, 10, new Color(0x902018)));
            g.fill(new Rectangle2D.Double(x, 0, 10, 10));
        }
        g.setColor(new Color(0, 0, 0, 120));
        g.fill(new Rectangle2D.Double(10, 0, 1, 10));
        g.fill(new Rectangle2D.Double(53, 0, 1, 10));
        g.setColor(new Color(255, 255, 255, 160));
        g.fill(new RoundRectangle2D.Double(3, 1.5, 58, 2, 2, 2));
        g.setClip(null);
        g.setColor(new Color(0x30343C));
        g.setStroke(new BasicStroke(0.6f));
        g.draw(body);
        g.dispose();
        return image;
    }

    /** The ball, 8 × 8: a white sphere lit from the top left. */
    static BufferedImage ball() {
        BufferedImage image = new BufferedImage(8 * SCALE, 8 * SCALE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = graphics(image);
        g.setPaint(new RadialGradientPaint(2.8f, 2.6f, 5.5f, new float[]{0f, 0.35f, 1f},
                new Color[]{Color.WHITE, new Color(0xE0E8F0), new Color(0x707C90)}));
        g.fill(new Ellipse2D.Double(0.3, 0.3, 7.4, 7.4));
        g.dispose();
        return image;
    }
}
