/***************************************************************
* file: Block.java
* authors: Jeremy Canning, Dylan Chung, Camron Fortenbery, Grant Posner
* class: CS 4450: Computer Graphics
*
* assignment: Final Project
* date last modified: 3/27/2019
*
* purpose: Block object. Contains enumerated type for block types.
*
****************************************************************/ 
package graphically_inclined_checkpoint_3;

public class Block {
    private boolean isActive;
    private BlockType type;
    private float x, y, z;
    
    public enum BlockType {
        BlockType_Grass(0),
        BlockType_Sand(1),
        BlockType_Water(2),
        BlockType_Dirt(3),
        BlockType_Stone(4),
        BlockType_Bedrock(5);
        private int blockID;
        BlockType(int i) {
            blockID = i;
        }
        public int getID() {
            return blockID;
        }
        public void setID(int i) {
            blockID = i;
        }
    }
    
    public Block(BlockType t) {
        type = t;
    }
    
    // method: setCoords
    // purpose: Sets the block's x, y, and z coordinates.
    public void setCoords(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // method: isActive
    // purpose: Checks to see if the block is active.
    public boolean isActive() {
        return isActive;
    }
    
    // method: setActive
    // purpose: Activates or deactivates the block.
    public void setActive(boolean active) {
        isActive = active;
    }
    
    // method: getID
    // purpose: Gets the block's type by returning its type ID.
    public int getID() {
        return type.getID();
    }
}
