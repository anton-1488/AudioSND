import main.java.org.plovdev.audioengine.tracks.meta.TrackMetaData;

public class Test {
    public static void main(String[] args) {
        TrackMetaData data = new TrackMetaData();
        data.addMetadata(TrackMetaData.MetaKey.ALBUM, "plovvs");
        String str = data.getMetadata(TrackMetaData.MetaKey.ALBUM);
        System.out.println(str);
    }
}