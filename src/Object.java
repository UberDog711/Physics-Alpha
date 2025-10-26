import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import java.nio.IntBuffer;

import java.util.*;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.Vector;
@SuppressWarnings("unused")
public class Object {
    //Identifiers
    private String name;
    private int object_id;

    //Physics State 
    private Vector3f position;
    private Vector3f velocity;
    private Vector3f acceleration;

    //Physical Properties
    private float mass;
    private float restitution;
    private float friction;
    private float size;

    //Render Properties
    private int vao;
    private int vbo;
    private int ebo;
    private int indexCount;
    

    public Object(float size2) {
        this.size = size2;
        float h = size / 2.0f;

        // pos (3), normal (3), uv (2) = 8 floats per vertex
        float[] vertices = {
            // back face
            -h, -h, -h,  0,  0, -1,  0, 0,
             h, -h, -h,  0,  0, -1,  1, 0,
             h,  h, -h,  0,  0, -1,  1, 1,
            -h,  h, -h,  0,  0, -1,  0, 1,

            // front face
            -h, -h,  h,  0,  0,  1,  0, 0,
             h, -h,  h,  0,  0,  1,  1, 0,
             h,  h,  h,  0,  0,  1,  1, 1,
            -h,  h,  h,  0,  0,  1,  0, 1,

            // left face
            -h, -h, -h, -1,  0,  0,  0, 0,
            -h,  h, -h, -1,  0,  0,  1, 0,
            -h,  h,  h, -1,  0,  0,  1, 1,
            -h, -h,  h, -1,  0,  0,  0, 1,

            // right face
             h, -h, -h,  1,  0,  0,  0, 0,
             h,  h, -h,  1,  0,  0,  1, 0,
             h,  h,  h,  1,  0,  0,  1, 1,
             h, -h,  h,  1,  0,  0,  0, 1,

            // bottom face
            -h, -h, -h,  0, -1,  0,  0, 0,
             h, -h, -h,  0, -1,  0,  1, 0,
             h, -h,  h,  0, -1,  0,  1, 1,
            -h, -h,  h,  0, -1,  0,  0, 1,

            // top face
            -h,  h, -h,  0,  1,  0,  0, 0,
             h,  h, -h,  0,  1,  0,  1, 0,
             h,  h,  h,  0,  1,  0,  1, 1,
            -h,  h,  h,  0,  1,  0,  0, 1,
        };

        int[] indices = {
            0, 1, 2, 2, 3, 0,       // back
            4, 5, 6, 6, 7, 4,       // front
            8, 9,10,10,11, 8,       // left
           12,13,14,14,15,12,       // right
           16,17,18,18,19,16,       // bottom
           20,21,22,22,23,20        // top
        };

        indexCount = indices.length;

        // --- Upload to OpenGL ---
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        // Convert to FloatBuffer
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        // VBO
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Convert to IntBuffer
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        // EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        int stride = 8 * Float.BYTES;
        // position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(0);
        // normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        // texcoord attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6L * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    public void Render() {
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0L);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
    }
}