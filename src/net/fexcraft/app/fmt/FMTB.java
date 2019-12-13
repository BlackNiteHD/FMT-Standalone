package net.fexcraft.app.fmt;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Timer;

import javax.script.ScriptException;

import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.fexcraft.app.fmt.demo.ModelT1P;
import net.fexcraft.app.fmt.porters.PorterManager;
import net.fexcraft.app.fmt.ui.Dialog;
import net.fexcraft.app.fmt.ui.FontRenderer;
import net.fexcraft.app.fmt.ui.UserInterface;
import net.fexcraft.app.fmt.ui.editor.Editor;
import net.fexcraft.app.fmt.ui.editor.GeneralEditor;
import net.fexcraft.app.fmt.ui.editor.ModelGroupEditor;
import net.fexcraft.app.fmt.ui.editor.PreviewEditor;
import net.fexcraft.app.fmt.ui.editor.TextureEditor;
import net.fexcraft.app.fmt.ui.general.AltMenu;
import net.fexcraft.app.fmt.ui.general.Bottombar;
import net.fexcraft.app.fmt.ui.general.ControlsAdjuster;
import net.fexcraft.app.fmt.ui.general.Crossbar;
import net.fexcraft.app.fmt.ui.general.DialogBox;
import net.fexcraft.app.fmt.ui.general.DropDown;
import net.fexcraft.app.fmt.ui.general.FileSelector;
import net.fexcraft.app.fmt.ui.general.SettingsBox;
import net.fexcraft.app.fmt.ui.general.TextField;
import net.fexcraft.app.fmt.ui.general.Toolbar;
import net.fexcraft.app.fmt.ui.tree.HelperTree;
import net.fexcraft.app.fmt.ui.tree.ModelTree;
import net.fexcraft.app.fmt.utils.Backups;
import net.fexcraft.app.fmt.utils.DiscordUtil;
import net.fexcraft.app.fmt.utils.GGR;
import net.fexcraft.app.fmt.utils.HelperCollector;
import net.fexcraft.app.fmt.utils.ImageHelper;
import net.fexcraft.app.fmt.utils.KeyCompound;
import net.fexcraft.app.fmt.utils.RayCoastAway;
import net.fexcraft.app.fmt.utils.SaveLoad;
import net.fexcraft.app.fmt.utils.SessionHandler;
import net.fexcraft.app.fmt.utils.Settings;
import net.fexcraft.app.fmt.utils.StyleSheet;
import net.fexcraft.app.fmt.utils.TextureManager;
import net.fexcraft.app.fmt.utils.TextureUpdate;
import net.fexcraft.app.fmt.utils.Translator;
import net.fexcraft.app.fmt.wrappers.GroupCompound;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.HttpUtil;
import net.fexcraft.lib.common.utils.Print;
import net.fexcraft.lib.tmt.ModelRendererTurbo;

/**
 * @author Ferdinand Calo' (FEX___96)
 * 
 * All rights reserved &copy; 2019 fexcraft.net
 * */
public class FMTB {
	
	public static final String deftitle = "[FPS:%s] Fexcraft Modelling Toolbox - %s";
	public static final String deftitle0 = "Fexcraft Modelling Toolbox - %s";
	public static final String version = "1.3.3";
	public static final String CLID = "587016218196574209";
	//
	private static String title = "Unnamed Model";
	private boolean close;
	public static GGR ggr;
	//public int width, height;
	private static FMTB INSTANCE;
	private DisplayMode displaymode;
	public UserInterface UI;
	private static File lwjgl_natives;
	public static GroupCompound MODEL = new GroupCompound(null);
	public static Timer BACKUP_TIMER, TEX_UPDATE_TIMER;
	//public static boolean GAMETEST;
	private long lf, lfps, fps;
	private static int disk_update;
	
