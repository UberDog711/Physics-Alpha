public class PerlinNoise {

    private static final int[] permutation = {
        151,160,137,91,90,15,
        131,13,201,95,96,53,194,233,7,225,
        140,36,103,30,69,142,8,99,37,240,
        21,10,23,190,6,148,247,120,234,75,
        0,26,197,62,94,252,219,203,117,35,
        11,32,57,177,33,88,237,149,56,87,
        174,20,125,136,171,168,68,175,74,
        165,71,134,139,48,27,166,77,146,158,
        231,83,111,229,122,60,211,133,230,220,
        105,92,41,55,46,245,40,244,102,143,
        54,65,25,63,161,1,216,80,73,209,
        76,132,187,208,89,18,169,200,196,135,
        130,116,188,159,86,164,100,109,198,173,
        186,3,64,52,217,226,250,124,123,5,
        202,38,147,118,126,255,82,85,212,207,
        206,59,227,47,16,58,17,182,189,28,
        42,223,183,170,213,119,248,152,2,44,
        154,163,70,221,153,101,155,167,43,172,
        9,129,22,39,253,19,98,108,110,79,
        113,224,232,178,185,112,104,218,246,97,
        228,251,34,242,193,238,210,144,12,191,
        179,162,241,81,51,145,235,249,14,239,
        107,49,192,214,31,181,199,106,157,184,
        84,204,176,115,121,50,45,127,4,150,
        254,138,236,205,93,222,114,67,29,24,
        72,243,141,128,195,78,66,215,61,156,
        180
    };

    private static final int[] p = new int[512];
    static {
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i] = permutation[i];
        }
    }

    public static float[][] generateNoiseMap(int width, int height, float scale, int seed, int octaves, float persistence, float lacunarity, int worldOffsetX, int worldOffsetY) {
        float[][] noiseMap = new float[height][width];
        java.util.Random rand = new java.util.Random(seed);
        float[] octaveOffsetsX = new float[octaves];    
        float[] octaveOffsetsY = new float[octaves];
        for (int i = 0; i < octaves; i++) {
            octaveOffsetsX[i] = rand.nextFloat() * 20000f - 10000f;
            octaveOffsetsY[i] = rand.nextFloat() * 20000f - 10000f;
        }

        if (scale <= 0) scale = 0.0001f;

        // Calculate max possible amplitude for normalization
        float maxAmplitude = 0f;
        float amp = 1f;
        for (int i = 0; i < octaves; i++) {
            maxAmplitude += amp;
            amp *= persistence;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float amplitude = 1f;
                float frequency = 0.001f;
                float noiseHeight = 0f;

                for (int o = 0; o < octaves; o++) {
                    float sampleX = ((x + worldOffsetX) / scale) * frequency + octaveOffsetsX[o];
                    float sampleY = ((y + worldOffsetY) / scale) * frequency + octaveOffsetsY[o];

                    float perlinValue = perlin(sampleX, sampleY) * 2f - 1f;
                    noiseHeight += perlinValue * amplitude;

                    amplitude *= persistence;
                    frequency *= lacunarity;
                }

                // Normalize noiseHeight to 0-1 range based on max amplitude sum
                float normalizedHeight = (noiseHeight + maxAmplitude) / (2f * maxAmplitude);

                // Scale to desired range (-100 to 100)
                noiseMap[y][x] = normalizedHeight * 200f - 100f;
            }
        }

        return noiseMap;
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static float grad(int hash, float x, float y) {
        int h = hash & 15;
        float u = h < 8 ? x : y;
        float v = h < 4 ? y : (h == 12 || h == 14 ? x : 0);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public static float perlin(float x, float y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        float u = fade(x);
        float v = fade(y);

        int A = p[X] + Y, AA = p[A], AB = p[A + 1];
        int B = p[X + 1] + Y, BA = p[B], BB = p[B + 1];

        float res = lerp(
            lerp(grad(p[AA], x, y), grad(p[BA], x - 1, y), u),
            lerp(grad(p[AB], x, y - 1), grad(p[BB], x - 1, y - 1), u),
            v
        );
        return (res + 1) / 2f;  // Normalize to 0–1
    }
}
