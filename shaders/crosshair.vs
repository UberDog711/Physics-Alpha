#version 150

in vec3 aPos;

void main() {
    // Crosshair is in normalized device coordinates (NDC)
    gl_Position = vec4(aPos, 1.0);
}
