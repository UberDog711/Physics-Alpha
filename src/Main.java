
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
@SuppressWarnings("unused")



public class Main {
    //Setup
    private long window;
    private int shaderProgram;
    private Camera camera;
    private Matrix4 projection;
    private final int WIDTH = 3840;
    private final int HEIGHT = 2160;
    private final int FOV = 110;

    //Time
    private double lastTime = glfwGetTime();
    private double lastFrameTime = glfwGetTime();
    private int nbFrames = 1;
    private float deltaTime = 0;

    // Mouse
    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;
    private float mouseSensitivity = 0.15f;
    private float og_speed = 50.0f;

    // Data
    private float[] fps_data;

    // Object
    private Object test;
    private Shader shader;
    


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
        // Seting up GLFW
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
        
        


        // Setup Camera
        camera = new Camera(new Vector3f(0, 30f, 0 + 10f)); // tweak as needed
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
        test = new Object(2);
        shader = new Shader("cube.vert","cube.frag");
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {

            double currentTime = glfwGetTime();
            double deltaTime = (float)(currentTime - lastFrameTime);
            lastFrameTime = currentTime;
            
            System.out.println("FPS: " + 1 / deltaTime);
            

            glfwPollEvents();
            processInput();
            glEnable(GL_DEPTH_TEST);
            glClearColor(0.53f, 0.8f, 0.92f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Use Shader
            shader.bind();

            // Create a model matrix (cube at origin)
            Matrix4 model = new Matrix4().identity();
            shader.setUniformMat4f("model", model);

            // Set view & projection
            shader.setUniformMat4f("view", camera.getViewMatrix());
            shader.setUniformMat4f("projection", projection);

            // Render the cube
            test.Render();

            shader.unbind();





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
