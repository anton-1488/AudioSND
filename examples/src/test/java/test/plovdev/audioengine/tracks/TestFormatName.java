package test.plovdev.audioengine.tracks;

public enum TestFormatName {
    PCM_8_U_LE("pcm8ule.wav"),
    PCM_32_FLOAT("pcm32float.wav"),
    PCM_64_FLOAT("pcm64float.wav"),
    PCM_ALAW("pcmalaw.wav"),
    PCM_ULAW("pcmulaw.wav"),
    PCM_IMA_ADPCM("pcmimaadpcm.wav"),
    PCM_MICROSOFT_ADPCM("pcmmicadpcm.wav"),
    PCM_GSM_6("pcmgsm6.wav");

    final String name;
    TestFormatName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}