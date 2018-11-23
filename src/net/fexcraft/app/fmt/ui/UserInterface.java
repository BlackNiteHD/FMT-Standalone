package net.fexcraft.app.fmt.ui;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.FMTGLProcess;
import net.fexcraft.app.fmt.ui.generic.DialogBox;
import net.fexcraft.app.fmt.ui.generic.FileChooser;
import net.fexcraft.app.fmt.ui.generic.Menulist;
import net.fexcraft.app.fmt.ui.generic.TextField;
import net.fexcraft.app.fmt.utils.RayCoastAway;
import net.fexcraft.app.fmt.utils.SessionHandler;
import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.math.Time;

public class UserInterface {

	public static DialogBox DIALOGBOX;
	public static FileChooser FILECHOOSER;
	//
	private HashMap<String, Element> elements = new HashMap<>();
	private FMTGLProcess root;

	public UserInterface(FMTGLProcess main){
		this.root = main; root.setupUI(this);
	}
	
	private int width, height;

	public void render(boolean bool){
		width = root.getDisplayMode().getWidth(); height = root.getDisplayMode().getHeight();
		{
			GL11.glPushMatrix();
	        GL11.glMatrixMode(GL11.GL_PROJECTION);
	        GL11.glPushMatrix();
	        GL11.glLoadIdentity();
	        GL11.glOrtho(0, width, height, 0, -100, 100);
	        GL11.glMatrixMode(GL11.GL_MODELVIEW);
	        GL11.glPushMatrix();
	        GL11.glLoadIdentity();
		}
		//
		GL11.glLoadIdentity();
		if(bool){
			tmelm.render(width, height); logintxt.render(width, height);
		}
		else{
			elements.values().forEach(elm -> elm.render(width, height));
		}
		//
		{
	        GL11.glMatrixMode(GL11.GL_PROJECTION);
	        GL11.glPopMatrix();
	        GL11.glMatrixMode(GL11.GL_MODELVIEW);
	        GL11.glPopMatrix();
	        GL11.glDepthFunc(GL11.GL_LEQUAL);
	        GL11.glClearColor(0.5f, 0.5f, 0.5f, 0.2f);
	        GL11.glClearDepth(1.0);
	        GL11.glPopMatrix();
		}
	}
	
	private Element tmelm = new TextField(null, "text", 4, 4, 500){
		@Override
		public void renderSelf(int rw, int rh){
			this.y = rh - root.getDisplayMode().getHeight() + 4;
			this.setText((Time.getDay() % 2 == 0 ? "FMT - Fexcraft Modelling Toolbox" : "FMT - Fex's Modelling Toolbox") + (Static.dev() ? " [Developement Version]" : " [Standard Version]"), false);
			super.renderSelf(rw, rh);
		}
	};
	private Element logintxt = new TextField(null, "text", 4, 4, 500){
		@Override
		public void renderSelf(int rw, int rh){
			this.y = rh - root.getDisplayMode().getHeight() + 32;
			switch(FMTB.MODEL.creators.size()){
				case 0: {
					this.setText(FMTB.MODEL.name + " - " + (SessionHandler.isLoggedIn() ? SessionHandler.getUserName() : "Guest User"), false);
					break;
				}
				case 1: {
					if(FMTB.MODEL.creators.get(0).equals(SessionHandler.getUserName())){
						this.setText(FMTB.MODEL.name + " - by " + SessionHandler.getUserName(), false);
					}
					else{
						this.setText(FMTB.MODEL.name + " - by " + String.format("%s (logged:%s)", FMTB.MODEL.creators.get(0), SessionHandler.getUserName()), false);
					}
					break;
				}
				default: {
					if(FMTB.MODEL.creators.contains(SessionHandler.getUserName())){
						this.setText(FMTB.MODEL.name + " - by " + SessionHandler.getUserName() + " (and " + (FMTB.MODEL.creators.size() - 1) + " others)", false);
					}
					else{
						this.setText(FMTB.MODEL.name + " - " + String.format("(logged:%s)", SessionHandler.getUserName()), false);
					}
					break;
				}
			}
			super.renderSelf(rw, rh);
		}
	};

	public boolean isAnyHovered(){
		return elements.values().stream().filter(pre -> pre.anyHovered()).count() > 0;
	}

	public void onButtonPress(int i){
		if(Menulist.anyMenuHovered()){
			for(Menulist list : Menulist.arrlist){
				if(list.hovered && list.onButtonClick(Mouse.getX(), root.getDisplayMode().getHeight() - Mouse.getY(), i == 0, true)) return;
			}
		}
		else{
			Element eelm = null;
			for(Element elm : elements.values()){
				if(elm.visible && elm.enabled /*&& elm.hovered*/){
					if(elm.onButtonClick(Mouse.getX(), root.getDisplayMode().getHeight() - Mouse.getY(), i == 0, elm.hovered)){
						return;
					} else eelm = elm;
				}
			}
			if(i == 0 && (eelm == null ? true : eelm.id.equals("toolbar"))){//TODO mostly obsolete check, but /shrug
				RayCoastAway.doTest(true, true);
			}
		}
	}

	public boolean onScrollWheel(int wheel){
		for(Element elm : elements.values()){
			if(elm.visible && elm.enabled){
				if(elm.onScrollWheel(wheel)) return true;
			}
		} return false;
	}

	public Element getElement(String string){
		return elements.get(string);
	}

	public boolean hasElement(String string){
		return elements.containsKey(string);
	}
	
	public Map<String, Element> getElements(){
		return this.elements;
	}
	
}