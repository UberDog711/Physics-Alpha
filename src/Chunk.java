import java.util.*;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Chunk {
    // constructor now receives world offset and shader program id from Main
    public Chunk(int x, int z, int shaderProgram) {
        this.x = x;
        this.z = z;
        this.shaderProgram = shaderProgram;
    }

    // === FIELDS ===
    private final int x, z;
    private final double startTime = glfwGetTime();

    private final ArrayList<Vector3> vertex_data = new ArrayList<>();
    private final ArrayList<Vector3> color_data  = new ArrayList<>();

    private int vbo_id, cbo_id, vao_id, vertex_count;
    private final int shaderProgram;

    private final Map<Integer, Byte> blocks = new HashMap<>();
    private final int chunk_size = Main.CHUNK_SIZE;

    private final ArrayList<Vector3i> offsets = new ArrayList<>(Arrays.asList(
        new Vector3i(0, 1, 0),  new Vector3i(0, -1, 0),
        new Vector3i(-1, 0, 0), new Vector3i(1, 0, 0),
        new Vector3i(0, 0, 1),  new Vector3i(0, 0, -1)
    ));
    private final Map<Integer, Vector3[]> FACE_DEFS = createFaceDefs();

    private Map<Integer, Vector3[]> createFaceDefs() {
        Map<Integer, Vector3[]> map = new HashMap<>();
        map.put(0, new Vector3[]{ new Vector3(0,1,1), new Vector3(1,1,1), new Vector3(1,1,0), new Vector3(0,1,0) }); // +Y
        map.put(1, new Vector3[]{ new Vector3(0,0,0), new Vector3(1,0,0), new Vector3(1,0,1), new Vector3(0,0,1) }); // -Y
        map.put(2, new Vector3[]{ new Vector3(0,0,1), new Vector3(0,1,1), new Vector3(0,1,0), new Vector3(0,0,0) }); // -X
        map.put(3, new Vector3[]{ new Vector3(1,0,0), new Vector3(1,1,0), new Vector3(1,1,1), new Vector3(1,0,1) }); // +X
        map.put(4, new Vector3[]{ new Vector3(0,0,1), new Vector3(1,0,1), new Vector3(1,1,1), new Vector3(0,1,1) }); // +Z
        map.put(5, new Vector3[]{ new Vector3(0,0,0), new Vector3(0,1,0), new Vector3(1,1,0), new Vector3(1,0,0) }); // -Z
        return map;
    }

    private ArrayList<Float> face_color(int face, int block_type) {
        float BASE_R = 30f/255f, BASE_G = 70f/255f, BASE_B = 40f/255f;
        float mul = (face == 0) ? 1.2f : (face == 1) ? 0.6f : (face == 2 || face == 3) ? 0.8f : 1.0f;
        ArrayList<Float> c = new ArrayList<>(3);
        c.add(BASE_R * mul); c.add(BASE_G * mul); c.add(BASE_B * mul);
        return c;
    }

    private float[] flattenVertexData(ArrayList<Vector3> data) {
        float[] flat = new float[data.size() * 3];
        for (int i = 0; i < data.size(); i++) {
            Vector3 v = data.get(i);
            flat[i*3] = v.x; flat[i*3+1] = v.y; flat[i*3+2] = v.z;
        }
        return flat;
    }

    private float[] flattenColorData(ArrayList<Vector3> data) {
        float[] flat = new float[data.size() * 3];
        for (int i = 0; i < data.size(); i++) {
            Vector3 v = data.get(i);
            flat[i*3] = v.x; flat[i*3+1] = v.y; flat[i*3+2] = v.z;
        }
        return flat;
    }

    public int packPos(int x, int y, int z) {
        return (x & 0x7F) | ((y & 0x7F) << 7) | ((z & 0x7F) << 14);
    }
    public int getX(int p) { return  p        & 0x7F; }
    public int getY(int p) { return (p >> 7)  & 0x7F; }
    public int getZ(int p) { return (p >> 14) & 0x7F; }

    public double chunk_time() { return glfwGetTime() - startTime; }

    // === BUILD GEOMETRY + GPU BUFFERS ===
    public void create_world() {
        // ---- generate simple heightfield blocks ----
        float scale = 150f;
        int seed = 0, octaves = 2;
        float persistence = 0.5f, lacunarity = 0.5f;

        int baseX = this.x;
        int baseZ = this.z;

        Random seeded = new Random(seed);
        float[] offX = new float[octaves], offZ = new float[octaves];
        for (int i = 0; i < octaves; i++) {
            offX[i] = seeded.nextFloat()*20000f - 10000f;
            offZ[i] = seeded.nextFloat()*20000f - 10000f;
        }

        for (int lx = 0; lx < chunk_size; lx++) {
            for (int lz = 0; lz < chunk_size; lz++) {
                int wx = (baseX + lx);
                int wz = (baseZ + lz);
                float amp = 1f, freq = 1f, heightSum = 0f;
                for (int o = 0; o < octaves; o++) {
                    float sx = (wx / scale) * freq + offX[o];
                    float sz = (wz / scale) * freq + offZ[o];
                    float p  = PerlinNoise.perlin(sx, sz) * 2f - 1f;
                    heightSum += p * amp;
                    amp *= persistence;
                    freq *= lacunarity;
                }
                float val = 1f + heightSum;
                int h = (int)(val*val*32f);
                if (h < 0) h = 0;
                if (h > 127) h = 127;

                for (int y = h - 4; y <= h; y++) {
                    if (y >= 0) blocks.put(packPos(lx, y, lz), (byte)0);
                }
            }
        }

        // ---- build visible faces (TRIANGULATED) ----
        for (int packed : blocks.keySet()) {
            int bx = getX(packed), by = getY(packed), bz = getZ(packed);

            for (int face = 0; face < 6; face++) {
                if (face == 1) continue; // optional: skip bottom

                Vector3i off = offsets.get(face);
                int nx = bx + off.x, ny = by + off.y, nz = bz + off.z;

                boolean neighborFilled = false;
                if (nx >= 0 && nx < chunk_size && ny >= 0 && ny < chunk_size && nz >= 0 && nz < chunk_size) {
                    neighborFilled = blocks.containsKey(packPos(nx, ny, nz));
                }
                if (neighborFilled) continue;

                Vector3[] q = FACE_DEFS.get(face);

                // world-space quad corners (positioned by chunk origin x,z)
                Vector3 q0 = new Vector3(q[0].x + bx + this.x, q[0].y + by, q[0].z + bz + this.z);
                Vector3 q1 = new Vector3(q[1].x + bx + this.x, q[1].y + by, q[1].z + bz + this.z);
                Vector3 q2 = new Vector3(q[2].x + bx + this.x, q[2].y + by, q[2].z + bz + this.z);
                Vector3 q3 = new Vector3(q[3].x + bx + this.x, q[3].y + by, q[3].z + bz + this.z);

                // triangulate: (q0,q1,q2) and (q2,q3,q0)
                vertex_data.add(q0); vertex_data.add(q1); vertex_data.add(q2);
                vertex_data.add(q2); vertex_data.add(q3); vertex_data.add(q0);

                ArrayList<Float> c = face_color(face, 0);
                Vector3 col = new Vector3(c.get(0), c.get(1), c.get(2));
                for (int i = 0; i < 6; i++) color_data.add(col);
            }
        }

        // DO NOT load shader here — Main provides shaderProgram.
        float[] flat_vertices = flattenVertexData(vertex_data);
        float[] flat_colors   = flattenColorData(color_data);
        vertex_data.clear();
        color_data.clear();

        vertex_count = flat_vertices.length / 3; // number of vertices

        // ---- GPU buffers ----
        vbo_id = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo_id);
        FloatBuffer vbuf = BufferUtils.createFloatBuffer(flat_vertices.length);
        vbuf.put(flat_vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vbuf, GL_STATIC_DRAW);

        cbo_id = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cbo_id);
        FloatBuffer cbuf = BufferUtils.createFloatBuffer(flat_colors.length);
        cbuf.put(flat_colors).flip();
        glBufferData(GL_ARRAY_BUFFER, cbuf, GL_STATIC_DRAW);

        // VAO (store attribute bindings)
        vao_id = glGenVertexArrays();
        glBindVertexArray(vao_id);

        int posLoc = glGetAttribLocation(shaderProgram, "aPos");
        int colLoc = glGetAttribLocation(shaderProgram, "aColor");

        // position
        glBindBuffer(GL_ARRAY_BUFFER, vbo_id);
        if (posLoc >= 0) {
            glEnableVertexAttribArray(posLoc);
            glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, 0, 0);
        }

        // color
        glBindBuffer(GL_ARRAY_BUFFER, cbo_id);
        if (colLoc >= 0) {
            glEnableVertexAttribArray(colLoc);
            glVertexAttribPointer(colLoc, 3, GL_FLOAT, false, 0, 0);
        }

        // cleanup binds
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void render() {
        glBindVertexArray(vao_id);
        glDrawArrays(GL_TRIANGLES, 0, vertex_count);
        glBindVertexArray(0);
    }
}
