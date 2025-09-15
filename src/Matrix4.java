import java.nio.FloatBuffer;

public class Matrix4 {
    public float[] m = new float[16];

    public Matrix4() {
        identity();
    }

    public Matrix4 identity() {
        for (int i=0; i<16; i++) m[i] = 0;
        m[0] = 1; m[5] = 1; m[10] = 1; m[15] = 1;
        return this;
    }

    public Matrix4 multiply(Matrix4 other) {
        Matrix4 result = new Matrix4();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                result.m[col*4 + row] =
                    m[0*4 + row] * other.m[col*4 + 0] +
                    m[1*4 + row] * other.m[col*4 + 1] +
                    m[2*4 + row] * other.m[col*4 + 2] +
                    m[3*4 + row] * other.m[col*4 + 3];
            }
        }

        this.m = result.m;
        return this;
    }

    public static Matrix4 translation(float x, float y, float z) {
        Matrix4 mat = new Matrix4().identity();
        mat.m[12] = x;
        mat.m[13] = y;
        mat.m[14] = z;
        return mat;
    }

    public static Matrix4 scale(float x, float y, float z) {
        Matrix4 mat = new Matrix4().identity();
        mat.m[0] = x;
        mat.m[5] = y;
        mat.m[10] = z;
        return mat;
    }

    public static Matrix4 rotationX(float angleRadians) {
        Matrix4 mat = new Matrix4().identity();
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        mat.m[5] = c;
        mat.m[6] = s;
        mat.m[9] = -s;
        mat.m[10] = c;
        return mat;
    }

    public static Matrix4 rotationY(float angleRadians) {
        Matrix4 mat = new Matrix4().identity();
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        mat.m[0] = c;
        mat.m[2] = -s;
        mat.m[8] = s;
        mat.m[10] = c;
        return mat;
    }

    public static Matrix4 rotationZ(float angleRadians) {
        Matrix4 mat = new Matrix4().identity();
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        mat.m[0] = c;
        mat.m[1] = s;
        mat.m[4] = -s;
        mat.m[5] = c;
        return mat;
    }

    public static Matrix4 perspective(float fovRadians, float aspect, float near, float far) {
        Matrix4 mat = new Matrix4();
        float f = (float)(1.0 / Math.tan(fovRadians / 2.0));
        mat.m[0] = f / aspect;
        mat.m[5] = f;
        mat.m[10] = (far + near) / (near - far);
        mat.m[11] = -1;
        mat.m[14] = (2 * far * near) / (near - far);
        mat.m[15] = 0;
        return mat;
    }

    public static Matrix4 lookAt(Vector3f eye, Vector3f center, Vector3f up) {
        Vector3f f = center.sub(eye).normalize();
        Vector3f s = f.cross(up).normalize();
        Vector3f u = s.cross(f);

        Matrix4 mat = new Matrix4().identity();

        mat.m[0] = s.x;
        mat.m[4] = s.y;
        mat.m[8] = s.z;

        mat.m[1] = u.x;
        mat.m[5] = u.y;
        mat.m[9] = u.z;

        mat.m[2] = -f.x;
        mat.m[6] = -f.y;
        mat.m[10] = -f.z;

        mat.m[12] = -s.dot(eye);
        mat.m[13] = -u.dot(eye);
        mat.m[14] = f.dot(eye);

        return mat;
    }
    public void get(FloatBuffer buffer) {
        buffer.put(m);
        buffer.flip();  // prepare buffer for reading by OpenGL
    }

}
