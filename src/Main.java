import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;

public class Main {

    private long window;
    private int shaderProgram;
    
    private int vao;
    

    private Camera camera;
    private Matrix4 projection;

    private final int WIDTH = 2560;
    private final int HEIGHT = 1440;
    private double lastTime = glfwGetTime();
    private int nbFrames = 1;
    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;
    private float mouseSensitivity = 0.15f;
    private double lastFrameTime = glfwGetTime();
    private float deltaTime = 0f;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        init();
        loop();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        if (Platform.get() == Platform.MACOSX) glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "3D Camera Example", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);

        GL.createCapabilities();

        // Load main shader
        shaderProgram = ShaderUtils.loadShader("shaders/vertex.glsl", "shaders/fragment.glsl");
        


        // Setup triangle VAO/VBO
        float[] vertices = {
            -5.0f, -5.0f, 0.0f,
             5.0f, -5.0f, 0.0f,
            -5.0f,  5.0f, 0.0f,
            -5.0f,  5.0f, 0.0f,
             5.0f, -5.0f, 0.0f,
             5.0f,  5.0f, 0.0f
        };
        vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer vb = BufferUtils.createFloatBuffer(vertices.length);
        vb.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vb, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

    


        // Setup camera and projection
        camera = new Camera(new Vector3(0, 0, 3));
        projection = Matrix4.perspective((float)Math.toRadians(60), (float)WIDTH/HEIGHT, 0.1f, 100f);

        glEnable(GL_DEPTH_TEST);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            if (firstMouse) { lastMouseX = xpos; lastMouseY = ypos; firstMouse = false; }
            float dx = (float)(xpos - lastMouseX);
            float dy = (float)(ypos - lastMouseY);
            lastMouseX = xpos;
            lastMouseY = ypos;
            camera.addMouseDelta(dx, dy, mouseSensitivity);
        });
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            deltaTime = (float)(currentTime - lastFrameTime);
            lastFrameTime = currentTime;
            nbFrames++;

            if (currentTime - lastTime >= 1.0) {
                System.out.println("FPS: " + nbFrames);
                nbFrames = 0;
                lastTime += 1.0;
            }

            glfwPollEvents();
            processInput();

            int viewLoc = glGetUniformLocation(shaderProgram, "view");
            int projLoc = glGetUniformLocation(shaderProgram, "projection");

            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                camera.getViewMatrix().get(fb);
                glUniformMatrix4fv(viewLoc, false, fb);
                projection.get(fb);
                glUniformMatrix4fv(projLoc, false, fb);
            }

            glClearColor(0.1f,0.1f,0.1f,1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            
            // Draw triangle wall
            glUseProgram(shaderProgram);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);

            glfwSwapBuffers(window);
        }
    }

    private void processInput() {
        float speed = 10.0f * deltaTime;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) camera.moveForward(speed);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) camera.moveBackward(speed);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) camera.moveLeft(speed);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) camera.moveRight(speed);
    }
}
