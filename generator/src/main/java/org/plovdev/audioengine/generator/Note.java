package org.plovdev.audioengine.generator;

/**
 * Note description class.
 *
 * @param frequency frequency of note. Example, 440 - A2.
 * @param amplitude note volume
 */
public record Note(float frequency, float amplitude, int octaveShift, int semitoneShift) {
    public static Note C2 = ofFreq(261.63f);
    public static Note D2 = ofFreq(293.66f);
    public static Note E2 = ofFreq(329.63f);
    public static Note F2 = ofFreq(349.23f);
    public static Note G2 = ofFreq(392.00f);
    public static Note A2 = ofFreq(440.00f);
    public static Note B2 = ofFreq(492.88f);



    public static Note ofFreq(float freq) {
        return new Note(freq, 0.5f, 0, 0);
    }

    /**
     * НАСТОЯЩИЙ modulation factor - множитель частоты!
     * 0.5 = на октаву ниже
     * 2.0 = на октаву выше
     * 1.05946 = на полутон выше (12√2)
     */
    public static Note withFactor(float frequency, float amplitude, float modulationFactor) {
        // Преобразуем factor в октавы и полутоны для понятного API
        double octaves = Math.log(modulationFactor) / Math.log(2);
        int fullOctaves = (int) octaves;
        int semitones = (int) Math.round((octaves - fullOctaves) * 12);

        return new Note(frequency, amplitude, fullOctaves, semitones);
    }

    /**
     * Рассчитывает итоговую частоту с учетом смещений
     */
    public float frequency() {
        // Каждая октава = умножение/деление на 2
        double freq = frequency * Math.pow(2, octaveShift);

        // Каждый полутон = умножение/деление на 12√2 (≈1.059463)
        if (semitoneShift != 0) {
            freq *= Math.pow(2, semitoneShift / 12.0);
        }

        return (float) freq;
    }

    /**
     * Сдвиг на октаву вверх
     */
    public Note upOctave() {
        return new Note(frequency, amplitude, octaveShift + 1, semitoneShift);
    }

    /**
     * Сдвиг на октаву вниз
     */
    public Note downOctave() {
        return new Note(frequency, amplitude, octaveShift - 1, semitoneShift);
    }

    /**
     * Сдвиг на N полутонов
     */
    public Note transpose(int semitones) {
        int totalSemitones = semitoneShift + semitones;
        int newOctaveShift = octaveShift + totalSemitones / 12;
        int newSemitoneShift = totalSemitones % 12;

        return new Note(frequency, amplitude, newOctaveShift, newSemitoneShift);
    }

    @Override
    public String toString() {
        return String.format("(Freq: %s; Ampl: %s)", frequency, amplitude);
    }
}