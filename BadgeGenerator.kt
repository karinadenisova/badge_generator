// BadgeGenerator.kt
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

data class Options(
    val name: String,
    val title: String?,
    val company: String?,
    val photo: String,
    val output: String,
    val width: Int,
    val height: Int,
    val bg: Color,
    val textColor: Color,
    val photoSize: Int,
    val photoShape: String
)

fun parseArgs(args: Array<String>): Options {
    val map = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        if (args[i].startsWith("--")) {
            val key = args[i].substring(2)
            if (i + 1 < args.size && !args[i+1].startsWith("--")) {
                map[key] = args[++i]
            } else {
                map[key] = ""
            }
        }
        i++
    }
    val name = map["name"] ?: ""
    val title = map["title"]
    val company = map["company"]
    val photo = map["photo"] ?: ""
    val output = map["output"] ?: "badge.png"
    val width = map["width"]?.toIntOrNull() ?: 600
    val height = map["height"]?.toIntOrNull() ?: 400
    val bg = Color.decode(map["bg"] ?: "#FFFFFF")
    val textColor = Color.decode(map["text-color"] ?: "#000000")
    val photoSize = map["photo-size"]?.toIntOrNull() ?: 120
    val photoShape = map["photo-shape"] ?: "circle"
    return Options(name, title, company, photo, output, width, height, bg, textColor, photoSize, photoShape)
}

fun generateBadge(opts: Options) {
    // Загружаем фото
    val photo = ImageIO.read(File(opts.photo)) ?: throw Exception("Не удалось загрузить фото")
    val scaledPhoto = photo.getScaledInstance(opts.photoSize, opts.photoSize, Image.SCALE_SMOOTH)
    val photoImg = BufferedImage(opts.photoSize, opts.photoSize, BufferedImage.TYPE_INT_ARGB)
    val g2 = photoImg.createGraphics()
    if (opts.photoShape == "circle") {
        val clip = Ellipse2D.Double(0.0, 0.0, opts.photoSize.toDouble(), opts.photoSize.toDouble())
        g2.clip = clip
    }
    g2.drawImage(scaledPhoto, 0, 0, null)
    g2.dispose()

    // Бейдж
    val badge = BufferedImage(opts.width, opts.height, BufferedImage.TYPE_INT_RGB)
    val g = badge.createGraphics()
    g.color = opts.bg
    g.fillRect(0, 0, opts.width, opts.height)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    // Фото
    val photoX = 30
    val photoY = (opts.height - opts.photoSize) / 2
    g.drawImage(photoImg, photoX, photoY, null)

    // Текст
    val textX = photoX + opts.photoSize + 30
    val textY = (opts.height - 90) / 2
    g.color = opts.textColor
    val nameFont = Font("Arial", Font.BOLD, 36)
    g.font = nameFont
    g.drawString(opts.name, textX, textY)
    opts.title?.let {
        val titleFont = Font("Arial", Font.PLAIN, 24)
        g.font = titleFont
        g.drawString(it, textX, textY + 40)
    }
    opts.company?.let {
        val compFont = Font("Arial", Font.PLAIN, 28)
        g.font = compFont
        g.drawString(it, textX, textY + 70)
    }
    g.dispose()

    // Сохраняем
    ImageIO.write(badge, "png", File(opts.output))
    println("Бейдж сохранён в ${opts.output}")
}

fun main(args: Array<String>) {
    val opts = parseArgs(args)
    if (opts.name.isEmpty() || opts.photo.isEmpty()) {
        System.err.println("Ошибка: --name и --photo обязательны")
        exitProcess(1)
    }
    try {
        generateBadge(opts)
    } catch (e: Exception) {
        System.err.println("Ошибка: ${e.message}")
        exitProcess(1)
    }
}
