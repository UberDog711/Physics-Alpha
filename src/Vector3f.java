public class Vector3f {
    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Vector addition
    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    // Vector subtraction
    public Vector3f sub(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    // Scalar multiply
    public Vector3f mul(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    // Cross product
    public Vector3f cross(Vector3f other) {
        return new Vector3f(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    // Dot product
    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    // Magnitude
    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    // Normalization
    public Vector3f normalize() {
        float len = length();
        if (len == 0) return new Vector3f(0, 0, 0);
        return new Vector3f(x / len, y / len, z / len);
    }

    @Override
    public String toString() {
        return "Vector3f(" + x + ", " + y + ", " + z + ")";
    }
}
