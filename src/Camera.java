
public class Camera {
    public Vector3 position;
    public Vector3 front = new Vector3(0, 0, -1);
    public Vector3 up    = new Vector3(0, 1, 0);
    public Vector3 right = new Vector3(1, 0, 0);
    public Vector3 worldUp = new Vector3(0, 1, 0);
 
    // angles in degrees
    public float yaw   = -90.0f; // look toward -Z by default
    public float pitch =   0.0f;

    public Camera(Vector3 position) {
        this.position = position;
        updateCameraVectors();
    }

    public Matrix4 getViewMatrix() {
        Vector3 center = position.add(front);
        return Matrix4.lookAt(position, center, up);
    }
    public void moveForward(float delta)  { 
        Vector3 dir = new Vector3(front.x, 0, front.z).normalize();
        position = position.add(dir.mul(delta)); 
    }

    public void moveBackward(float delta) { 
        Vector3 dir = new Vector3(front.x, 0, front.z).normalize();
        position = position.sub(dir.mul(delta)); 
    }

    public void moveRight(float delta) { 
        Vector3 dir = new Vector3(right.x, 0, right.z).normalize();
        position = position.add(dir.mul(delta)); 
    }

    public void moveLeft(float delta) { 
        Vector3 dir = new Vector3(right.x, 0, right.z).normalize();
        position = position.sub(dir.mul(delta)); 
    }


    public void moveUp(float delta)       { position = position.add(up.mul(delta));    }
    public void moveDown(float delta)     { position = position.sub(up.mul(delta));    }
    public void addMouseDelta(float dx, float dy, float sensitivity) {
        yaw   += dx * sensitivity;
        pitch -= dy * sensitivity;          // invert Y so moving mouse up looks up
        if (pitch > 89.0f)  pitch = 89.0f;  // clamp to avoid flip
        if (pitch < -89.0f) pitch = -89.0f;
        updateCameraVectors();
    }

    private void updateCameraVectors() {
        double cy = Math.cos(Math.toRadians(yaw));
        double sy = Math.sin(Math.toRadians(yaw));
        double cp = Math.cos(Math.toRadians(pitch));
        double sp = Math.sin(Math.toRadians(pitch));

        // Forward vector (direction you're looking)
        front = new Vector3((float)(cy * cp), (float)sp, (float)(sy * cp)).normalize();

        // Right vector, always relative to worldUp
        right = front.cross(worldUp).normalize();

        // Keep UP locked to world Y (no tilting)
        up = worldUp;
    }
}
