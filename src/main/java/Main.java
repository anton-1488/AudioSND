import org.plovdev.audioengine.AudioEngine;
import org.plovdev.audioengine.NativeAudioEngine;
import org.plovdev.audioengine.tracks.TrackPlayer;

public class Main {
    public static void main(String[] args) {
        try (AudioEngine engine = new NativeAudioEngine();
             TrackPlayer player = engine.getTrackPlayer(engine.loadTrack("track.wav"))) {

            player.play();

            Thread.sleep(10000); // listen music
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}