package org.plovdev.audioengine.effects.parsers;

import org.plovdev.audioengine.effects.AudioEffect;
import org.plovdev.audioengine.effects.ash.ASHParser;
import org.plovdev.audioengine.exceptions.effects.EffectException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class AudioEffectParser implements ASHParser {

    @Override
    public AudioEffect parseAudioShader(File shader) throws EffectException, IOException {
        return null;
    }

    @Override
    public AudioEffect parseAudioShader(InputStream shader) throws EffectException, IOException {
        return null;
    }

    @Override
    public AudioEffect parseAudioShader(URI shader) throws EffectException, IOException {
        return null;
    }

    @Override
    public String shaderVersion() {
        return "";
    }

    @Override
    public boolean validateShader(File shader) {
        return false;
    }

    @Override
    public boolean validateShader(InputStream shader) {
        return false;
    }

    @Override
    public boolean validateShader(URI shader) {
        return false;
    }
}