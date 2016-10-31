package AudioAnalysis.decoder;

public class WindowFactory {
    public static HammingWindow HammingWindow(int windowSize) {
        return new HammingWindow(windowSize);
    }

    public static HammingWindow HammingWindow() {
        return new HammingWindow();
    }
}
