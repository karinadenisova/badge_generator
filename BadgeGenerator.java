// BadgeGenerator.java
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.imageio.ImageIO;

public class BadgeGenerator {
    private String name, title, company, photoPath;
    private int width, height, photoSize;
    private Color bgColor, textColor;
    private String photoShape;
    private Font font;

    public BadgeGenerator(String name, String title, String company, String photoPath,
                          int width, int height, Color bg, Color text, int photoSize,
                          String photoShape, Font font) {
        this.name = name;
        this.title = title;
        this.company = company;
        this.photoPath = photoPath;
        this.width = width;
        this.height = height;
        this.bgColor = bg;
        this.textColor = text;
        this.photoSize = photoSize;
        this.photoShape = photoShape;
        this.font = font;
    }

    public void generate(String output) throws Exception {
        // Загружаем фото
        BufferedImage photo = ImageIO.read(new File(photoPath));
        if (photo == null) throw new IOException("Не удалось загрузить фото");
        // Масштабируем
        Image scaledPhoto = photo.getScaledInstance(photoSize, photoSize, Image.SCALE_SMOOTH);
        BufferedImage photoImg = new BufferedImage(photoSize, photoSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = photoImg.createGraphics();
        g2.drawImage(scaledPhoto, 0, 0, null);
        if (photoShape.equals("circle")) {
            // Создаём круглую маску
            Ellipse2D circle = new Ellipse2D.Double(0, 0, photoSize, photoSize);
            g2.setClip(circle);
            g2.drawImage(scaledPhoto, 0, 0, null);
        }
        g2.dispose();

        // Создаём бейдж
        BufferedImage badge = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = badge.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Вставляем фото
        int photoX = 30;
        int photoY = (height - photoSize) / 2;
        g.drawImage(photoImg, photoX, photoY, null);

        // Текст
        int textX = photoX + photoSize + 30;
        int textY = (height - 90) / 2;
        g.setColor(textColor);
        Font nameFont = font != null ? font.deriveFont(Font.BOLD, 36) : new Font("Arial", Font.BOLD, 36);
        g.setFont(nameFont);
        g.drawString(name, textX, textY);
        if (title != null && !title.isEmpty()) {
            Font titleFont = font != null ? font.deriveFont(Font.PLAIN, 24) : new Font("Arial", Font.PLAIN, 24);
            g.setFont(titleFont);
            g.drawString(title, textX, textY + 40);
        }
        if (company != null && !company.isEmpty()) {
            Font compFont = font != null ? font.deriveFont(Font.PLAIN, 28) : new Font("Arial", Font.PLAIN, 28);
            g.setFont(compFont);
            g.drawString(company, textX, textY + 70);
        }
        g.dispose();

        // Сохраняем
        ImageIO.write(badge, "png", new File(output));
        System.out.println("Бейдж сохранён в " + output);
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i+1].startsWith("--")) {
                    params.put(key, args[++i]);
                } else {
                    params.put(key, "");
                }
            }
        }
        String name = params.getOrDefault("name", "");
        String title = params.getOrDefault("title", null);
        String company = params.getOrDefault("company", null);
        String photoPath = params.getOrDefault("photo", "");
        String output = params.getOrDefault("output", "badge.png");
        int width = Integer.parseInt(params.getOrDefault("width", "600"));
        int height = Integer.parseInt(params.getOrDefault("height", "400"));
        Color bg = Color.decode(params.getOrDefault("bg", "#FFFFFF"));
        Color textColor = Color.decode(params.getOrDefault("text-color", "#000000"));
        int photoSize = Integer.parseInt(params.getOrDefault("photo-size", "120"));
        String photoShape = params.getOrDefault("photo-shape", "circle");
        // Шрифт пока игнорируем (используем Arial)

        if (name.isEmpty() || photoPath.isEmpty()) {
            System.err.println("Ошибка: --name и --photo обязательны");
            System.exit(1);
        }
        BadgeGenerator gen = new BadgeGenerator(name, title, company, photoPath,
                width, height, bg, textColor, photoSize, photoShape, null);
        gen.generate(output);
    }
}
