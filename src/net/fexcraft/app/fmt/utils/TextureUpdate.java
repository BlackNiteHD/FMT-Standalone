package net.fexcraft.app.fmt.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimerTask;
import java.util.stream.Collectors;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.ui.DialogBox;
import net.fexcraft.app.fmt.ui.UserInterface;
import net.fexcraft.app.fmt.utils.TextureManager.Texture;
import net.fexcraft.app.fmt.wrappers.PolygonWrapper;
import net.fexcraft.app.fmt.wrappers.TurboList;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Print;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class TextureUpdate extends TimerTask {
	
	private Texture texture; private static long lastedit;
	public static boolean HALT = true, ALL, SAVESPACE;
	private static ArrayList<PolygonWrapper> list;
	private static BufferedImage image;
	private static int last, per;

	@Override
	public void run(){
        try{
            if(FMTB.MODEL.texture == null) return;
            texture = TextureManager.getTexture(FMTB.MODEL.texture, true);
            if(texture == null || texture.getFile() == null || !texture.getFile().exists()) return;
            if(texture.getFile().lastModified() > lastedit){
            	updateLastEdit(texture.getFile().lastModified()); texture.reload();
            	Print.console("Changes detected, reloading texture.");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
	}
	
	public static final void updateLastEdit(long date){
		lastedit = date; return;
	}

	public static void updateSize(TurboList list){
		Texture texture = TextureManager.getTexture(list == null ? FMTB.MODEL.texture : list.getGroupTexture(), true);
		if(texture == null || texture.getImage() == null) return;
		BufferedImage image = texture.getImage();
		int texX = list == null ? FMTB.MODEL.textureSizeX : list.textureX;
		int texY = list == null ? FMTB.MODEL.textureSizeY : list.textureY;
		int texS = list == null ? FMTB.MODEL.textureScale : list.textureS;
		for(int i = 1; i < texS; i++){ Print.console(texX + " * 2"); texX *= 2; texY *= 2; Print.console(" = " + texX);  }
		if(image.getWidth() != texX || image.getHeight() != texY){
			String tx = (list == null ? FMTB.MODEL.textureSizeX : list.textureX) + "x," + texX + "xs";
			String ty = (list == null ? FMTB.MODEL.textureSizeY : list.textureY) + "y," + texY + "ys";
			if(texX > 4096 || texY > 4096){
				String str = Translator.format("dialog.texture_update.resize.exceeding_4096", "Exceeding 4096 pixel!<nl>[%s], [%s]<nl>Texture Cache NOT updated.", tx,ty);
				UserInterface.DIALOGBOX.show(str, Translator.translate("dialog.texture_update.resize.exceeding_4096.confirm", "OK,"), null, DialogBox.NOTHING, null);
				return;
			}
			texture.resize(texX, texY, 0x00ffffff); TextureManager.saveTexture(FMTB.MODEL.texture);
			if(list == null) FMTB.MODEL.recompile(); else list.recompile(); updateLastEdit(Time.getDate());
			String str = Translator.format("dialog.texture_update.resize.success", "Resized!<nl>[%s], [%s]", tx, ty);
			UserInterface.DIALOGBOX.show(str, Translator.translate("dialog.texture_update.resize.success.confirm", "Good!"), null, DialogBox.NOTHING, null);
		}
		else return;
	}

	public static void tryAutoPos(Boolean bool){
		if(bool == null){
			String str = Translator.translate("dialog.texture_update.auto_positioner.info", "This process may mark<nl>FMT as not responding.");
			String ok = Translator.translate("dialog.texture_update.auto_positioner.info.confirm", "ok");
			FMTB.showDialogbox(str, ok, Translator.translate("dialog.texture_update.auto_positioner.info.cancel", "cancel"), () -> {
				Runnable ZERO = () -> { HALT = false; ALL = false; };
				Runnable AALL = () -> { HALT = false; ALL = true;  };
				String str0 = Translator.translate("dialog.texture_update.auto_positioner.save_space", "Use save-space mode?<nl>May reduce readability.");
				String yes = Translator.translate("dialog.texture_update.auto_positioner.save_space.confirm", "Yes");
				String str1 = Translator.translate("dialog.texture_update.auto_positioner.only_00", "Only process polygons with<nl>0, 0 texture pos?");
				String yes0 = Translator.translate("dialog.texture_update.auto_positioner.only_00.confirm", "Yes");
				String noall = Translator.translate("dialog.texture_update.auto_positioner.only_00.cancel", "No (All)");
				FMTB.showDialogbox(str0, yes, Translator.translate("dialog.texture_update.auto_positioner.save_space.cancel", "No"), () -> {
					SAVESPACE = true; FMTB.showDialogbox(str1, yes0, noall, ZERO, AALL);
				}, () -> {
					SAVESPACE = false; FMTB.showDialogbox(str1, yes0, noall, ZERO, AALL);
				});
			}, DialogBox.NOTHING);
			return;
		} HALT = false; ALL = bool;
		//
		if(list == null){
			list = getSortedList(ALL); last = 0; image = new BufferedImage(FMTB.MODEL.tx(null), FMTB.MODEL.ty(null), BufferedImage.TYPE_INT_ARGB);
			for(int i = 0; i < image.getWidth(); i++){
				for(int j = 0; j < image.getHeight(); j++){
					image.setRGB(i, j, Color.WHITE.getRGB());
				}
			}
		}
		try{
			if(HALT || last < 0 || last >= list.size()){
				String str = Translator.translate("dialog.texture_update.auto_positioner.complete", "Auto texture positioning<nl>Complete!");
				FMTB.showDialogbox(str, Translator.translate("dialog.texture_update.auto_positioner.complete.confirm", "Good!"), null, DialogBox.NOTHING, null);
				last = (HALT = (list = null) == null) ? -1 : 0; image = null; return;
			}
			PolygonWrapper wrapper = list.get(last); last++;
			String str = Translator.format("dialog.texture_update.auto_positioner.processing", "Processing: %s percent<nl>%s:%s",
				per = getPercent(last, list.size()), wrapper.getTurboList().id, wrapper.name());
			FMTB.showDialogbox(str, null, null, null, null, per, null);
			if(wrapper.texpos == null || wrapper.texpos.length == 0){ Print.console("skipping1 [" + wrapper.getTurboList().id + ":" + wrapper.name() + "]"); return; }
			if(wrapper.textureX != 0f && wrapper.textureY != 0f && !ALL){
				Print.console("skipping0 [" + wrapper.getTurboList().id + ":" + wrapper.name() + "]");
				wrapper.burnToTexture(image, null); Thread.sleep(10); return;
			}
			//
			for(int yar = 0; yar < FMTB.MODEL.ty(null); yar++){
				for(int xar = 0; xar < FMTB.MODEL.tx(null); xar++){
					if(check(wrapper.texpos, xar, yar)){
						Print.console("[" + wrapper.getTurboList().id + ":" + wrapper.name() + "] >> " + xar + "x, " + yar + "y;");
						wrapper.textureX = xar; wrapper.textureY = yar; wrapper.recompile(); wrapper.burnToTexture(image, null); Thread.sleep(10);
						return;
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace(); //FMTB.showDialogbox("Autoposition failed with Exception", "See Console for details.", "ok", null, DialogBox.NOTHING, null);
		}
	}
	
	private static boolean check(float[][][] texpos, int xx, int yy){
		float[][] ends = null;
		for(int i = 0; i < texpos.length; i++){ ends = texpos[i];
			if(!SAVESPACE){
				float[][] newend = new float[ends.length][];
				for(int k = 0; k < newend.length; k++){
					if(newend[k] == null){ newend[k] = new float[ends[k].length]; }
					for(int l = 0; l < newend[k].length; l++){ newend[k][l] = ends[k][l]; }
				} ends = newend;
				//
				ends[0][0] -= 1; ends[1][0] += 1;//x
				ends[0][1] -= 1; ends[1][1] += 1;//y
			}
			for(float y = ends[0][1]; y < ends[1][1]; y += 0.5f){
				for(float x = ends[0][0]; x < ends[1][0]; x += 0.5f){
					int xr = (int)(xx + x), yr = (int)(yy + y);
					if(xr < 0  || yr < 0 ) continue;
					if(xr >= image.getWidth()|| yr >= image.getHeight()) return false;
					//
					if(image.getRGB(xr, yr) != Color.WHITE.getRGB()){ /*Print.console(xr + " " + yr + " || " + x + " " + y);*/ return false; } else continue;
				}
			}
		} return true;
	}
	
	private static ArrayList<PolygonWrapper> getSortedList(boolean all){
		ArrayList<PolygonWrapper> arrlist = new ArrayList<>();
		for(TurboList list : FMTB.MODEL.getGroups()){ arrlist.addAll(list); }
		arrlist.sort(new java.util.Comparator<PolygonWrapper>(){
			@Override
			public int compare(PolygonWrapper left, PolygonWrapper righ){
				int x0 = (int)(left.getType().isCylinder() ? left.getFloat("cyl0", true, false, false) * 4 : left.getFloat("size", true, false, false));
				int x1 = (int)(righ.getType().isCylinder() ? righ.getFloat("cyl0", true, false, false) * 4 : righ.getFloat("size", true, false, false));
				int y0 = (int)(left.getType().isCylinder() ? (left.getFloat("cyl0", true, false, false) * 2) + left.getFloat("cyl0", true, false, false) : left.getFloat("size", false, true, false));
				int y1 = (int)(righ.getType().isCylinder() ? (righ.getFloat("cyl0", true, false, false) * 2) + righ.getFloat("cyl0", true, false, false) : righ.getFloat("size", false, true, false));
				if(Integer.compare(x0, x1) > 1){ return Integer.compare(y0, y1); } return Integer.compare(x0, x1);
			}
		});
		Collections.reverse(arrlist);
		if(!all){
			ArrayList<PolygonWrapper> pri = (ArrayList<PolygonWrapper>)arrlist.stream().filter(pre -> pre.textureX > 0 || pre.textureY > 0).collect(Collectors.toList());
			ArrayList<PolygonWrapper> sec = (ArrayList<PolygonWrapper>)arrlist.stream().filter(pre -> pre.textureX <= 0 || pre.textureY <= 0).collect(Collectors.toList());
			arrlist.clear(); arrlist.addAll(pri); arrlist.addAll(sec);
		} return arrlist;
	}
	
	private static int getPercent(int i, int all){ return (i * 100) / all; }

}
