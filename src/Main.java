import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;

public class Main {
    private ArrayList<Chunk> chunks = new ArrayList<>();
    public static int CHUNK_SIZE = 128; // smaller for testing; increase later
    public static int RENDER_DISTANCE = 8;
    private long window;
    private int shaderProgram;
    private Camera camera;
    private Matrix4 projection;
    private final int WIDTH = 3840;
    private final int HEIGHT = 2160;
    private double lastTime = glfwGetTime();
    private int nbFrames = 1;
    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;
    private float mouseSensitivity = 0.15f;
    private double lastFrameTime = glfwGetTime();
    private float deltaTime = 0f;
    private float og_speed = 50.0f;
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

        // load shader once (must match attribute/uniform names used in Chunk)
        shaderProgram = ShaderUtils.loadShader("shaders/vertex.glsl", "shaders/fragment.glsl");

        // Make Chunks (pass shaderProgram to each)
        double tot = (RENDER_DISTANCE*2+1) * (RENDER_DISTANCE * 2+1);
        double cur = 0;

        for (int cx = -RENDER_DISTANCE; cx <= RENDER_DISTANCE; cx++) {
            for (int cz = -RENDER_DISTANCE; cz <= RENDER_DISTANCE; cz++) {
                // place chunks in world coordinates (cx*CHUNK_SIZE, cz*CHUNK_SIZE)
                Chunk chunk = new Chunk(cx * CHUNK_SIZE, cz * CHUNK_SIZE, shaderProgram);
                chunk.create_world();
                chunks.add(chunk);

                cur ++;
                System.out.println(cur/tot*100);
            }
        }

        // Setup camera near center of generated chunks
        // Place camera above center of chunks so terrain is visible immediately
        float worldCenterX = 0f + (RENDER_DISTANCE * CHUNK_SIZE);
        float worldCenterZ = 0f + (RENDER_DISTANCE * CHUNK_SIZE);
        camera = new Camera(new Vector3(worldCenterX, 30f, worldCenterZ + 10f)); // tweak as needed
        projection = Matrix4.perspective((float)Math.toRadians(100), (float)WIDTH/HEIGHT, 0.1f, 4800f);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE); // disable culling while testing; enable later if winding is correct
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

            // Use shader and set uniforms once per frame
            glUseProgram(shaderProgram);
            int viewLoc = glGetUniformLocation(shaderProgram, "view");
            int projLoc = glGetUniformLocation(shaderProgram, "projection");

            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                camera.getViewMatrix().get(fb);
                glUniformMatrix4fv(viewLoc, false, fb);
                projection.get(fb);
                glUniformMatrix4fv(projLoc, false, fb);
            }

            glClearColor(0.53f, 0.8f, 0.92f, 1.0f); // light sky
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // render all chunks (they rely on the program & uniforms already set)
            for (Chunk chunk : chunks) {
                chunk.render();
            }

            glfwSwapBuffers(window);
        }
    }

    private void processInput() {
        
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) og_speed+=1.0f;
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) og_speed-=1.0f;
        float speed = og_speed * deltaTime; // adjust
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) camera.moveForward(speed);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) camera.moveBackward(speed);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) camera.moveLeft(speed);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) camera.moveRight(speed);
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) camera.moveUp(speed);
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) camera.moveDown(speed);
    }
}
