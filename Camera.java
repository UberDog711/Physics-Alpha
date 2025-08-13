public class Camera {
    public Vector3 position;
    public Vector3 front;
    public Vector3 up;

    public Camera(Vector3 position) {
        this.position = position;
        this.front = new Vector3(0, 0, -1);
        this.up = new Vector3(0, 1, 0);
    }

    public Matrix4 getViewMatrix() {
        Vector3 center = position.add(front);
        return Matrix4.lookAt(position, center, up);
    }

    public void moveForward(float delta) {
        position = position.add(front.mul(delta));
    }

    public void moveBackward(float delta) {
        position = position.sub(front.mul(delta));
    }

    public void moveRight(float delta) {
        Vector3 right = front.cross(up).normalize();
        position = position.add(right.mul(delta));
    }

    public void moveLeft(float delta) {
        Vector3 right = front.cross(up).normalize();
        position = position.sub(right.mul(delta));
    }
}
