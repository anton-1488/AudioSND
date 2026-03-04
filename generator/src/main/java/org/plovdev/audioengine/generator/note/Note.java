package org.plovdev.audioengine.generator.note;

/**
 * Represents a musical note with frequency, amplitude, and pitch shifting capabilities.
 * <p>
 * The note stores a base frequency and amplitude, along with octave and semitone shifts
 * that are applied when {@link #frequency()} is called. This allows for efficient
 * transposition without recalculating the base frequency.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * Note a4 = Note.A2;                    // A4 note (440 Hz)
 * Note a5 = a4.upOctave();               // A5 (880 Hz)
 * Note cSharp = a4.transpose(4);          // C#5 (4 semitones up)
 * float freq = a5.frequency();            // 880.0
 * </pre>
 * </p>
 *
 * @param frequency    base frequency in Hz (e.g., 440.0 for A4)
 * @param amplitude    volume (0.0 to 1.0)
 * @param octaveShift  number of octaves to shift (+1 = up one octave, -1 = down one octave)
 * @param semitoneShift number of semitones to shift (-12 to +12)
 */
public record Note(float frequency, float amplitude, int octaveShift, int semitoneShift) {

    /**
     * C4 note (261.63 Hz)
     */
    public static Note C2 = ofFreq(261.63f);

    /**
     * D4 note (293.66 Hz)
     */
    public static Note D2 = ofFreq(293.66f);

    /**
     * E4 note (329.63 Hz)
     */
    public static Note E2 = ofFreq(329.63f);

    /**
     * F4 note (349.23 Hz)
     */
    public static Note F2 = ofFreq(349.23f);

    /**
     * G4 note (392.00 Hz)
     */
    public static Note G2 = ofFreq(392.00f);

    /**
     * A4 note (440.00 Hz) - standard tuning reference
     */
    public static Note A2 = ofFreq(440.00f);

    /**
     * B4 note (492.88 Hz)
     */
    public static Note B2 = ofFreq(492.88f);

    /**
     * Creates a note with the specified frequency and default amplitude (0.5).
     *
     * @param freq frequency in Hz
     * @return a new Note instance
     */
    public static Note ofFreq(float freq) {
        return new Note(freq, 0.5f, 0, 0);
    }

    /**
     * Creates a note using a modulation factor.
     * <p>
     * The modulation factor is a frequency multiplier:
     * <ul>
     *   <li>0.5 = one octave down</li>
     *   <li>1.0 = no change</li>
     *   <li>2.0 = one octave up</li>
     *   <li>1.05946 = one semitone up (12th root of 2)</li>
     * </ul>
     * The factor is automatically converted into octave and semitone shifts.
     * </p>
     *
     * @param frequency base frequency in Hz
     * @param amplitude volume (0.0 to 1.0)
     * @param modulationFactor frequency multiplier
     * @return a new Note with calculated shifts
     */
    public static Note withFactor(float frequency, float amplitude, float modulationFactor) {
        double octaves = Math.log(modulationFactor) / Math.log(2);
        int fullOctaves = (int) octaves;
        int semitones = (int) Math.round((octaves - fullOctaves) * 12);

        return new Note(frequency, amplitude, fullOctaves, semitones);
    }

    /**
     * Calculates the effective frequency after applying octave and semitone shifts.
     * <p>
     * The calculation follows these rules:
     * <ul>
     *   <li>Each octave shift multiplies/divides the frequency by 2</li>
     *   <li>Each semitone shift multiplies/divides by 2^(1/12) ≈ 1.059463</li>
     * </ul>
     * </p>
     *
     * @return the shifted frequency in Hz
     */
    public float frequency() {
        double freq = frequency * Math.pow(2, octaveShift);

        if (semitoneShift != 0) {
            freq *= Math.pow(2, semitoneShift / 12.0);
        }

        return (float) freq;
    }

    /**
     * Returns a new note shifted up by one octave.
     *
     * @return note one octave higher
     */
    public Note upOctave() {
        return new Note(frequency, amplitude, octaveShift + 1, semitoneShift);
    }

    /**
     * Returns a new note shifted down by one octave.
     *
     * @return note one octave lower
     */
    public Note downOctave() {
        return new Note(frequency, amplitude, octaveShift - 1, semitoneShift);
    }

    /**
     * Returns a new note transposed by the specified number of semitones.
     * <p>
     * The transposition automatically wraps octaves:
     * {@code transpose(12)} equals {@code upOctave()}.
     * </p>
     *
     * @param semitones number of semitones to shift (positive = up, negative = down)
     * @return transposed note
     */
    public Note transpose(int semitones) {
        int totalSemitones = semitoneShift + semitones;
        int newOctaveShift = octaveShift + totalSemitones / 12;
        int newSemitoneShift = totalSemitones % 12;

        return new Note(frequency, amplitude, newOctaveShift, newSemitoneShift);
    }

    @Override
    public String toString() {
        return String.format("Note[frequency=%.2f Hz, amplitude=%.2f, octaveShift=%d, semitoneShift=%d]", frequency, amplitude, octaveShift, semitoneShift);
    }
}