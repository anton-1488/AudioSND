package org.plovdev.audioengine.effects.ash;

import org.plovdev.audioengine.effects.AudioEffect;
import org.plovdev.audioengine.exceptions.effects.EffectException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface ASHParser {
    AudioEffect parseAudioShader(File shader) throws EffectException, IOException;
    AudioEffect parseAudioShader(InputStream shader) throws EffectException, IOException;
    AudioEffect parseAudioShader(URI shader) throws EffectException, IOException;

    String shaderVersion(); // from parsed shader(before created to AudioEffect)

    // checks shader for validity(return false, if contains exception)

    boolean validateShader(File shader);
    boolean validateShader(InputStream shader);
    boolean validateShader(URI shader);
}