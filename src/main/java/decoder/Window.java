package decoder;


public abstract class Window {
    protected double[] weights;
    protected int windowLength;

    private final int DEFAULT_WINDOW_LENGTH = 1024;

    public Window() {
        this.windowLength = DEFAULT_WINDOW_LENGTH;
        weights = new double[this.windowLength];
        calculateWeights();
    }

    public Window(int windowLength) {
        this.windowLength = windowLength;
        weights = new double[this.windowLength];
        calculateWeights();
    }

    public abstract void calculateWeights();

    public double getWeight(int index) {
        return weights[index];
    }
}