	public static void main(String... args) throws Exception {
	    switch(LWJGLUtil.getPlatform()){
	        case LWJGLUtil.PLATFORM_WINDOWS:{ lwjgl_natives = new File("./libs/native/windows"); break; }
	        case LWJGLUtil.PLATFORM_LINUX:{ lwjgl_natives = new File("./libs/native/linux"); break; }
	        case LWJGLUtil.PLATFORM_MACOSX:{ lwjgl_natives = new File("./libs/native/macosx"); break; }
	    }
	    System.setProperty("org.lwjgl.librarypath", lwjgl_natives.getAbsolutePath());
	    //
		FMTB.INSTANCE = new FMTB(); try{ INSTANCE.run(); } catch(Throwable thr){ thr.printStackTrace(); System.exit(1); }
	}
	
	public static final Process startProcess(Class<?> clazz) throws Exception {
	    String sp = System.getProperty("file.separator"), path = System.getProperty("java.home") + sp + "bin" + sp + "java";
	    return new ProcessBuilder(path, "-cp", System.getProperty("java.class.path"), clazz.getName()).start();
	}
	
	public static final int getResult(String[] text, int buttons) throws Exception {
	    String sp = System.getProperty("file.separator"), path = System.getProperty("java.home") + sp + "bin" + sp + "java";
	    return new ProcessBuilder(path, "-cp", System.getProperty("java.class.path"), Object.class.getName()).start().waitFor();
	    //TODO make new "dialogbox class" using this?
	}

	public static final FMTB get(){ return INSTANCE; }
	
	public FMTB setTitle(String string){ title = string; DiscordUtil.update(false); return this; }
	
	public void run() throws LWJGLException, InterruptedException, IOException, NoSuchMethodException, ScriptException {
		TextureManager.init(); this.setIcon(); Settings.load(); StyleSheet.load(); Translator.init(); 
		setupDisplay(); initOpenGL(); ggr = new GGR(this, 0, 4, 4); ggr.rotation.xCoord = 45; FontRenderer.init();
		PorterManager.load(); HelperCollector.reload(); Display.setResizable(true); UI = new UserInterface(this); this.setupUI(UI);
		SessionHandler.checkIfLoggedIn(true, true); checkForUpdates(); KeyCompound.init(); KeyCompound.load();
		//
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli(); long date = Time.getDate(); while((mid += Time.MIN_MS * 5) < date);
		if(BACKUP_TIMER == null){ (BACKUP_TIMER = new Timer()).schedule(new Backups(), new Date(mid), Time.MIN_MS * 5); }
		if(TEX_UPDATE_TIMER == null){ (TEX_UPDATE_TIMER = new Timer()).schedule(new TextureUpdate(), Time.SEC_MS, Time.SEC_MS / 2); }
		//
		if(Settings.discordrpc()){
			DiscordEventHandlers.Builder handler = new DiscordEventHandlers.Builder();
			handler.setReadyEventHandler(new DiscordUtil.ReadyEventHandler());
			handler.setErroredEventHandler(new DiscordUtil.ErroredEventHandler());
			handler.setDisconnectedEventHandler(new DiscordUtil.DisconectedEventHandler());
			handler.setJoinGameEventHandler(new DiscordUtil.JoinGameEventHandler());
			handler.setJoinRequestEventHandler(new DiscordUtil.JoinRequestEventHandler());
			handler.setSpectateGameEventHandler(new DiscordUtil.SpectateGameEventHandler());
			DiscordRPC.discordInitialize(CLID, handler.build(), true);
			DiscordRPC.discordRunCallbacks(); DiscordUtil.update(true);
			Runtime.getRuntime().addShutdownHook(new Thread(){ @Override public void run(){ DiscordRPC.discordShutdown(); } });
		}
		//
		this.getDelta(); lfps = this.getTime();
		while(!close){
			loop(this.getDelta()); render();
			if(!RayCoastAway.PICKING){
				if(ImageHelper.HASTASK){
					UI.render(true); ImageHelper.doTask();
				}
				else UI.render(false);
				//
			}
			Display.update(); Display.sync(60);
			if(Settings.discordrpc()) if(++disk_update > 60000){ DiscordRPC.discordRunCallbacks(); disk_update = 0; }
			//Thread.sleep(50);
		} DiscordRPC.discordShutdown();
		Display.destroy(); Settings.save(); StyleSheet.save(); KeyCompound.save(); SessionHandler.save(); System.exit(0);
	}

