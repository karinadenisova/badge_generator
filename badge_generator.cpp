// badge_generator.cpp
#include <iostream>
#include <string>
#include <vector>
#include <cstring>
#include <cmath>
#include <fstream>
#include <sstream>
#include <algorithm>

// Для работы с PNG используем stb_image и stb_image_write
#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"
#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "stb_image_write.h"

using namespace std;

struct Color {
    unsigned char r, g, b;
    Color(unsigned char r_=255, unsigned char g_=255, unsigned char b_=255) : r(r_), g(g_), b(b_) {}
};

Color hexToColor(const string& hex) {
    string h = hex;
    if (h[0] == '#') h = h.substr(1);
    int val = stoi(h, nullptr, 16);
    unsigned char r = (val >> 16) & 0xFF;
    unsigned char g = (val >> 8) & 0xFF;
    unsigned char b = val & 0xFF;
    return Color(r, g, b);
}

void resizeImage(const vector<unsigned char>& src, int srcW, int srcH, vector<unsigned char>& dst, int dstW, int dstH) {
    dst.resize(dstW * dstH * 3);
    for (int y = 0; y < dstH; ++y) {
        for (int x = 0; x < dstW; ++x) {
            int srcX = x * srcW / dstW;
            int srcY = y * srcH / dstH;
            int srcIdx = (srcY * srcW + srcX) * 3;
            int dstIdx = (y * dstW + x) * 3;
            dst[dstIdx] = src[srcIdx];
            dst[dstIdx+1] = src[srcIdx+1];
            dst[dstIdx+2] = src[srcIdx+2];
        }
    }
}

void drawTextOnImage(vector<unsigned char>& img, int w, int h, const string& text, int x, int y, int size, Color color) {
    // Для C++ не реализуем вывод текста (слишком сложно без библиотеки)
    // Просто выводим сообщение о необходимости внешней библиотеки
    // Здесь можно было бы использовать stb_truetype, но для тестового репозитория оставим заглушку
    // В реальном проекте следует подключить FreeType
    // Мы просто нарисуем прямоугольник с текстом (чтобы было видно)
    for (int i = 0; i < (int)text.size() && i < 20; ++i) {
        int px = x + i * 12;
        int py = y - size/2;
        for (int dy = 0; dy < size; ++dy) {
            for (int dx = 0; dx < 10; ++dx) {
                int idx = ((py + dy) * w + (px + dx)) * 3;
                if (idx >= 0 && idx < (int)img.size()) {
                    img[idx] = color.r;
                    img[idx+1] = color.g;
                    img[idx+2] = color.b;
                }
            }
        }
    }
}

int main(int argc, char* argv[]) {
    string name, title, company, photoPath, output = "badge.png";
    int width = 600, height = 400, photoSize = 120;
    string bg = "#FFFFFF", textColor = "#000000", photoShape = "circle";

    for (int i = 1; i < argc; ++i) {
        string arg = argv[i];
        if (arg == "--name" && i+1 < argc) name = argv[++i];
        else if (arg == "--title" && i+1 < argc) title = argv[++i];
        else if (arg == "--company" && i+1 < argc) company = argv[++i];
        else if (arg == "--photo" && i+1 < argc) photoPath = argv[++i];
        else if (arg == "--output" && i+1 < argc) output = argv[++i];
        else if (arg == "--width" && i+1 < argc) width = stoi(argv[++i]);
        else if (arg == "--height" && i+1 < argc) height = stoi(argv[++i]);
        else if (arg == "--bg" && i+1 < argc) bg = argv[++i];
        else if (arg == "--text-color" && i+1 < argc) textColor = argv[++i];
        else if (arg == "--photo-size" && i+1 < argc) photoSize = stoi(argv[++i]);
        else if (arg == "--photo-shape" && i+1 < argc) photoShape = argv[++i];
    }

    if (name.empty() || photoPath.empty()) {
        cerr << "Ошибка: --name и --photo обязательны" << endl;
        return 1;
    }

    // Загружаем фото
    int w, h, channels;
    unsigned char* photoData = stbi_load(photoPath.c_str(), &w, &h, &channels, 3);
    if (!photoData) {
        cerr << "Не удалось загрузить фото" << endl;
        return 1;
    }
    vector<unsigned char> photo(photoData, photoData + w*h*3);
    stbi_image_free(photoData);

    // Масштабируем фото
    vector<unsigned char> scaledPhoto;
    resizeImage(photo, w, h, scaledPhoto, photoSize, photoSize);

    // Создаём бейдж
    vector<unsigned char> badge(width * height * 3);
    Color bgColor = hexToColor(bg);
    Color txtColor = hexToColor(textColor);

    // Заполняем фон
    for (int i = 0; i < (int)badge.size(); i+=3) {
        badge[i] = bgColor.r;
        badge[i+1] = bgColor.g;
        badge[i+2] = bgColor.b;
    }

    // Вставляем фото
    int photoX = 30;
    int photoY = (height - photoSize) / 2;
    for (int y = 0; y < photoSize; ++y) {
        for (int x = 0; x < photoSize; ++x) {
            if (photoShape == "circle") {
                int cx = photoSize/2, cy = photoSize/2, r = photoSize/2;
                if ((x-cx)*(x-cx) + (y-cy)*(y-cy) > r*r) continue;
            }
            int srcIdx = (y * photoSize + x) * 3;
            int dstIdx = ((photoY + y) * width + (photoX + x)) * 3;
            badge[dstIdx] = scaledPhoto[srcIdx];
            badge[dstIdx+1] = scaledPhoto[srcIdx+1];
            badge[dstIdx+2] = scaledPhoto[srcIdx+2];
        }
    }

    // Текст (заглушка)
    int textX = photoX + photoSize + 30;
    int textY = (height - 90) / 2;
    drawTextOnImage(badge, width, height, name, textX, textY, 30, txtColor);
    if (!title.empty()) drawTextOnImage(badge, width, height, title, textX, textY+40, 20, txtColor);
    if (!company.empty()) drawTextOnImage(badge, width, height, company, textX, textY+70, 24, txtColor);

    // Сохраняем PNG
    stbi_write_png(output.c_str(), width, height, 3, badge.data(), width*3);
    cout << "Бейдж сохранён в " << output << endl;
    return 0;
}
