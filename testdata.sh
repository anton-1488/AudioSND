#!/bin/bash

# Оптимизированный скрипт ТОЛЬКО для WAV
# Быстро, просто, надежно

set -e

if [ $# -eq 0 ]; then
    echo "Использование: $0 <файлы.wav>"
    echo "Пример: $0 *.wav"
    exit 1
fi

MAIN_DIR="testdata"
echo "🧹 Очистка старой папки..."
rm -rf "$MAIN_DIR" 2>/dev/null || true
mkdir -p "$MAIN_DIR"

echo "🎯 Конвертация WAV файлов"
echo "📁 Целевая директория: $MAIN_DIR"
echo ""

# Оптимальные параметры
SAMPLE_RATES=("8000" "44100" "48000" "96000" "192000")
BIT_DEPTHS=("8" "16" "24")

# Счетчики
TOTAL_FILES=0
PROCESSED_FILES=0

# Подсчитываем файлы
for input_file in "$@"; do
    if [ -f "$input_file" ] && [[ "$input_file" =~ \.wav$ ]]; then
        TOTAL_FILES=$((TOTAL_FILES + 1))
    fi
done

echo "📊 Найдено WAV файлов: $TOTAL_FILES"
echo "⚡️ Начинаю обработку..."
echo "========================================"

# Основной цикл
for input_file in "$@"; do
    if [ ! -f "$input_file" ]; then
        echo "⚠️  Пропускаем: $input_file (не найден)"
        continue
    fi

    if [[ "$input_file" =~ \.wav$ ]]; then
        PROCESSED_FILES=$((PROCESSED_FILES + 1))
        BASE_NAME=$(basename "$input_file" .wav)

        echo "🔊 [$PROCESSED_FILES/$TOTAL_FILES] Обработка: $BASE_NAME"
        echo "  📁 Создание WAV форматов..."

        for sr in "${SAMPLE_RATES[@]}"; do
            for bd in "${BIT_DEPTHS[@]}"; do
                # Определяем PCM формат
                case $bd in
                    8)  pcm_format="pcm_u8" ;;
                    16) pcm_format="pcm_s16le" ;;
                    24) pcm_format="pcm_s24le" ;;
                    32) pcm_format="pcm_s32le" ;;
                esac

                # Создаем директории
                DIR_PATH="$MAIN_DIR/wav/${sr}/${bd}"
                mkdir -p "$DIR_PATH"

                # Стерео версия
                STEREO_FILE="$DIR_PATH/${BASE_NAME}-stereo.wav"
                ffmpeg -i "$input_file" -ar $sr -acodec "$pcm_format" -ac 2 -y "$STEREO_FILE" 2>/dev/null

                # Моно версия
                MONO_FILE="$DIR_PATH/${BASE_NAME}-mono.wav"
                ffmpeg -i "$input_file" -ar $sr -acodec "$pcm_format" -ac 1 -y "$MONO_FILE" 2>/dev/null

                echo "    ✓ ${sr}Hz/${bd}bit"
            done
        done

        echo "  ✅ $BASE_NAME завершен"
        echo ""
    else
        echo "⚠️  Пропускаем: $input_file (не WAV)"
    fi
done

echo "========================================"
echo "🎉 Конвертация завершена!"
echo ""
echo "📊 Статистика:"
echo "   Всего файлов: $#"
echo "   WAV файлов: $TOTAL_FILES"
echo "   Обработано: $PROCESSED_FILES"
echo "   Папок создано: $(find "$MAIN_DIR" -type d 2>/dev/null | wc -l)"
echo "   Файлов создано: $(find "$MAIN_DIR" -type f 2>/dev/null | wc -l)"
echo ""
echo "📁 Структура:"
echo "   $MAIN_DIR/wav/"
echo "   ├── 8000/8/     (mono/stereo)"
echo "   ├── 8000/16/    (mono/stereo)"
echo "   ├── 8000/24/    (mono/stereo)"
echo "   ├── 8000/32/    (mono/stereo)"
echo "   ├── 44100/8/    (mono/stereo)"
echo "   └── ..."
echo ""
echo "💾 Размер:"
du -sh "$MAIN_DIR"