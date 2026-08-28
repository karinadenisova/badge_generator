#!/usr/bin/env node
// badge_generator.js
const { program } = require('commander');
const fs = require('fs');
const sharp = require('sharp');
const chalk = require('chalk');

class BadgeGenerator {
    constructor(options) {
        this.name = options.name;
        this.title = options.title || '';
        this.company = options.company || '';
        this.photoPath = options.photo;
        this.width = options.width || 600;
        this.height = options.height || 400;
        this.bgColor = options.bg || '#FFFFFF';
        this.textColor = options.textColor || '#000000';
        this.photoSize = options.photoSize || 120;
        this.photoShape = options.photoShape || 'circle';
        this.font = options.font || null;
    }

    async generate(output) {
        // Создаём фон
        let badge = sharp({
            create: {
                width: this.width,
                height: this.height,
                channels: 3,
                background: this.bgColor
            }
        });

        // Обрабатываем фото
        let photo = sharp(this.photoPath);
        const photoMeta = await photo.metadata();
        let photoBuffer = await photo
            .resize(this.photoSize, this.photoSize, { fit: 'cover' })
            .toBuffer();

        // Если круг, создаём маску
        if (this.photoShape === 'circle') {
            const mask = await sharp({
                create: {
                    width: this.photoSize,
                    height: this.photoSize,
                    channels: 1,
                    background: { r: 0, g: 0, b: 0 }
                }
            })
            .composite([{
                input: await sharp({
                    create: {
                        width: this.photoSize,
                        height: this.photoSize,
                        channels: 1,
                        background: { r: 255, g: 255, b: 255 }
                    }
                })
                .composite([{
                    input: Buffer.from(`<svg><circle cx="${this.photoSize/2}" cy="${this.photoSize/2}" r="${this.photoSize/2}" fill="black"/></svg>`),
                    blend: 'dest-in'
                }])
                .png()
                .toBuffer()
            }])
            .png()
            .toBuffer();
            photoBuffer = await sharp(photoBuffer)
                .composite([{ input: mask, blend: 'dest-in' }])
                .png()
                .toBuffer();
        }

        // Накладываем фото на бейдж
        const photoX = 30;
        const photoY = (this.height - this.photoSize) / 2;
        badge = badge.composite([{
            input: photoBuffer,
            top: Math.round(photoY),
            left: photoX
        }]);

        // Создаём SVG для текста (для простоты используем SVG-оверлей)
        const textX = photoX + this.photoSize + 30;
        const textY = (this.height - 90) / 2;
        let svg = `<svg width="${this.width}" height="${this.height}">`;
        svg += `<text x="${textX}" y="${textY}" font-size="36" fill="${this.textColor}" font-family="Arial, sans-serif">${this.name}</text>`;
        if (this.title) {
            svg += `<text x="${textX}" y="${textY + 40}" font-size="24" fill="${this.textColor}" font-family="Arial, sans-serif">${this.title}</text>`;
        }
        if (this.company) {
            svg += `<text x="${textX}" y="${textY + 70}" font-size="28" fill="${this.textColor}" font-family="Arial, sans-serif">${this.company}</text>`;
        }
        svg += '</svg>';

        badge = badge.composite([{
            input: Buffer.from(svg),
            top: 0,
            left: 0
        }]);

        await badge.toFile(output);
        console.log(chalk.green(`Бейдж сохранён в ${output}`));
    }
}

program
    .option('--name <name>', 'Имя на бейдже')
    .option('--title <title>', 'Должность')
    .option('--company <company>', 'Компания')
    .requiredOption('--photo <path>', 'Путь к фото')
    .option('--output <file>', 'Выходной файл', 'badge.png')
    .option('--width <number>', 'Ширина', parseInt, 600)
    .option('--height <number>', 'Высота', parseInt, 400)
    .option('--bg <color>', 'Цвет фона', '#FFFFFF')
    .option('--text-color <color>', 'Цвет текста', '#000000')
    .option('--photo-size <number>', 'Размер фото', parseInt, 120)
    .option('--photo-shape <shape>', 'Форма фото (circle, square)', 'circle')
    .option('--font <path>', 'Путь к шрифту (не используется в SVG)')
    .option('--batch <file>', 'CSV для пакетной генерации')
    .option('--output-dir <dir>', 'Директория для пакетного вывода', '.')
    .parse(process.argv);

const opts = program.opts();

if (opts.batch) {
    const csv = require('csv-parser');
    const fs = require('fs');
    const path = require('path');
    const stream = fs.createReadStream(opts.batch).pipe(csv());
    stream.on('data', async (row) => {
        const name = row.name || '';
        const title = row.title || '';
        const company = row.company || '';
        const photo = row.photo || '';
        const out = row.output || `${name.replace(/\s/g, '_')}.png`;
        const outPath = path.join(opts.outputDir, out);
        const gen = new BadgeGenerator({
            name, title, company, photo,
            width: opts.width,
            height: opts.height,
            bg: opts.bg,
            textColor: opts.textColor,
            photoSize: opts.photoSize,
            photoShape: opts.photoShape,
            font: opts.font
        });
        try {
            await gen.generate(outPath);
        } catch (e) {
            console.error(chalk.red(`Ошибка при генерации ${outPath}: ${e.message}`));
        }
    });
} else {
    if (!opts.name) {
        console.error(chalk.red('Ошибка: --name обязателен'));
        process.exit(1);
    }
    const gen = new BadgeGenerator(opts);
    gen.generate(opts.output);
}
