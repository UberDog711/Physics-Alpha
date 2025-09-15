import static org.lwjgl.opengl.GL20.*;
import java.nio.file.*;
import java.io.IOException;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;

public class Shader {
    private int programId;

    public Shader(String vertexPath, String fragmentPath) {
        String vertexCode = loadShader(vertexPath);
        String fragmentCode = loadShader(fragmentPath);

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexCode);
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentCode);
        glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);
        checkCompileErrors(programId, "PROGRAM");

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public int getProgramId() { return programId; }

    public void bind() { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }
    public void cleanup() { glDeleteProgram(programId); }

    private String loadShader(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load shader: " + filePath);
            return "";
        }
    }

    private void checkCompileErrors(int shader, String type) {
        if (type.equals("PROGRAM")) {
            if (glGetProgrami(shader, GL_LINK_STATUS) == GL_FALSE) {
                System.err.println("Program Linking Error: " + glGetProgramInfoLog(shader));
            }
        } else {
            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
                System.err.println(type + " Shader Compilation Error: " + glGetShaderInfoLog(shader));
            }
        }
    }

    // Convenient uniform setters
    public void setUniformMat4f(String name, Matrix4 matrix) {
        int loc = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(loc, false, fb);
        }
    }

    public void setUniform3f(String name, Vector3f vec) {
        int loc = glGetUniformLocation(programId, name);
        glUniform3f(loc, vec.x, vec.y, vec.z);
    }
}
