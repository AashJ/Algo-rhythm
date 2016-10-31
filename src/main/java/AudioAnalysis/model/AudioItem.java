package AudioAnalysis.model;


public class AudioItem {
    public AudioType type;
    public String path;

    public AudioItem(AudioType type, String path) {
        this.type = type;
        this.path = path;
    }

    public String toString(){
        return this.path;
    }

    public enum AudioType {
        WAV, MP3;
    }
}
