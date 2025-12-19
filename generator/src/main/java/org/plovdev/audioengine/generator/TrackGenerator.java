package org.plovdev.audioengine.generator;

import org.plovdev.audioengine.tracks.Track;
import org.plovdev.audioengine.tracks.format.TrackFormat;

import java.time.Duration;

import static org.plovdev.audioengine.generator.TrackGeneratorHelper.generate;

public class TrackGenerator {
    /**
     * Создание тестовых треков
     */
    public static Track generateSine(TrackFormat format, Duration duration, Note note) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(note.frequency())
                .amplitude(note.amplitude())
                .waveType(WaveType.SINE)
                .build();

        return generate(duration, format, config);
    }


    public static Track generateSine(TrackFormat format, Duration duration, Note... notes) {
        double[] freqs = new double[notes.length];
        double[] ampls = new double[notes.length];

        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            freqs[i] = note.frequency();
            ampls[i] = note.amplitude();
        }

        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(freqs)
                .amplitude(ampls)
                .waveType(WaveType.SINE)
                .build();

        return generate(duration, format, config);
    }

    public static Track generateNoise(TrackFormat format, Duration duration) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .amplitude(0.1)
                .waveType(WaveType.NOISE)
                .build();

        return generate(duration, format, config);
    }

    public static Track generateSilence(TrackFormat format, Duration duration) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .amplitude(0.0)
                .waveType(WaveType.SILENCE)
                .build();

        return generate(duration, format, config);
    }

    /**
     * Создание чирпа (частотной развертки)
     */
    public static Track generateChirp(TrackFormat format, Duration duration, Note note1, Note note2) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(note1.frequency())
                .amplitude((note1.amplitude()) + note2.amplitude() / 2.0)
                .waveType(WaveType.CHIRP)
                .frequencySweep(note1.frequency(), note2.frequency())
                .build();

        return generate(duration, format, config);
    }

    /**
     * Генерация прямоугольной волны (Square wave)
     *
     * @param dutyCycle скважность 0.0-1.0 (0.5 = меандр)
     */
    public static Track generateSquare(TrackFormat format, Duration duration,
                                       Note note, double dutyCycle) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(note.frequency())
                .amplitude(note.amplitude())
                .waveType(WaveType.SQUARE)
                .dutyCycle(dutyCycle) // Важно! Без этого будет дефолтный 0.5
                .build();

        return generate(duration, format, config);
    }

    /**
     * Генерация пилообразной волны (Sawtooth)
     * Идеально для басов в электронной музыке
     */
    public static Track generateSawtooth(TrackFormat format, Duration duration, Note note) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(note.frequency())
                .amplitude(note.amplitude())
                .waveType(WaveType.SAWTOOTH)
                .build();

        return generate(duration, format, config);
    }


    /**
     * Генерация треугольной волны (Triangle)
     * Мягкий звук, похож на флейту
     */
    public static Track generateTriangle(TrackFormat format, Duration duration, Note note) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .frequency(note.frequency())
                .amplitude(note.amplitude())
                .waveType(WaveType.TRIANGLE)
                .build();

        return generate(duration, format, config);
    }


    /**
     * Генерация импульса (Impulse)
     * Дираковский импульс - полезен для тестирования
     */
    public static Track generateImpulse(TrackFormat format, Duration duration) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .amplitude(1.0) // Полная амплитуда
                .waveType(WaveType.IMPULSE)
                .build();

        return generate(duration, format, config);
    }

    /**
     * Экспоненциальный свип (Sweep)
     * Логарифмическая развертка частоты
     */
    public static Track generateSweep(TrackFormat format, Duration duration, Note startNote, Note endNote) {
        GeneratorConfig config = new GeneratorConfig.Builder()
                .channels(format.channels())
                .amplitude((startNote.amplitude() + endNote.amplitude()) / 2.0)
                .waveType(WaveType.SWEEP)
                .frequencySweep(startNote.frequency(), endNote.frequency())
                .build();

        return generate(duration, format, config);
    }
}