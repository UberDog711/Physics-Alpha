public class Vector3 {
    public float x, y, z;

    public Vector3() {
        this(0,0,0);
    }

    public Vector3(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 sub(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 mul(float scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public Vector3 normalize() {
        float length = length();
        if (length == 0) return new Vector3(0,0,0);
        return new Vector3(x/length, y/length, z/length);
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public float dot(Vector3 v) {
        return x*v.x + y*v.y + z*v.z;
    }

    public Vector3 cross(Vector3 v) {
        return new Vector3(
            y * v.z - z * v.y,
            z * v.x - x * v.z,
            x * v.y - y * v.x
        );
    }
}
