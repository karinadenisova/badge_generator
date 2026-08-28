
#!/usr/bin/env python3
# badge_generator.py
import argparse
import csv
import os
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from PIL.Image import Resampling

class BadgeGenerator:
    def __init__(self, name, title=None, company=None, photo_path=None,
                 width=600, height=400, bg_color="#FFFFFF", text_color="#000000",
                 photo_size=120, photo_shape="circle", font_path=None):
        self.name = name
        self.title = title
        self.company = company
        self.photo_path = photo_path
        self.width = width
        self.height = height
        self.bg_color = bg_color
        self.text_color = text_color
        self.photo_size = photo_size
        self.photo_shape = photo_shape
        self.font_path = font_path

    def _create_circle_mask(self, size):
        mask = Image.new('L', (size, size), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size, size), fill=255)
        return mask

    def generate(self, output_path):
        # Создаём фон
        bg = Image.new('RGB', (self.width, self.height), self.bg_color)
        draw = ImageDraw.Draw(bg)

        # Загружаем фото
        try:
            photo = Image.open(self.photo_path).convert('RGBA')
        except Exception as e:
            print(f"Ошибка загрузки фото: {e}", file=sys.stderr)
            sys.exit(1)

        # Масштабируем фото
        photo = photo.resize((self.photo_size, self.photo_size), Resampling.LANCZOS)
        # Применяем маску (круг или квадрат)
        if self.photo_shape == "circle":
            mask = self._create_circle_mask(self.photo_size)
            photo = Image.composite(photo, Image.new('RGBA', (self.photo_size, self.photo_size), (0,0,0,0)), mask)

        # Позиционируем фото (слева с отступом)
        photo_x = 30
        photo_y = (self.height - self.photo_size) // 2
        bg.paste(photo, (photo_x, photo_y), photo)

        # Текст
        try:
            # Пытаемся загрузить шрифт, если не указан, используем дефолтный
            if self.font_path:
                font_name = ImageFont.truetype(self.font_path, 36)
                font_title = ImageFont.truetype(self.font_path, 24)
                font_company = ImageFont.truetype(self.font_path, 28)
            else:
                # Используем системный шрифт (не всегда работает, но для демонстрации)
                font_name = ImageFont.load_default()
                font_title = ImageFont.load_default()
                font_company = ImageFont.load_default()
        except:
            font_name = ImageFont.load_default()
            font_title = ImageFont.load_default()
            font_company = ImageFont.load_default()

        text_x = photo_x + self.photo_size + 30
        text_y = (self.height - 90) // 2

        draw.text((text_x, text_y), self.name, fill=self.text_color, font=font_name)
        if self.title:
            draw.text((text_x, text_y + 40), self.title, fill=self.text_color, font=font_title)
        if self.company:
            draw.text((text_x, text_y + 70), self.company, fill=self.text_color, font=font_company)

        # Сохраняем
        bg.save(output_path, 'PNG')
        print(f"Бейдж сохранён в {output_path}")

def batch_generate(csv_path, output_dir, **kwargs):
    import csv
    with open(csv_path, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row.get('name', '')
            title = row.get('title', '')
            company = row.get('company', '')
            photo = row.get('photo', '')
            out = row.get('output', f"{name.replace(' ', '_')}.png")
            out_path = os.path.join(output_dir, out)
            gen = BadgeGenerator(name, title, company, photo, **kwargs)
            gen.generate(out_path)

def main():
    parser = argparse.ArgumentParser(description="Генератор бейджей с фотографией")
    parser.add_argument("--name", required=True, help="Имя на бейдже")
    parser.add_argument("--title", help="Должность")
    parser.add_argument("--company", help="Компания")
    parser.add_argument("--photo", required=True, help="Путь к фото")
    parser.add_argument("--output", default="badge.png", help="Выходной файл")
    parser.add_argument("--width", type=int, default=600, help="Ширина бейджа")
    parser.add_argument("--height", type=int, default=400, help="Высота бейджа")
    parser.add_argument("--bg", default="#FFFFFF", help="Цвет фона (HEX)")
    parser.add_argument("--text-color", default="#000000", help="Цвет текста (HEX)")
    parser.add_argument("--photo-size", type=int, default=120, help="Размер фото")
    parser.add_argument("--photo-shape", choices=["circle", "square"], default="circle", help="Форма фото")
    parser.add_argument("--font", help="Путь к TTF-шрифту")
    parser.add_argument("--batch", help="CSV для пакетной генерации")
    parser.add_argument("--output-dir", default=".", help="Директория для пакетного вывода")
    args = parser.parse_args()

    if args.batch:
        batch_args = {
            "width": args.width,
            "height": args.height,
            "bg_color": args.bg,
            "text_color": args.text_color,
            "photo_size": args.photo_size,
            "photo_shape": args.photo_shape,
            "font_path": args.font
        }
        batch_generate(args.batch, args.output_dir, **batch_args)
    else:
        gen = BadgeGenerator(
            name=args.name,
            title=args.title,
            company=args.company,
            photo_path=args.photo,
            width=args.width,
            height=args.height,
            bg_color=args.bg,
            text_color=args.text_color,
            photo_size=args.photo_size,
            photo_shape=args.photo_shape,
            font_path=args.font
        )
        gen.generate(args.output)

if __name__ == "__main__":
    main()
