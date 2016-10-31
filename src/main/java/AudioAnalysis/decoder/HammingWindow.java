package AudioAnalysis.decoder;


public class HammingWindow extends Window {
    private final double HAMMING_ALPHA = 0.53836;
    private final double HAMMING_BETA = 0.46164;

    public HammingWindow() {
        super();
    }

    public HammingWindow(int windowSize) {
        super(windowSize);
    }

    @Override
    public void calculateWeights() {
        for (int i = 0; i < windowLength; i++) {
            // w(n) = alpha-beta*cos(2πn/(N-1)) where n = index, N = window length, alpha, beta defined above
            weights[i] = HAMMING_ALPHA - HAMMING_BETA*Math.cos((2*Math.PI*i)/(windowLength-1));
        }
    }
}
