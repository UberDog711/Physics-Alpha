#version 150
in vec3 aPos;
in vec3 aColor;
out vec3 vColor;
uniform mat4 view;
uniform mat4 projection;
void main() {
    vColor = aColor;
    gl_Position = projection * view * vec4(aPos, 1.0);
}
