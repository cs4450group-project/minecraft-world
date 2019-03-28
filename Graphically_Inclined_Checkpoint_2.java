/***************************************************************
* file: Graphically_Inclined_Checkpoint_2.java
* authors: Jeremy Canning, Dylan Chung, Camron Fortenbery, Grant Posner
* class: CS 4450: Computer Graphics
*
* assignment: Final Project
* date last modified: 3/27/2019
*
* purpose: This file contains the necessary code for window creation and 
* display mode setting. Also contains the main method.
*
****************************************************************/ 
package graphically_inclined_checkpoint_2;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;

public class Graphically_Inclined_Checkpoint_2 {
    private FPCameraController cam;
    private DisplayMode dMode;
    
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
        Display.setTitle("Graphically_Inclined_Checkpoint_2");
        Display.create();
    }
    
    // method: initGL
    // purpose: Colors window black and initializes coordinate system.
    // Enables texture transparency.
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
//        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }
    
    // method: start
    // purpose: Calls for window creation and initialization.
    public void start() {
        try {
            createWindow();
            initGL();
            cam = new FPCameraController(-29.0f, -26.0f, -28.0f);
            cam.gameLoop();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // method: main
    // purpose: Creates new instance of Graphically_Inclined_Checkpoint_1 and
    // calls start() method on it.
    public static void main(String[] args) {
        Graphically_Inclined_Checkpoint_2 entity  = new Graphically_Inclined_Checkpoint_2();
        entity.start();
    }
}
