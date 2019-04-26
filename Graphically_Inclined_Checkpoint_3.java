/***************************************************************
* file: Graphically_Inclined_Checkpoint_3.java
* authors: Jeremy Canning, Dylan Chung, Camron Fortenbery, Grant Posner
* class: CS 4450: Computer Graphics
*
* assignment: Final Project
* date last modified: 4/22/2019
*
* purpose: This file contains the necessary code for window creation and 
* display mode setting. Also contains the main method.
*
****************************************************************/ 
package graphically_inclined_checkpoint_3;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

public class Graphically_Inclined_Checkpoint_3 {
    private FPCameraController cam;
    private DisplayMode dMode;
    static final int NUM_CHUNKS = 1;
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;
    private FloatBuffer darkLight;
    
    // method: createWindow
    // purpose: Creates a window of size 640x480 and titles it "Graphically_Inclined_Checkpoint_2".
    private void createWindow() throws Exception {
        Display.setFullscreen(false);
        DisplayMode d[] = Display.getAvailableDisplayModes();
        for (int i = 0; i < d.length; i++) {
            if (d[i].getWidth() == 640 && d[i].getHeight() == 480 && d[i].getBitsPerPixel() == 32) {
                dMode = d[i];
                break;
            }
        }
        Display.setDisplayMode(dMode);
        Display.setTitle("Graphically_Inclined_Checkpoint_3");
        Display.create();
    }
    
    // method: initGL
    // purpose: Colors window black and initializes coordinate system. Specifies
    // lighting material color details. Enables texture transparency. Note that 
    // lightPosition is also used in place of whiteLight at the ambient lighting
    // setting; this is to allow realistic front and back ambient and diffuse
    // lighting. (Basically, the grass stopped being green unless I added the
    // extra code, so in it goes, I suppose!)
    private void initGL() {
        glClearColor(0.5f, 0.6f, 1.0f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, (float)dMode.getWidth()/(float)dMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_CULL_FACE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        initLightArrays();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);
        glLight(GL_LIGHT0, GL_AMBIENT, darkLight);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        glEnable(GL_COLOR_MATERIAL);
    }
    
    // method: initLightArrays
    // purpose: Initializes the arrays used in lighting specification.
    private void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(0).put(0).put(0).put(1.0f).flip();
        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
        darkLight = BufferUtils.createFloatBuffer(4);
        darkLight.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
    }
    
    // method: start
    // purpose: Calls for window creation and initialization.
    public void start() {
        try {
            createWindow();
            initGL();
            cam = new FPCameraController(-30f * NUM_CHUNKS + 1f, -26.0f, -30f * NUM_CHUNKS + 2f, NUM_CHUNKS);
            cam.gameLoop();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // method: main
    // purpose: Creates new instance of Graphically_Inclined_Checkpoint_1 and
    // calls start() method on it.
    public static void main(String[] args) {
        Graphically_Inclined_Checkpoint_3 entity  = new Graphically_Inclined_Checkpoint_3();
        entity.start();
    }
}
