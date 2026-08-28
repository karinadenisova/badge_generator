// BadgeGenerator.cs
using System;
using System.IO;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using SixLabors.ImageSharp.Processing;
using SixLabors.ImageSharp.Drawing.Processing;
using SixLabors.Fonts;

namespace BadgeGenerator
{
    class Program
    {
        static void Main(string[] args)
        {
            var opts = ParseArgs(args);
            if (string.IsNullOrEmpty(opts.Name) || string.IsNullOrEmpty(opts.Photo))
            {
                Console.Error.WriteLine("Ошибка: --name и --photo обязательны");
                return;
            }
            var gen = new BadgeGenerator(opts);
            gen.Generate(opts.Output);
        }

        static Options ParseArgs(string[] args)
        {
            var opts = new Options();
            for (int i = 0; i < args.Length; i++)
            {
                switch (args[i])
                {
                    case "--name": opts.Name = args[++i]; break;
                    case "--title": opts.Title = args[++i]; break;
                    case "--company": opts.Company = args[++i]; break;
                    case "--photo": opts.Photo = args[++i]; break;
                    case "--output": opts.Output = args[++i]; break;
                    case "--width": opts.Width = int.Parse(args[++i]); break;
                    case "--height": opts.Height = int.Parse(args[++i]); break;
                    case "--bg": opts.Bg = args[++i]; break;
                    case "--text-color": opts.TextColor = args[++i]; break;
                    case "--photo-size": opts.PhotoSize = int.Parse(args[++i]); break;
                    case "--photo-shape": opts.PhotoShape = args[++i]; break;
                }
            }
            return opts;
        }

        class Options
        {
            public string Name { get; set; }
            public string Title { get; set; }
            public string Company { get; set; }
            public string Photo { get; set; }
            public string Output { get; set; } = "badge.png";
            public int Width { get; set; } = 600;
            public int Height { get; set; } = 400;
            public string Bg { get; set; } = "#FFFFFF";
            public string TextColor { get; set; } = "#000000";
            public int PhotoSize { get; set; } = 120;
            public string PhotoShape { get; set; } = "circle";
        }

        class BadgeGenerator
        {
            private Options opts;

            public BadgeGenerator(Options opts)
            {
                this.opts = opts;
            }

            public void Generate(string output)
            {
                using (Image<Rgba32> badge = new Image<Rgba32>(opts.Width, opts.Height))
                {
                    // Фон
                    var bgColor = Color.ParseHex(opts.Bg);
                    badge.Mutate(ctx => ctx.BackgroundColor(bgColor));

                    // Фото
                    using (Image<Rgba32> photo = Image.Load<Rgba32>(opts.Photo))
                    {
                        photo.Mutate(x => x.Resize(opts.PhotoSize, opts.PhotoSize));
                        if (opts.PhotoShape == "circle")
                        {
                            // Создаём круглую маску (упрощённо: применяем clipping)
                            photo.Mutate(x => x.Crop(new EllipsePolygon(opts.PhotoSize/2, opts.PhotoSize/2, opts.PhotoSize/2)));
                        }
                        int photoX = 30;
                        int photoY = (opts.Height - opts.PhotoSize) / 2;
                        badge.Mutate(ctx => ctx.DrawImage(photo, new Point(photoX, photoY), 1f));
                    }

                    // Текст
                    var textColor = Color.ParseHex(opts.TextColor);
                    var font = SystemFonts.CreateFont("Arial", 36, FontStyle.Bold);
                    int textX = 30 + opts.PhotoSize + 30;
                    int textY = (opts.Height - 90) / 2;
                    badge.Mutate(ctx => ctx.DrawText(opts.Name, font, textColor, new PointF(textX, textY)));
                    if (!string.IsNullOrEmpty(opts.Title))
                    {
                        var fontTitle = SystemFonts.CreateFont("Arial", 24, FontStyle.Regular);
                        badge.Mutate(ctx => ctx.DrawText(opts.Title, fontTitle, textColor, new PointF(textX, textY + 40)));
                    }
                    if (!string.IsNullOrEmpty(opts.Company))
                    {
                        var fontCompany = SystemFonts.CreateFont("Arial", 28, FontStyle.Regular);
                        badge.Mutate(ctx => ctx.DrawText(opts.Company, fontCompany, textColor, new PointF(textX, textY + 70)));
                    }

                    // Сохраняем
                    badge.Save(output);
                    Console.WriteLine($"Бейдж сохранён в {output}");
                }
            }
        }
    }
}
