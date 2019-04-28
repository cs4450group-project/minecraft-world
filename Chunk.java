/***************************************************************
* file: Chunk.java
* authors: Jeremy Canning, Dylan Chung, Camron Fortenbery, Grant Posner
* class: CS 4450: Computer Graphics
*
* assignment: Final Project
* date last modified: 4/28/2019
*
* purpose: Is the chunk. Contains render data, invokes simplex
* noise and other methods for realistic terrain generation. 
* Constructor ensures that top layer is grass, water, or sand (based 
* on height, and water is level), the first three below that are dirt, 
* the layers below that are stone or dirt (favoring stone), and the 
* bottom layer is bedrock. Renders the chunk.
*
****************************************************************/ 
package graphically_inclined_final_project;
import java.nio.FloatBuffer;
import java.util.Random;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class Chunk {
    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    private int seed;
    private Block[][][] blocks;
    public int genX;
    public int genY;
    public int genZ;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private Texture texture;
    private SimplexNoise sNoise;
    private Random r;
    static private ChunkType type = Chunk.ChunkType.ChunkType_Overworld;
    
    public enum ChunkType {
        ChunkType_Overworld(0),
        ChunkType_Nether(1);
        private int chunkID;
        ChunkType(int i) {
            chunkID = i;
        }
        public int getID() {
            return chunkID;
        }
        public void setID(int i) {
            chunkID = i;
        }
    }
    
    public Chunk(int startX, int startY, int startZ, int s) {
        try {
            texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("terrain.png"));
        } catch (Exception e) {
            System.err.println("Error loading terrain: terrain.png not found");
        }
        genX = startX;
        genY = startY;
        genZ = startZ;
        seed = s;
        sNoise = new SimplexNoise(120, 0.6, seed);
        r = new Random();
        int height;
        blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = -startX; x < -startX + CHUNK_SIZE; x++) {
            for (int z = -startZ; z < -startZ + CHUNK_SIZE; z++) {
                height = (int) (((sNoise.getNoise((int)x + startX/2, (int)z + startZ/2)) + 1) / 2 * 10) + 5;
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    if (y == 0) {
                        blocks[x + startX][y][z + startZ] = new Block(Block.BlockType.BlockType_Bedrock);
                    } else if (y < height - 3) {
                        if (r.nextFloat() > 0.1) {
                            blocks[x + startX][y][z + startZ] = new Block(Block.BlockType.BlockType_Stone);
                        } else {
                            blocks[x + startX][y][z + startZ] = new Block(Block.BlockType.BlockType_Dirt);
                        }
                    } else if (y < height) {
                        blocks[x + startX][y][z + startZ] = new Block(Block.BlockType.BlockType_Dirt);
                    } else {
                        if (height > 10) {
                            blocks[x + startX][y][z + startZ] = new Block(Block.BlockType.BlockType_Grass);
                        } else {
                            blocks[x + startX][y][z + startZ] = new Block(Block.BlockType.BlockType_Sand);
                        }
                    }
                }
                if (height < 11) {
                    for (int y = height + 1; y < CHUNK_SIZE; y++) {
                        blocks[x + startX][y][z + startZ] = new Block(Block.BlockType.BlockType_Water);
                    }
                }
            }
        }
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        rebuildMesh(startX, startY, startZ);
    }
    
    // method: createCubeVertexCol
    // purpose: Takes a float array for colors and returns a float
    // array for 24 copies of that color data for the cube vertices
    // to be colored in. Colors everything white except grass tops,
    // which are colored green.
    private float[] createCubeVertexCol(float[] cubeColorArray) {
        float[] cubeColors = new float[cubeColorArray.length * 24];
        if (cubeColorArray[0] == 0) {
            for (int i = 0; i < cubeColors.length; i++) {
                cubeColors[i] = 1;
            }
            for (int i = 0; i < 12; i += 3) {
                cubeColors[i] = 0.8f;
                cubeColors[i + 1] = 1;
                cubeColors[i + 2] = 0.5f;
            }
            return cubeColors;
        }
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = cubeColorArray[i % cubeColorArray.length];
        }
        return cubeColors;
    }
    
    // method: getCubeColor
    // purpose: Returns default value of {1, 1, 1} for all block
    // types except grass, which is given {0, 0, 0} for the purposes
    // of recognizing it in the createCubeVertexCol method.
    private float[] getCubeColor(Block block) {
        if (block.getID() == 0 && type.getID() == 0) {
            return new float[] {0, 0, 0};
        } 
        return new float[] {1, 1, 1};
    }
    
    // method: render
    // purpose: Renders the chunk.
    public void render() {
        glPushMatrix();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0l);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3, GL_FLOAT, 0, 0l);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2, GL_FLOAT, 0, 0l);
        glDrawArrays(GL_QUADS, 0, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 24);
        glPopMatrix();
    }
    
    // method: rebuildMesh
    // purpose: Places cube position, color, and texture data into the
    // buffers to prepare for rendering.
    public void rebuildMesh(float initX, float initY, float initZ) {
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        int height;
        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 72);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 72);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 72);
        for (float x = -initX; x < -initX + CHUNK_SIZE; x++) {
            for (float z = -initZ; z < -initZ + CHUNK_SIZE; z++) {
                height = (int) (((sNoise.getNoise((int)x + (int)initX/2, (int)z + (int)initZ/2)) + 1) / 2 * 10) + 5;
                for (float y = 0; y <= height; y++) {
                    VertexPositionData.put(createCube((float)(initX + x * CUBE_LENGTH), (float)(initY + y * CUBE_LENGTH), (float)(initZ + z * CUBE_LENGTH)));
                    VertexColorData.put(createCubeVertexCol(getCubeColor(blocks[(int) x + (int)initX][(int) y][(int) z + (int)initZ])));
                    VertexTextureData.put(createTexCube((float) 0, (float) 0, blocks[(int) x + (int)initX][(int) y][(int) z + (int)initZ]));
                }
                if (height < 11) {
                    for (float y = height; y < 11; y++) {
                        VertexPositionData.put(createCube((float)(initX + x * CUBE_LENGTH), (float)(initY + y * CUBE_LENGTH), (float)(initZ + z * CUBE_LENGTH)));
                        VertexColorData.put(createCubeVertexCol(getCubeColor(blocks[(int) x + (int)initX][(int) y][(int) z + (int)initZ])));
                        VertexTextureData.put(createTexCube((float) 0, (float) 0, blocks[(int) x + (int)initX][(int) y][(int) z + (int)initZ]));
                    }
                }
            }
        }
        VertexColorData.flip();
        VertexPositionData.flip();
        VertexTextureData.flip();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexColorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    // method: createCube
    // purpose: Returns a float array of position data necessary for
    // rendering a cube.
    public static float[] createCube (float x, float y, float z) {
        int offset = CUBE_LENGTH/2;
        return new float[] {
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z,
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x - offset, y + offset, z,
            x - offset, y - offset, z,
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y + offset, z,
            x + offset, y + offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z
        };
    }
    
    // method: createTexCube
    // purpose: Textures cubes based on its BlockType. Default texture
    // is the red one to the side with the text on it.
    public static float[] createTexCube(float x, float y, Block block) {
        float offset = 1/16f;
        switch(type.getID()) {
            case 0:
                switch(block.getID()) {
                    case 0:
                        return new float[] {
                            x + offset * 1, y + offset * 1,
                            x + offset * 0, y + offset * 1,
                            x + offset * 0, y + offset * 0,
                            x + offset * 1, y + offset * 0,
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 4, y + offset * 0,
                            x + offset * 4, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 4, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 3, y + offset * 0,
                            x + offset * 4, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 4, y + offset * 0,
                            x + offset * 4, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 3, y + offset * 0,
                            x + offset * 4, y + offset * 0,
                            x + offset * 4, y + offset * 1,
                            x + offset * 3, y + offset * 1
                        };
                    case 1:
                        return new float[] {
                            x + offset * 3, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 2, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 3, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 2, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 3, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 3, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 2, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 3, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 2, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 3, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 2, y + offset * 1,
                            x + offset * 3, y + offset * 1
                        };
                    case 2:
                        return new float[] {
                            x + offset * 14, y + offset * 13,
                            x + offset * 13, y + offset * 13,
                            x + offset * 13, y + offset * 12,
                            x + offset * 14, y + offset * 12,
                            x + offset * 14, y + offset * 13,
                            x + offset * 13, y + offset * 13,
                            x + offset * 13, y + offset * 12,
                            x + offset * 14, y + offset * 12,
                            x + offset * 13, y + offset * 12,
                            x + offset * 14, y + offset * 12,
                            x + offset * 14, y + offset * 13,
                            x + offset * 13, y + offset * 13,
                            x + offset * 14, y + offset * 13,
                            x + offset * 13, y + offset * 13,
                            x + offset * 13, y + offset * 12,
                            x + offset * 14, y + offset * 12,
                            x + offset * 14, y + offset * 13,
                            x + offset * 13, y + offset * 13,
                            x + offset * 13, y + offset * 12,
                            x + offset * 14, y + offset * 12,
                            x + offset * 14, y + offset * 13,
                            x + offset * 13, y + offset * 13,
                            x + offset * 13, y + offset * 12,
                            x + offset * 14, y + offset * 12
                        };
                    case 3:
                        return new float[] {
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 2, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 0,
                            x + offset * 3, y + offset * 0,
                            x + offset * 3, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 0,
                            x + offset * 3, y + offset * 0
                        };
                    case 4:
                        return new float[] {
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 1, y + offset * 0,
                            x + offset * 2, y + offset * 0,
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 1, y + offset * 0,
                            x + offset * 2, y + offset * 0,
                            x + offset * 1, y + offset * 0,
                            x + offset * 2, y + offset * 0,
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 1, y + offset * 0,
                            x + offset * 2, y + offset * 0,
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 1, y + offset * 0,
                            x + offset * 2, y + offset * 0,
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 1, y + offset * 0,
                            x + offset * 2, y + offset * 0
                        };
                    case 5:
                        return new float[] {
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1
                        };
                }
            case 1:
                switch(block.getID()) {
                    case 0:
                        return new float[] {
                            x + offset * 4, y + offset * 2,
                            x + offset * 3, y + offset * 2,
                            x + offset * 3, y + offset * 1,
                            x + offset * 4, y + offset * 1,
                            x + offset * 4, y + offset * 2,
                            x + offset * 3, y + offset * 2,
                            x + offset * 3, y + offset * 1,
                            x + offset * 4, y + offset * 1,
                            x + offset * 3, y + offset * 1,
                            x + offset * 4, y + offset * 1,
                            x + offset * 4, y + offset * 2,
                            x + offset * 3, y + offset * 2,
                            x + offset * 4, y + offset * 2,
                            x + offset * 3, y + offset * 2,
                            x + offset * 3, y + offset * 1,
                            x + offset * 4, y + offset * 1,
                            x + offset * 4, y + offset * 2,
                            x + offset * 3, y + offset * 2,
                            x + offset * 3, y + offset * 1,
                            x + offset * 4, y + offset * 1,
                            x + offset * 4, y + offset * 2,
                            x + offset * 3, y + offset * 2,
                            x + offset * 3, y + offset * 1,
                            x + offset * 4, y + offset * 1
                        };
                    case 1:
                        return new float[] {
                            x + offset * 9, y + offset * 7,
                            x + offset * 8, y + offset * 7,
                            x + offset * 8, y + offset * 6,
                            x + offset * 9, y + offset * 6,
                            x + offset * 9, y + offset * 7,
                            x + offset * 8, y + offset * 7,
                            x + offset * 8, y + offset * 6,
                            x + offset * 9, y + offset * 6,
                            x + offset * 8, y + offset * 6,
                            x + offset * 9, y + offset * 6,
                            x + offset * 9, y + offset * 7,
                            x + offset * 8, y + offset * 7,
                            x + offset * 9, y + offset * 7,
                            x + offset * 8, y + offset * 7,
                            x + offset * 8, y + offset * 6,
                            x + offset * 9, y + offset * 6,
                            x + offset * 9, y + offset * 7,
                            x + offset * 8, y + offset * 7,
                            x + offset * 8, y + offset * 6,
                            x + offset * 9, y + offset * 6,
                            x + offset * 9, y + offset * 7,
                            x + offset * 8, y + offset * 7,
                            x + offset * 8, y + offset * 6,
                            x + offset * 9, y + offset * 6
                        };
                    case 2:
                        return new float[] {
                            x + offset * 14, y + offset * 15,
                            x + offset * 13, y + offset * 15,
                            x + offset * 13, y + offset * 14,
                            x + offset * 14, y + offset * 14,
                            x + offset * 14, y + offset * 15,
                            x + offset * 13, y + offset * 15,
                            x + offset * 13, y + offset * 14,
                            x + offset * 14, y + offset * 14,
                            x + offset * 13, y + offset * 14,
                            x + offset * 14, y + offset * 14,
                            x + offset * 14, y + offset * 15,
                            x + offset * 13, y + offset * 15,
                            x + offset * 14, y + offset * 15,
                            x + offset * 13, y + offset * 15,
                            x + offset * 13, y + offset * 14,
                            x + offset * 14, y + offset * 14,
                            x + offset * 14, y + offset * 15,
                            x + offset * 13, y + offset * 15,
                            x + offset * 13, y + offset * 14,
                            x + offset * 14, y + offset * 14,
                            x + offset * 14, y + offset * 15,
                            x + offset * 13, y + offset * 15,
                            x + offset * 13, y + offset * 14,
                            x + offset * 14, y + offset * 14
                        };
                    case 3:
                        return new float[] {
                            x + offset * 8, y + offset * 7,
                            x + offset * 7, y + offset * 7,
                            x + offset * 7, y + offset * 6,
                            x + offset * 8, y + offset * 6,
                            x + offset * 8, y + offset * 7,
                            x + offset * 7, y + offset * 7,
                            x + offset * 7, y + offset * 6,
                            x + offset * 8, y + offset * 6,
                            x + offset * 7, y + offset * 6,
                            x + offset * 8, y + offset * 6,
                            x + offset * 8, y + offset * 7,
                            x + offset * 7, y + offset * 7,
                            x + offset * 8, y + offset * 7,
                            x + offset * 7, y + offset * 7,
                            x + offset * 7, y + offset * 6,
                            x + offset * 8, y + offset * 6,
                            x + offset * 8, y + offset * 7,
                            x + offset * 7, y + offset * 7,
                            x + offset * 7, y + offset * 6,
                            x + offset * 8, y + offset * 6,
                            x + offset * 8, y + offset * 7,
                            x + offset * 7, y + offset * 7,
                            x + offset * 7, y + offset * 6,
                            x + offset * 8, y + offset * 6
                        };
                    case 4:
                        return new float[] {
                            x + offset * 1, y + offset * 15,
                            x + offset * 0, y + offset * 15,
                            x + offset * 0, y + offset * 14,
                            x + offset * 1, y + offset * 14,
                            x + offset * 1, y + offset * 15,
                            x + offset * 0, y + offset * 15,
                            x + offset * 0, y + offset * 14,
                            x + offset * 1, y + offset * 14,
                            x + offset * 0, y + offset * 14,
                            x + offset * 1, y + offset * 14,
                            x + offset * 1, y + offset * 15,
                            x + offset * 0, y + offset * 15,
                            x + offset * 1, y + offset * 15,
                            x + offset * 0, y + offset * 15,
                            x + offset * 0, y + offset * 14,
                            x + offset * 1, y + offset * 14,
                            x + offset * 1, y + offset * 15,
                            x + offset * 0, y + offset * 15,
                            x + offset * 0, y + offset * 14,
                            x + offset * 1, y + offset * 14,
                            x + offset * 1, y + offset * 15,
                            x + offset * 0, y + offset * 15,
                            x + offset * 0, y + offset * 14,
                            x + offset * 1, y + offset * 14
                        };
                    case 5:
                        return new float[] {
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1,
                            x + offset * 2, y + offset * 2,
                            x + offset * 1, y + offset * 2,
                            x + offset * 1, y + offset * 1,
                            x + offset * 2, y + offset * 1
                        };
                }
        }
        
        return new float[] {
            x + offset * 15, y + offset * 1,
            x + offset * 16, y + offset * 1,
            x + offset * 16, y + offset * 2,
            x + offset * 15, y + offset * 2,
            x + offset * 15, y + offset * 1,
            x + offset * 16, y + offset * 1,
            x + offset * 16, y + offset * 2,
            x + offset * 15, y + offset * 2,
            x + offset * 15, y + offset * 1,
            x + offset * 16, y + offset * 1,
            x + offset * 16, y + offset * 2,
            x + offset * 15, y + offset * 2,
            x + offset * 15, y + offset * 1,
            x + offset * 16, y + offset * 1,
            x + offset * 16, y + offset * 2,
            x + offset * 15, y + offset * 2,
            x + offset * 15, y + offset * 1,
            x + offset * 16, y + offset * 1,
            x + offset * 16, y + offset * 2,
            x + offset * 15, y + offset * 2,
            x + offset * 15, y + offset * 1,
            x + offset * 16, y + offset * 1,
            x + offset * 16, y + offset * 2,
            x + offset * 15, y + offset * 2
        };
    }
    
    // method: changeType
    // purpose: Changes the chunk's type.
    public void changeType(int t) {
        switch (t) {
            case 0:
                type = Chunk.ChunkType.ChunkType_Overworld;
                break;
            case 1:
                type = Chunk.ChunkType.ChunkType_Nether;
                break;
        }
    }
}
