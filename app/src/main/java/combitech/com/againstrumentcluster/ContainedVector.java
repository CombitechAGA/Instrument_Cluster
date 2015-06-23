package combitech.com.againstrumentcluster;

public class ContainedVector {

    private double[] data;
    private final int N;

    private double sum;

    public ContainedVector(double... data) {
        N = data.length;
        this.data = new double[data.length];
        for (int i = 0; i < data.length; i++)
            this.data[i] = data[i];
    }

    public ContainedVector(int length) {
        N = length;
        data = new double[length];
    }

    public int length() { return N; }

    /**
     * @param that
     * @return the inner product of this Vector a and that Vector b
     */
    public double dot(ContainedVector that) {
        if (this.N != that.N)
            throw new RuntimeException("Dimensions do not match (this: " + this.length() + ", that: " + that.length());
        sum = 0.0;
        for (int i = 0; i < N; i++)
            sum = sum + (this.data[i] * that.data[i]);
        return sum;
    }

    /**
     * @param that
     * @return this + that
     */
    public ContainedVector plus(ContainedVector that) {
        if (this.N != that.N)
            throw new RuntimeException("Dimensions do not match (this: " + this.length() + ", that: " + that.length());
        for (int i = 0; i < N; i++)
            data[i] = data[i] + that.data[i];
        return this;
    }

    /**
     * @param that
     * @return this - that
     */
    public ContainedVector minus(ContainedVector that) {
        if (this.N != that.N)
            throw new RuntimeException("Dimensions do not match (this: " + this.length() + ", that: " + that.length());
        for (int i = 0; i < N; i++)
            data[i] = data[i] - that.data[i];
        return this;
    }

    /**
     * @param factor
     * @return this * factor
     */
    public ContainedVector times(double factor) {
        for (int i = 0; i < N; i++)
            data[i] = factor * data[i];
        return this;
    }

    /**
     * @param i
     * @return the corresponding coordinate
     */
    public double cartesian(int i) {
        return data[i];
    }


    /**
     * @return the Euclidean norm of this Vector
     */
    public double magnitude() {
        return Math.sqrt(this.dot(this));
    }

    /**
     * @param that
     * @return the Euclidean distance between this and that
     */
    public double distanceTo(ContainedVector that) {
        if (this.N != that.N)
            throw new RuntimeException("Dimensions do not match (this: " + this.length() + ", that: " + that.length());
        return this.minus(that).magnitude();
    }

    /**
     * @return the corresponding unit vector
     */
    public ContainedVector direction() {
        if (this.magnitude() == 0.0) throw new RuntimeException("Zero-vector has no direction");
        return this.times(1.0 / this.magnitude());
    }

    public void set(double... data) {
        if (data.length != N)
            throw new RuntimeException("Dimensions do not match (this: " + this.length() + ", that: " + data.length);
        for (int i = 0; i < data.length; i++)
            this.data[i] = data[i];
    }
}