	private void setIcon(){
		Display.setIcon(new java.nio.ByteBuffer[]{ TextureManager.getTexture("icon", false).getBuffer(), TextureManager.getTexture("icon", false).getBuffer() });
	}

	private void loop(long delta){
		ggr.pollInput(0.1f); ggr.apply();
		//
		if(Display.isCloseRequested()){ SaveLoad.checkIfShouldSave(true, false); }
		//
		if(Display.wasResized()){
			displaymode = new DisplayMode(Display.getWidth(), Display.getHeight());
	        GLU.gluPerspective(45.0f, displaymode.getWidth() / displaymode.getHeight(), 0.1f, 4096f / 2);
			GL11.glViewport(0, 0, displaymode.getWidth(), displaymode.getHeight());
			this.initOpenGL(); UI.rescale();
		}
        if(!Display.isVisible()) {
            try{ Thread.sleep(100); }
            catch(Exception e){ e.printStackTrace(); }
        }
        if(!TextureUpdate.HALT){ TextureUpdate.tryAutoPos(TextureUpdate.ALL); }
        //
        this.updateFPS();
	}
	
	public long getTime(){
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	public long getDelta(){
	    long time = getTime();
	    long delta = time - lf;
	    lf = time; return delta;
	}
	
    public void updateFPS() {
        if(getTime() - lfps > 1000){
        	if(Settings.bottombar()){
        		Bottombar.fps = fps; Display.setTitle(String.format(deftitle0, title)); 
        	}
        	else{
        		Display.setTitle(String.format(deftitle, fps, title));
        	}
        	fps = 0; lfps += 1000;
        } fps++;
    }
	
	private void render(){
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glLoadIdentity(); //GL11.glLoadIdentity();		
        GL11.glRotatef(ggr.rotation.xCoord, 1, 0, 0);
        GL11.glRotatef(ggr.rotation.yCoord, 0, 1, 0);
        GL11.glRotatef(ggr.rotation.zCoord, 0, 0, 1);
        GL11.glTranslatef(-ggr.pos.xCoord, -ggr.pos.yCoord, -ggr.pos.zCoord);
        //if(GAMETEST){ GameHandler.render(); return; }
        GL11.glRotatef(180, 1, 0, 0);
        GL11.glPushMatrix();
        RGB.WHITE.glColorApply();
        if(ImageHelper.HASTASK && ImageHelper.getTaskId() == 2){
        	GL11.glRotatef((ImageHelper.getStage() - 20) * 10, 0, 1, 0);
        }
        //
        if(RayCoastAway.PICKING){ 
            MODEL.render(); GL11.glPopMatrix(); render();
        }
        else{
	        if(Settings.floor()){
	            TextureManager.bindTexture("floor");
	            GL11.glRotatef(-90, 0, 1, 0);
	            GL11.glPushMatrix();
	            //GL11.glCullFace(GL11.GL_BACK); GL11.glEnable(GL11.GL_CULL_FACE);
	            GL11.glBegin(GL11.GL_QUADS); float cs = 16;
	    		GL11.glTexCoord2f(0, 1); GL11.glVertex3f( cs, 1f / 16 * 10,  cs);
	            GL11.glTexCoord2f(1, 1); GL11.glVertex3f(-cs, 1f / 16 * 10,  cs);
	            GL11.glTexCoord2f(1, 0); GL11.glVertex3f(-cs, 1f / 16 * 10, -cs);
	            GL11.glTexCoord2f(0, 0); GL11.glVertex3f( cs, 1f / 16 * 10, -cs);
	            //GL11.glCullFace(GL11.GL_FRONT_AND_BACK); GL11.glDisable(GL11.GL_CULL_FACE); //apparently the front renderer doesn't like this.
	            GL11.glEnd(); GL11.glPopMatrix();
	            GL11.glRotatef( 90, 0, 1, 0);
	        }
			if(Settings.lighting()) GL11.glEnable(GL11.GL_LIGHTING);
            if(Settings.cube()){
                TextureManager.bindTexture("demo"); compound0.render();
            }
            if(Settings.cullface()) GL11.glEnable(GL11.GL_CULL_FACE);
            MODEL.render();
            if(Settings.cullface()) GL11.glDisable(GL11.GL_CULL_FACE);
            if(HelperCollector.LOADED.size() > 0){
            	for(GroupCompound model : HelperCollector.LOADED) model.render();
            }
            if(Settings.demo()){
                TextureManager.bindTexture("t1p"); ModelT1P.INSTANCE.render();
            }
			if(Settings.lighting()) GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
	}
	
	private static final ModelRendererTurbo compound0 = new ModelRendererTurbo(null, 0, 0);
	static { compound0.textureHeight = compound0.textureWidth = 16; compound0.addBox(-8, 0, -8, 16, 16, 16); }

	private void initOpenGL(){
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glLightModeli(GL11.GL_LIGHT_MODEL_TWO_SIDE,GL11.GL_TRUE);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK,GL11.GL_AMBIENT_AND_DIFFUSE);
        this.setLightPos(Settings.getLight0Position());
        if(!Settings.lighting()) GL11.glDisable(GL11.GL_LIGHTING);
        //
		GL11.glEnable(GL11.GL_TEXTURE_2D);
    	GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(0.5f, 0.5f, 0.5f, 0.2f);
    	//float[] clearcolor = Settings.background_color.toFloatArray(); //applied in UserInterface.class
    	//GL11.glClearColor(clearcolor[0], clearcolor[1], clearcolor[2], Settings.background_color.alpha);
        GL11.glClearDepth(1.0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(45.0f, (float)displaymode.getWidth() / (float)displaymode.getHeight(), 0.1f, 4096f / 2);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        //
        GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void setLightPos(float[] position){
		java.nio.FloatBuffer fb = org.lwjgl.BufferUtils.createFloatBuffer(4); fb.put(position); fb.flip();
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, fb);
	}

	private void setupDisplay() throws LWJGLException {
		Display.setFullscreen(false); Display.setResizable(false);
		Display.setDisplayMode(displaymode = new DisplayMode(1280, 720));
		Display.setTitle("Fexcraft Modelling Toolbox"); Display.setVSyncEnabled(true);
		Display.create();
	}
	
	/** use SaveLoad.checkIfShouldSave(true) first! */
	public void close(boolean bool){
		close = bool;
	}
	
	public static void showDialogbox(String title, String button0, String button1, Runnable run0, Runnable run1){
		UserInterface.DIALOGBOX.show(title, button0, button1, run0, run1);
	}
	
	public static void showDialogbox(String title, String button0, String button1, Runnable run0, Runnable run1, int progress, RGB color){
		UserInterface.DIALOGBOX.show(title, button0, button1, run0, run1);
		UserInterface.DIALOGBOX.progress = progress; UserInterface.DIALOGBOX.progresscolor = color;
	}

	public DisplayMode getDisplayMode(){
		return displaymode;
	}

	public void setupUI(UserInterface ui){
		TextureManager.loadTexture("icons/pencil", null);
		TextureManager.loadTexture("icons/arrow_increase", null);
		TextureManager.loadTexture("icons/arrow_decrease", null);
		TextureManager.loadTexture("icons/group_delete", null);
		TextureManager.loadTexture("icons/group_visible", null);
		TextureManager.loadTexture("icons/group_edit", null);
		TextureManager.loadTexture("icons/group_minimize", null);
		TextureManager.loadTexture("icons/group_clone", null);
		TextureManager.loadTexture("icons/editors/minimized", null);
		TextureManager.loadTexture("icons/editors/expanded", null);
		//
		(UserInterface.TOOLBAR = new Toolbar()).repos();
		(UserInterface.BOTTOMBAR = new Bottombar()).setVisible(Settings.bottombar());
		ui.getElements().add(ModelTree.TREE);
		ui.getElements().add(HelperTree.TREE);
		ui.getElements().add(new GeneralEditor());
		ui.getElements().add(new ModelGroupEditor());
		ui.getElements().add(new TextureEditor());
		ui.getElements().add(new PreviewEditor());
		//
		ui.getElements().add(UserInterface.DIALOGBOX = new DialogBox());
		ui.getElements().add(UserInterface.SETTINGSBOX = new SettingsBox());
		ui.getElements().add(UserInterface.FILECHOOSER = new FileSelector());
		ui.getElements().add(UserInterface.CONTROLS = new ControlsAdjuster());
		//render last
		ui.getElements().add(UserInterface.TOOLBAR);
		ui.getElements().add(UserInterface.BOTTOMBAR);
		ui.getElements().add(new Crossbar());
		ui.getElements().add(UserInterface.RIGHTMENU = AltMenu.MENU);
		ui.getElements().add(UserInterface.DROPDOWN = DropDown.INST);
		FMTB.MODEL.updateFields();
	}

	public void reset(boolean esc){
		if(Dialog.anyVisible() || TextField.anySelected()){
			UserInterface.DIALOGBOX.reset(); UserInterface.FILECHOOSER.reset();
			UserInterface.CONTROLS.reset(); UserInterface.SETTINGSBOX.reset();
			TextField.deselectAll();
		} else if(esc && Editor.anyVisible()){ Editor.hideAll(); } else return;//open some kind of main menu / status / login screen.
	}

	private void checkForUpdates(){
		new Thread(){
			@Override
			public void run(){
				JsonObject obj = HttpUtil.request("http://fexcraft.net/minecraft/fcl/request", "mode=requestdata&modid=fmt");
				if(obj == null || !obj.has("latest_version")){
					Print.console("Couldn't fetch latest version.");
					Print.console(obj == null ? ">> no version response received" : obj.toString());
					return;
				}
				if(obj.has("blocked_versions")){
					JsonArray array = obj.get("blocked_versions").getAsJsonArray();
					for(JsonElement elm : array){
						if(elm.isJsonPrimitive() && elm.getAsString().equals(version)){
							Print.console("Blocked version detected, causing panic.");
							System.exit(2); System.exit(2); System.exit(2); System.exit(2);
						}
					}
				}
				String newver = obj.get("latest_version").getAsString(); boolean bool = version.equals(newver);
				String welcome = Translator.format("dialog.greeting.welcome", "Welcome to FMT!<nl><version:%s>", version);
				String newversion = Translator.format("dialog.greeting.newversion", "New version available!<nl>%s >> %s", newver, version);
				UserInterface.DIALOGBOX.show(bool ? welcome : newversion, Translator.translate("dialog.greeting.confirm", "ok"),
					bool ? Translator.translate("dialog.greeting.exit", "exit") : Translator.translate("dialog.greeting.update", "update"), DialogBox.NOTHING, () -> {
					if(bool){
						SaveLoad.checkIfShouldSave(true, false);
					}
					else{
						try{ Desktop.getDesktop().browse(new URL("http://fexcraft.net/app/fmt").toURI()); }
						catch(IOException | URISyntaxException e){ e.printStackTrace(); }
					}
				});
			}
		}.start();
	}
	
	public static boolean linux(){
		return LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX;
	}

	public static String getTitle(){
		return title;
	}

}
