import org.plovdev.audioengine.tracks.meta.TrackMetaData;

public class Test {
    public static void main(String[] args) {
        TrackMetaData data = new TrackMetaData();
        data.setAlbum("plovvs");
        data.getAlbum().ifPresent(System.out::println);
    }
}