/***************************************************************
* file: FPCameraController.java
* authors: Jeremy Canning, Dylan Chung, Camron Fortenbery, Grant Posner
* class: CS 4450: Computer Graphics
*
* assignment: Final Project
* date last modified: 3/27/2019
*
* purpose: This file contains the necessary code for camera movement 
* and creates the chunk.
*
****************************************************************/ 
package graphically_inclined_checkpoint_2;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;
import java.nio.FloatBuffer;

public class FPCameraController {
        private Vector3f pos = null;
        private Vector3f IPos = null;
        private float yaw = 0.0f;
        private float pitch = 0.0f;
        private double currTime = System.currentTimeMillis();
        private Chunk[] chunks;
        private int seed = (int)System.currentTimeMillis();
        private int numChunks ;
        
        public FPCameraController(float x, float y, float z, int nC) {
            pos = new Vector3f(x,y,z);
            IPos = new Vector3f(x,y,z);
            IPos.x = -x;
            IPos.y = -y + 8;
            IPos.z = -z;
            numChunks = nC;
            chunks = new Chunk[numChunks * numChunks];
            int k = 0;
            for (int i = 0; i < numChunks; i++) {
                for (int j = 0; j < numChunks; j++) {
                    chunks[k] = new Chunk(i * -60, 0, j * -60, seed);
                    k++;
                }
            }
        }
        
        // method: yaw
        // purpose: Adjusts the yaw of the camera based on the value passed to it.
        public void yaw(float amm) {
            yaw += amm;
        }
        
        // method: pitch
        // purpose: Adjusts the pitch of the camera based on the value passed to
        // it. Pitch is locked such that it never passes looking straight up or
        // straight down.
        public void pitch(float amm) {
            pitch -= amm;
            if (pitch > 90.0f) {
                pitch = 90.0f;
            } else if (pitch < -90.0f) {
                pitch = -90.0f;
            }
        }
        
        // method: ahead
        // purpose: Moves camera in the direction it's currently looking at.
        public void ahead(float dist) {
            float xOffset = dist*(float)Math.sin(Math.toRadians(yaw));
            float zOffset = dist*(float)Math.cos(Math.toRadians(yaw));
            pos.x -= xOffset;
            pos.z += zOffset;
            FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
            lightPosition.put(IPos.x).put(IPos.y).put(IPos.z).put(1.0f).flip();
            glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        }
        
        // method: astern
        // purpose: Moves camera in reverse of the direction it's currently 
        // looking at.
        public void astern(float dist) {
            float xOffset = dist*(float)Math.sin(Math.toRadians(yaw));
            float zOffset = dist*(float)Math.cos(Math.toRadians(yaw));
            pos.x += xOffset;
            pos.z -= zOffset;
            FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
            lightPosition.put(IPos.x).put(IPos.y).put(IPos.z).put(1.0f).flip();
            glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        }
        
        // method: aport
        // purpose: Moves camera left with respect to the direction it's 
        // currently looking at.
        public void aport(float dist) {
            float xOffset = dist*(float)Math.sin(Math.toRadians(yaw - 90));
            float zOffset = dist*(float)Math.cos(Math.toRadians(yaw - 90));
            pos.x -= xOffset;
            pos.z += zOffset;
            FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
            lightPosition.put(IPos.x).put(IPos.y).put(IPos.z).put(1.0f).flip();
            glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        }
        
        // method: astarboard
        // purpose: Moves camera right with respect to the direction it's 
        // currently looking at.
        public void astarboard(float dist) {
            float xOffset = dist*(float)Math.sin(Math.toRadians(yaw + 90));
            float zOffset = dist*(float)Math.cos(Math.toRadians(yaw + 90));
            pos.x -= xOffset;
            pos.z += zOffset;
            FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
            lightPosition.put(IPos.x).put(IPos.y).put(IPos.z).put(1.0f).flip();
            glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        }
        
        // method: ascend
        // purpose: Moves camera up.
        public void ascend(float dist) {
            pos.y -= dist;
        }
        
        // method: descend
        // purpose: Moves camera down.
        public void descend(float dist) {
            pos.y += dist;
        }
        
        // method: gaze
        // purpose: Transforms the matrix to provide dynamic 3D viewing of 
        // the scene.
        public void gaze() {
            glRotatef(pitch, 1.0f, 0.0f, 0.0f);
            glRotatef(yaw, 0.0f, 1.0f, 0.0f);
            glTranslatef(pos.x, pos.y, pos.z);
            FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
            lightPosition.put(IPos.x).put(IPos.y).put(IPos.z).put(1.0f).flip();
            glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        }
        
