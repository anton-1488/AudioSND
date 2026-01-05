package org.plovdev.audioengine.examples;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixerExample {
    private static final Logger log = LoggerFactory.getLogger(MixerExample.class);

    public static void main(String[] args) {
        print("Hello");
        print(null);
    }
    private static void print(@NotNull String str) {
        System.out.println(str);
    }
}