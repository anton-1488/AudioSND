package org.plovdev.audioengine.loaders.wav;

import org.plovdev.audioengine.loaders.TrackDecoder;
import org.plovdev.audioengine.loaders.wav.decoders.ALawDecoder;
import org.plovdev.audioengine.loaders.wav.decoders.IMAADPCMDecoder;
import org.plovdev.audioengine.loaders.wav.decoders.ULawDecoder;
import org.plovdev.audioengine.api.Track;
import org.plovdev.audioengine.format.TrackFormat;
import org.plovdev.audioengine.metadata.TrackMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

import static org.plovdev.audioengine.format.TrackFormat.AudioCodec.*;

public class WavTrackDecoder implements TrackDecoder {
    private static final List<TrackFormat.AudioCodec> allowFromDecodeCodecs = List.of(ULAW, ALAW, IMA_ADPCM, MIC_ADPCM, GSM_6);
    private static final List<TrackFormat.AudioCodec> allowToDecodeCodecs = List.of(PCM8, PCM16, PCM32, FLOAT32, FLOAT64);
    private static final Logger log = LoggerFactory.getLogger(WavTrackDecoder.class);

    @Override
    public Track decode(Track input, TrackFormat outFormat) {
        TrackFormat.AudioCodec inCodec = input.getFormat().audioCodec();
        TrackFormat.AudioCodec outCodec = outFormat.audioCodec();

        if (!allowFromDecodeCodecs.contains(inCodec) || !allowToDecodeCodecs.contains(outCodec)) {
            log.warn("Unsupported codec.");
            return input;
        }

        TrackFormat format = input.getFormat();
        Duration inputDuration = input.getDuration();
        TrackMetadata metadata = input.getMetaData();

        ByteBuffer trackData = input.getTrackData().asByteBuffer();
        return switch (inCodec) {
            case ULAW -> new Track(createMemorySegment(new ULawDecoder().decode(trackData)), inputDuration, outFormat, metadata);
            case ALAW -> new Track(createMemorySegment(new ALawDecoder().decode(trackData)), inputDuration, outFormat, metadata);
            case IMA_ADPCM -> new Track(createMemorySegment(new IMAADPCMDecoder(1024).decode(trackData)), inputDuration, outFormat, metadata);
            default -> input;
        };
    }

    private MemorySegment createMemorySegment(ByteBuffer src) {
        Arena arena = Arena.ofAuto();
        MemorySegment resultSegment = arena.allocate(src.capacity());
        resultSegment.asByteBuffer().put(src);
        return resultSegment;
    }
}