        // method: gameLoop
        // purpose: Displays the scene. Implements camera such that the chunk is 
        // rendered and the camera is controllable by the user. Additional 
        // controls are as follows: Holding Y and scrolling the mouse wheel 
        // raises or lowers the yaw sensitivity. Holding P and scrolling has an
        // equivalent effect on pitch sensitivity. Holding T and scrolling 
        // adjusts both at once. Pressing U sets the pitch sensitivity to the
        // current yaw sensitivity. Pressing O has the reverse effect to 
        // pressing U. Pressing I restores both sensitivities to their default
        // values. Holding M and scrolling raises or lowers the movement speed.
        // Pressing N restores movement speed to its default value. Pressing L
        // and comma simultaneously unlocks the camera from the chunk boundaries.
        // Pressing L and period simultaneously re-engages the lock. Off by 
        // default. Pressing Q and right shift simultaneously enables texture
        // transparency, while pressing Q disables texture transparency. Opaque
        // by default. G and comma activate gravity, during which time the space
        // becomes a jump key, while G and period end gravity mode. 1 loads
        // nether textures, 0 loads overworld textures.
        public void gameLoop() {
            FPCameraController camera = new FPCameraController(pos.x, pos.y, pos.z, numChunks);
            float dx;
            float dy;
            float yawSensitivity = 0.09f;
            float pitchSensitivity = 0.09f;
            float moveSpeed = 0.35f * numChunks;
            boolean grav = false;
            boolean locked = false;
            double initVel = 0;
            float prevYPos = 10;
            Mouse.setGrabbed(true);
            while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                dx = Mouse.getDX();
                dy = Mouse.getDY();
                camera.yaw(dx * yawSensitivity);
                camera.pitch(dy * pitchSensitivity);
                if (Keyboard.isKeyDown(Keyboard.KEY_Y)) {
                    yawSensitivity += (float)Mouse.getDWheel()/12000;
                    if (yawSensitivity < 0.01f) {
                        yawSensitivity = 0.01f;
                    } else if (yawSensitivity > 0.2f) {
                        yawSensitivity = 0.2f;
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                    pitchSensitivity += (float)Mouse.getDWheel()/12000;
                    if (pitchSensitivity < 0.01f) {
                        pitchSensitivity = 0.01f;
                    } else if (pitchSensitivity > 0.2f) {
                        pitchSensitivity = 0.2f;
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
                    float dS = (float)Mouse.getDWheel()/12000;
                    yawSensitivity += dS;
                    pitchSensitivity += dS;
                    if (pitchSensitivity < 0.01f) {
                        pitchSensitivity = 0.01f;
                    } else if (pitchSensitivity > 0.2f) {
                        pitchSensitivity = 0.2f;
                    }
                    if (yawSensitivity < 0.01f) {
                        yawSensitivity = 0.01f;
                    } else if (yawSensitivity > 0.2f) {
                        yawSensitivity = 0.2f;
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
                    pitchSensitivity = yawSensitivity;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                    yawSensitivity = pitchSensitivity;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
                    yawSensitivity = 0.09f;
                    pitchSensitivity = 0.09f;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
                    moveSpeed += (float)Mouse.getDWheel()/2400;
                    if (moveSpeed < 0.05f) {
                        moveSpeed = 0.05f;
                    } else if (moveSpeed > 0.60f) {
                        moveSpeed = 0.60f;
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_N)) {
                    moveSpeed = 0.35f;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    camera.ahead(moveSpeed);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    camera.astern(moveSpeed);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    camera.aport(moveSpeed);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    camera.astarboard(moveSpeed);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !grav) {
                    camera.ascend(moveSpeed);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    camera.descend(moveSpeed);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_L) && Keyboard.isKeyDown(Keyboard.KEY_COMMA)) {
                    locked = false;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_L) && Keyboard.isKeyDown(Keyboard.KEY_PERIOD)) {
                    locked = true;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_Q) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                    glEnable(GL_BLEND);
                } else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                    glDisable(GL_BLEND);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_0)) {
                    glClearColor(0.5f, 0.6f, 1.0f, 0.0f);
                    for (int i = 0; i < chunks.length; i++) {
                        chunks[i].changeType(0);
                        chunks[i].rebuildMesh(chunks[i].genX, chunks[i].genY, chunks[i].genZ);
                    }
                } else if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                    glClearColor(0.4f, 0.1f, 0.15f, 0.0f);
                    for (int i = 0; i < chunks.length; i++) {
                        chunks[i].changeType(1);
                        chunks[i].rebuildMesh(chunks[i].genX, chunks[i].genY, chunks[i].genZ);
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_G) && Keyboard.isKeyDown(Keyboard.KEY_COMMA)) {
                    currTime = (System.currentTimeMillis());
                    initVel = prevYPos - camera.pos.y;
                    grav = true;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_G) && Keyboard.isKeyDown(Keyboard.KEY_PERIOD)) {
                    grav = false;
                }
                if (grav) {
                    double falling = initVel + 4.9d * (System.currentTimeMillis() - currTime) * (System.currentTimeMillis() - currTime) / 250000;
                    camera.descend((float)falling);
                    if (locked) {
                        if (camera.pos.y > -30) {
                            camera.pos.y = -30;
                        } else if (camera.pos.y < -58) {
                            camera.pos.y = -58;
                        }
                    }
                    if (prevYPos == camera.pos.y) {
                        initVel = 0;
                        currTime = System.currentTimeMillis();
                        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                            camera.ascend(0.01f);
                            initVel = -1;
                        }
                    }
                }
                if (locked) {
                    if (camera.pos.x > 1) {
                        camera.pos.x = 1;
                    } else if (camera.pos.x < (numChunks) * -60 + 1) {
                        camera.pos.x = (numChunks) * -60 + 1;
                    }
                    if (camera.pos.y > -30) {
                        camera.pos.y = -30;
                    } else if (camera.pos.y < -58) {
                        camera.pos.y = -58;
                    }
                    if (camera.pos.z > 2) {
                        camera.pos.z = 2;
                    } else if (camera.pos.z < (numChunks) * -60 + 2) {
                        camera.pos.z = (numChunks) * -60 + 2;
                    }
                }
                glLoadIdentity();
                camera.gaze();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                for (int i = 0; i < chunks.length; i++) {
                    chunks[i].render();
                }
                prevYPos = camera.pos.y;
                Display.update();
                Display.sync(60);
            }
            Display.destroy();
        }
    }
