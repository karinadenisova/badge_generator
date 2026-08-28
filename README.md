# Генератор бейджей (фотография)

Многоязычная утилита для создания персонализированных бейджей с фотографией участника.  
Идеально подходит для конференций, мероприятий, пропусков и визиток.

## Особенности
- Вставка фотографии из файла (PNG, JPEG) в бейдж.
- Настраиваемый текст: имя, должность, компания.
- Поддержка различных шрифтов и размеров текста.
- Настраиваемый цвет фона и текста.
- Автоматическое масштабирование и обрезка фото (круглая или прямоугольная форма).
- Экспорт в PNG.
- Пакетная генерация из CSV-файла (опционально).
- Поддержка аргументов командной строки для автоматизации.

## Установка и запуск
Для каждого языка требуются соответствующие инструменты и зависимости.

### Запуск на разных языках

1. **Python**  
   Установка: `pip install pillow colorama`  
   Запуск: `python badge_generator.py --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

2. **JavaScript (Node.js)**  
   Установка: `npm install commander sharp chalk`  
   Запуск: `node badge_generator.js --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

3. **Go**  
   Установка: `go get github.com/fogleman/gg` и `go get github.com/golang/freetype`  
   Запуск: `go run badge_generator.go --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

4. **Rust**  
   Добавьте `image`, `rusttype`, `clap` в `Cargo.toml`.  
   Запуск: `cargo run -- --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

5. **Java**  
   Сборка: `javac BadgeGenerator.java` (требуется стандартная библиотека AWT)  
   Запуск: `java BadgeGenerator --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

6. **C# (.NET Core)**  
   Установка: `dotnet add package SixLabors.ImageSharp` и `SixLabors.ImageSharp.Drawing`  
   Запуск: `dotnet run -- --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

7. **C++**  
   Требуется компилятор с C++11 и библиотека `stb_image`/`stb_image_write` (включены в проект).  
   Сборка: `g++ -std=c++11 -o badge_generator badge_generator.cpp -lm -lpthread`  
   Запуск: `./badge_generator --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

8. **Kotlin (JVM)**  
   Используйте Java AWT (доступен из Kotlin).  
   Сборка: `kotlinc BadgeGenerator.kt -include-runtime -d badge_generator.jar`  
   Запуск: `java -jar badge_generator.jar --name "Иван Иванов" --title "Разработчик" --company "ООО Ромашка" --photo user.jpg --output badge.png`

## Использование

Общие аргументы командной строки (везде, где поддерживается):

- `--name <текст>` – имя на бейдже (обязательно).
- `--title <текст>` – должность (опционально).
- `--company <текст>` – компания (опционально).
- `--photo <путь>` – путь к файлу фотографии (PNG/JPEG, обязательно).
- `--output <файл>` – выходной PNG (по умолчанию `badge.png`).
- `--width <число>` – ширина бейджа в пикселях (по умолчанию 600).
- `--height <число>` – высота бейджа в пикселях (по умолчанию 400).
- `--bg <HEX>` – цвет фона (по умолчанию `#FFFFFF`).
- `--text-color <HEX>` – цвет текста (по умолчанию `#000000`).
- `--photo-size <число>` – размер фото в пикселях (по умолчанию 120).
- `--photo-shape <круг|квадрат>` – форма фото (по умолчанию `круг`).
- `--font <путь>` – путь к TTF-шрифту (опционально).
- `--batch <CSV>` – CSV-файл для пакетной генерации (колонки: name,title,company,photo,output).

Пример (Python):
```bash
python badge_generator.py --name "Анна Смирнова" --title "Дизайнер" --company "Студия Креатива" --photo anna.jpg --output anna_badge.png --bg "#2C3E50" --text-color "#ECF0F1" --photo-size 150 --photo-shape круг
Структура репозитория
text
/
├── README.md
├── badge_generator.py
├── badge_generator.js
├── badge_generator.go
├── badge_generator.rs
├── BadgeGenerator.java
├── BadgeGenerator.cs
├── badge_generator.cpp
└── BadgeGenerator.kt
Лицензия
MIT
