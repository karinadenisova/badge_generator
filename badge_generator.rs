// badge_generator.rs
use image::{ImageBuffer, Rgba, RgbaImage, GenericImageView};
use imageproc::drawing::draw_text_mut;
use rusttype::{Font, Scale};
use clap::{App, Arg};
use std::fs;
use std::path::Path;

fn hex_to_rgba(hex: &str) -> Rgba<u8> {
    let hex = hex.trim_start_matches('#');
    if hex.len() == 6 {
        let r = u8::from_str_radix(&hex[0..2], 16).unwrap_or(0);
        let g = u8::from_str_radix(&hex[2..4], 16).unwrap_or(0);
        let b = u8::from_str_radix(&hex[4..6], 16).unwrap_or(0);
        Rgba([r, g, b, 255])
    } else {
        Rgba([0, 0, 0, 255])
    }
}

struct BadgeGenerator {
    name: String,
    title: Option<String>,
    company: Option<String>,
    photo_path: String,
    width: u32,
    height: u32,
    bg_color: Rgba<u8>,
    text_color: Rgba<u8>,
    photo_size: u32,
    photo_shape: String,
    font_data: Vec<u8>,
}

impl BadgeGenerator {
    fn new(name: &str, title: Option<&str>, company: Option<&str>, photo_path: &str,
           width: u32, height: u32, bg: &str, text: &str, photo_size: u32, shape: &str, font: Option<&str>) -> Self {
        let font_data = if let Some(path) = font {
            fs::read(path).unwrap_or_else(|_| {
                eprintln!("Не удалось загрузить шрифт, используем встроенный");
                include_bytes!("DejaVuSans.ttf").to_vec()
            })
        } else {
            // Встроенный шрифт для демонстрации (требуется наличие файла)
            include_bytes!("DejaVuSans.ttf").to_vec()
        };
        BadgeGenerator {
            name: name.to_string(),
            title: title.map(String::from),
            company: company.map(String::from),
            photo_path: photo_path.to_string(),
            width,
            height,
            bg_color: hex_to_rgba(bg),
            text_color: hex_to_rgba(text),
            photo_size,
            photo_shape: shape.to_string(),
            font_data,
        }
    }

    fn generate(&self, output: &str) -> Result<(), Box<dyn std::error::Error>> {
        // Загружаем фото
        let photo = image::open(&self.photo_path)?.to_rgba8();
        let photo = image::imageops::resize(&photo, self.photo_size, self.photo_size, image::imageops::FilterType::Lanczos3);

        // Создаём бейдж
        let mut badge = ImageBuffer::from_pixel(self.width, self.height, self.bg_color);

        // Вставляем фото
        let photo_x = 30;
        let photo_y = (self.height - self.photo_size) / 2;
        for y in 0..self.photo_size {
            for x in 0..self.photo_size {
                let px = photo.get_pixel(x, y);
                if self.photo_shape == "circle" {
                    // Проверяем, попадает ли точка в круг
                    let cx = self.photo_size / 2;
                    let cy = self.photo_size / 2;
                    let r = self.photo_size / 2;
                    let dx = x as i32 - cx as i32;
                    let dy = y as i32 - cy as i32;
                    if dx*dx + dy*dy <= (r*r) as i32 {
                        badge.put_pixel(photo_x + x, photo_y + y, *px);
                    }
                } else {
                    badge.put_pixel(photo_x + x, photo_y + y, *px);
                }
            }
        }

        // Текст
        let font = Font::try_from_bytes(&self.font_data).expect("Не удалось загрузить шрифт");
        let scale = Scale::uniform(36.0);
        let text_x = photo_x + self.photo_size + 30;
        let text_y = (self.height - 90) / 2;
        draw_text_mut(&mut badge, self.text_color, text_x as i32, text_y as i32, scale, &font, &self.name);
        if let Some(t) = &self.title {
            let scale2 = Scale::uniform(24.0);
            draw_text_mut(&mut badge, self.text_color, text_x as i32, (text_y + 40) as i32, scale2, &font, t);
        }
        if let Some(c) = &self.company {
            let scale3 = Scale::uniform(28.0);
            draw_text_mut(&mut badge, self.text_color, text_x as i32, (text_y + 70) as i32, scale3, &font, c);
        }

        // Сохраняем
        badge.save(output)?;
        println!("Бейдж сохранён в {}", output);
        Ok(())
    }
}

fn main() {
    let matches = App::new("Badge Generator with Photo")
        .arg(Arg::with_name("name").long("name").takes_value(true).required(true))
        .arg(Arg::with_name("title").long("title").takes_value(true))
        .arg(Arg::with_name("company").long("company").takes_value(true))
        .arg(Arg::with_name("photo").long("photo").takes_value(true).required(true))
        .arg(Arg::with_name("output").long("output").takes_value(true).default_value("badge.png"))
        .arg(Arg::with_name("width").long("width").takes_value(true).default_value("600"))
        .arg(Arg::with_name("height").long("height").takes_value(true).default_value("400"))
        .arg(Arg::with_name("bg").long("bg").takes_value(true).default_value("#FFFFFF"))
        .arg(Arg::with_name("text-color").long("text-color").takes_value(true).default_value("#000000"))
        .arg(Arg::with_name("photo-size").long("photo-size").takes_value(true).default_value("120"))
        .arg(Arg::with_name("photo-shape").long("photo-shape").takes_value(true).default_value("circle"))
        .arg(Arg::with_name("font").long("font").takes_value(true))
        .arg(Arg::with_name("batch").long("batch").takes_value(true))
        .arg(Arg::with_name("output-dir").long("output-dir").takes_value(true).default_value("."))
        .get_matches();

    let name = matches.value_of("name").unwrap();
    let title = matches.value_of("title");
    let company = matches.value_of("company");
    let photo = matches.value_of("photo").unwrap();
    let output = matches.value_of("output").unwrap();
    let width: u32 = matches.value_of("width").unwrap().parse().expect("width");
    let height: u32 = matches.value_of("height").unwrap().parse().expect("height");
    let bg = matches.value_of("bg").unwrap();
    let text_color = matches.value_of("text-color").unwrap();
    let photo_size: u32 = matches.value_of("photo-size").unwrap().parse().expect("photo-size");
    let photo_shape = matches.value_of("photo-shape").unwrap();
    let font = matches.value_of("font");

    if let Some(batch_file) = matches.value_of("batch") {
        // Пакетная обработка (упрощённо)
        println!("Пакетная генерация из {} (требуется реализация)", batch_file);
        // В реальном коде здесь была бы обработка CSV
    } else {
        let gen = BadgeGenerator::new(
            name, title, company, photo,
            width, height, bg, text_color, photo_size, photo_shape, font
        );
        if let Err(e) = gen.generate(output) {
            eprintln!("Ошибка: {}", e);
            std::process::exit(1);
        }
    }
}
