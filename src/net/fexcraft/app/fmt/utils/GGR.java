package net.fexcraft.app.fmt.utils;

import java.time.Instant;
import java.util.TreeMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.ui.editor.Editor;
import net.fexcraft.lib.common.math.Vec3f;

/** CCR */
public class GGR {
	
    public float movespeed = 1.5f;//0.5f;
    public float maxlookrange = 85;
    public float sensivity = 1.0f;//= 0.05f;
    public Vec3f pos, rotation;
    private TreeMap<Integer, Long> keycooldown = new TreeMap<Integer, Long>();
    
    public GGR(int x, int y, int z){
        pos = new Vec3f(x, y, z);
        rotation = new Vec3f(0, 0, 0);
    }
    
    public GGR(float x, float y, float z){
        pos = new Vec3f(x, y, z);
        rotation = new Vec3f(0, 0, 0);
    }

    public void apply(){
        if(rotation.yCoord / 360 > 1){ rotation.yCoord -= 360; }
        else if(rotation.yCoord / 360 < -1){ rotation.yCoord += 360; }
        GL11.glLoadIdentity();
        GL11.glRotatef(rotation.xCoord, 1, 0, 0);
        GL11.glRotatef(rotation.yCoord, 0, 1, 0);
        GL11.glRotatef(rotation.zCoord, 0, 0, 1);
        GL11.glTranslatef(-pos.xCoord, -pos.yCoord, -pos.zCoord);
    }

    public void acceptInput(float delta){
        acceptInputRotate(delta);
        acceptInputGrab();
        acceptInputMove(delta);
    }
    
    private boolean clickedL, clickedR, panning;
    private int wheel, oldMouseX=-1,oldMouseY=-1;

    public void acceptInputRotate(float delta){
        if(clickedR && ! Mouse.isButtonDown(1)){
            Mouse.setGrabbed(false);//fix mouse grab sticking
        }
        if(Mouse.isGrabbed()){
            rotation.yCoord += Mouse.getDX() * sensivity * delta;
            rotation.xCoord += -Mouse.getDY() * sensivity * delta;
            rotation.xCoord = Math.max(-maxlookrange, Math.min(maxlookrange, rotation.xCoord));
        }
        else{
        	if(!Mouse.isInsideWindow()) return;
        	if(Mouse.isButtonDown(0) && !clickedL) FMTB.get().UI.onButtonPress(0); clickedL = Mouse.isButtonDown(0);
        	if(Mouse.isButtonDown(1) && !clickedR) FMTB.get().UI.onButtonPress(1); clickedR = Mouse.isButtonDown(1);
        	if((wheel = Mouse.getDWheel()) != 0){
        		if(!FMTB.get().UI.onScrollWheel(wheel)){
                    double[] zoom = rotatePoint(wheel * 0.005f, rotation.xCoord, rotation.yCoord - 90);
                    pos.xCoord += zoom[0]; pos.yCoord += zoom[1]; pos.zCoord += zoom[2];
        		}
        	}
        }
    }

    public static double[] rotatePoint(double f, float pitch, float yaw) {
        double[] xyz = new double[]{f,0,0};
            pitch *= 0.01745329251;
            xyz[1] = -(f * Math.sin(pitch));
            //
            yaw *= 0.01745329251;
            xyz[0] = (f * Math.cos(yaw));
            xyz[2] = (f * Math.sin(yaw));
        return xyz;
    }

    public void acceptInputGrab(){
        if((Mouse.isInsideWindow() && Keyboard.isKeyDown(Keyboard.KEY_E) || Mouse.isButtonDown(1))){
            Mouse.setGrabbed(true);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
            Mouse.setGrabbed(false); FMTB.getDialogBox().reset();
        }
        //
        if(isKeyDown(Keyboard.KEY_F1, 200)){ /*//TODO help UI*/ }
        if(isKeyDown(Keyboard.KEY_F2, 200)){ Settings.toggleFloor(); }
        if(isKeyDown(Keyboard.KEY_F3, 200)){ Settings.toggleLines(); }
        if(isKeyDown(Keyboard.KEY_F4, 200)){ Settings.toggleCube(); }
        if(isKeyDown(Keyboard.KEY_F5, 200)){ Settings.toggleDemo(); }
        if(isKeyDown(Keyboard.KEY_F6, 200)){ Settings.togglePolygoMarker(); }
        //
        if(isKeyDown(Keyboard.KEY_1)){ Editor.toggle("general_editor", false); }
        if(isKeyDown(Keyboard.KEY_2)){ Editor.toggle("shapebox_editor", false); }
        if(isKeyDown(Keyboard.KEY_3)){ Editor.toggle("cylinder_editor", false); }
        if(isKeyDown(Keyboard.KEY_4)){ Editor.toggle("group_editor", false); }
        //TODO other editors
    }
    
	private boolean isKeyDown(int key){
		return isKeyDown(key, 100);
	}

	public boolean isKeyDown(int key, int am){
    	long i = keycooldown.containsKey(key) ? keycooldown.get(key) : 0;
    	if(i - Instant.now().toEpochMilli() >= 0) return false;
    	else{
    		keycooldown.put(key, Instant.now().toEpochMilli() + am);
    		return Keyboard.isKeyDown(key);
    	}
    }

    public void acceptInputMove(float delta){
        if(Mouse.isButtonDown(2)){
            if(oldMouseX == -1){
                oldMouseX = Mouse.getX();
                oldMouseY = Mouse.getY();
            }
            //offset it because if we do it every frame it doesn't always work, but every few frames is fine.
            pos.xCoord += (Mouse.getX() - oldMouseX) * 0.001;
            pos.yCoord += (Mouse.getY() - oldMouseY) * 0.001;
            panning = true;
        }
        else if(panning){
            oldMouseX =- 1; panning = false;
        }
        //
    	if(!Mouse.isGrabbed()) return;
        boolean front = Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean back = Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean right = Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean left = Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean speedp = Keyboard.isKeyDown(Keyboard.KEY_R);
        boolean speedm = Keyboard.isKeyDown(Keyboard.KEY_F);
        boolean up = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        boolean down = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        float nspeed;
        if(speedp) nspeed = movespeed * 5;
        else if(speedm) nspeed = movespeed / 2;
        else nspeed = movespeed;
        nspeed *= delta;
        if(up) pos.yCoord += nspeed;
        if(down) pos.yCoord -= nspeed;
        if(back){
            pos.xCoord -= Math.sin(Math.toRadians(rotation.yCoord)) * nspeed;
            pos.zCoord += Math.cos(Math.toRadians(rotation.yCoord)) * nspeed;
        }
        if(front){
            pos.xCoord += Math.sin(Math.toRadians(rotation.yCoord)) * nspeed;
            pos.zCoord -= Math.cos(Math.toRadians(rotation.yCoord)) * nspeed;
        }
        if(left){
            pos.xCoord += Math.sin(Math.toRadians(rotation.yCoord - 90)) * nspeed;
            pos.zCoord -= Math.cos(Math.toRadians(rotation.yCoord - 90)) * nspeed;
        }
        if(right){
            pos.xCoord += Math.sin(Math.toRadians(rotation.yCoord + 90)) * nspeed;
            pos.zCoord -= Math.cos(Math.toRadians(rotation.yCoord + 90)) * nspeed;
        }
    }
    
}