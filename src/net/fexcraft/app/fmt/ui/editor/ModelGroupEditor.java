package net.fexcraft.app.fmt.ui.editor;

import java.util.ArrayList;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.ui.general.Button;
import net.fexcraft.app.fmt.ui.general.TextField;
import net.fexcraft.app.fmt.utils.TextureUpdate;
import net.fexcraft.app.fmt.wrappers.GroupCompound;
import net.fexcraft.app.fmt.wrappers.TurboList;
import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Vec3f;

public class ModelGroupEditor extends Editor {
	
	private static final int[] accepted_texsiz = new int[]{ 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 };
	private ContainerButton group, model;

	public ModelGroupEditor(){
		super("model_group_editor");
	}

	@Override
	protected ContainerButton[] setupSubElements(){
		group = new ContainerButton(this, "group", 300, 28, 4, y, new int[]{ 1, 3, 1, 1 }){
			@Override
			public void addSubElements(){
				this.elements.add(new Button(this, "text0", 290, 20, 0, 0, RGB.WHITE).setText("Group Preview Color/Overlay", false).setRowCol(0, 0));
				for(int i = 0; i < 3; i++){ final int j = i;
					this.elements.add(new TextField(this, "group_rgb" + i, 0, 0, 0){
						@Override public void updateNumberField(){ updateRGB(null, j); }
						@Override protected boolean processScrollWheel(int wheel){ return updateRGB(wheel > 0, j); }
					}.setAsNumberfield(0, 255, true).setRowCol(1, i));
				}
				this.elements.add(new Button(this, "text1", 290, 20, 0, 0, RGB.WHITE).setText("Group Name/ID", false).setRowCol(2, 0));
				this.elements.add(new TextField(this, "groupname", 0, 0, 0){
					@Override
					public void updateTextField(){
						if(FMTB.MODEL.getSelected().isEmpty()) return;
						TurboList list = null;
						if(FMTB.MODEL.getDirectlySelectedGroupsAmount() == 1){
							if(FMTB.MODEL.getCompound().isEmpty()) return;
							list = FMTB.MODEL.getFirstSelectedGroup();
							list = FMTB.MODEL.getCompound().remove(list.id);
							list.id = this.getTextValue().replace(" ", "_").replace("-", "_").replace(".", "");
							while(FMTB.MODEL.getCompound().containsKey(list.id)){ list.id += "_"; }
							FMTB.MODEL.getCompound().put(list.id, list);
						}
						else{
							ArrayList<TurboList> arrlist = FMTB.MODEL.getDirectlySelectedGroups();
							for(int i = 0; i < arrlist.size(); i++){
								list = FMTB.MODEL.getCompound().remove(arrlist.get(i).id); if(list == null) continue;
								list.id = this.getTextValue().replace(" ", "_").replace("-", "_").replace(".", "");
								list.id += list.id.contains("_") ? "_" + i : i + "";
								while(FMTB.MODEL.getCompound().containsKey(list.id)){ list.id += "_"; }
								FMTB.MODEL.getCompound().put(list.id, list);
							}
						}
						FMTB.MODEL.getSelected().clear();
					}
				}.setText("null", true).setRowCol(3, 0));
			}
		};
		group.setText("Group Settings", false);
		model = new ContainerButton(this, "model", 300, 28, 4, y, new int[]{ 1, 3, 1, 3, 1, 2, 1, 1 }){
			@Override
			public void addSubElements(){
				this.elements.add(new Button(this, "text0", 290, 20, 0, 0, RGB.WHITE).setText("Position Offset (full unit)", false).setRowCol(0, 0));
				this.elements.add(new Button(this, "text1", 290, 20, 0, 0, RGB.WHITE).setText("Rotation Offset (degrees)", false).setRowCol(2, 0));
				this.elements.add(new Button(this, "text2", 290, 20, 0, 0, RGB.WHITE).setText("Texture Size (U/V)", false).setRowCol(4, 0));
				this.elements.add(new Button(this, "text3", 290, 20, 0, 0, RGB.WHITE).setText("Model Name", false).setRowCol(6, 0));
				//
				for(int i = 0; i < 3; i++){
					final int j = i;
					this.elements.add(new TextField(this, "model_pos" + xyz[i], 0, 0, 0){
						@Override public void updateNumberField(){ updatePos(this, j, null); }
						@Override protected boolean processScrollWheel(int wheel){ return updatePos(j, wheel > 0); }
					}.setAsNumberfield(Integer.MIN_VALUE, Integer.MAX_VALUE, true).setRowCol(1, i));
					//
					this.elements.add(new TextField(this, "model_rot" + xyz[i], 0, 0, 0){
						@Override public void updateNumberField(){ updateRot(this, j, null); }
						@Override protected boolean processScrollWheel(int wheel){ return updateRot(j, wheel > 0); }
					}.setAsNumberfield(8, 4096, true).setRowCol(3, i));
					//
					if(i >= 2) continue;
					this.elements.add(new TextField(this, "model_tex" + xyz[i], 0, 0, 0){
						@Override public void updateNumberField(){ updateTexSize(this, j, null); }
						@Override protected boolean processScrollWheel(int wheel){ return updateTexSize(j, wheel > 0); }
					}.setAsNumberfield(8, 4096, true).setRowCol(5, i));
				}
				//
				this.elements.add(new TextField(this, "model_name", 0, 0, 0) {
					@Override public void updateTextField(){ if(FMTB.MODEL == null) return; FMTB.get().setTitle(FMTB.MODEL.name = this.getTextValue()); }
				}.setText(FMTB.MODEL.name, true).setRowCol(7, 0));
			}
		};
		model.setText("Model Settings", false);
		return new ContainerButton[]{ model, group };
	}
	
	protected boolean updateRGB(Boolean apply, int j){
		TextField field = (TextField)group.getElement("group_rgb" + j);
		if(apply != null) field.applyChange(field.tryChange(apply, FMTB.MODEL.rate));
		TurboList sel = FMTB.MODEL.getFirstSelectedGroup();
		if(sel != null){
			if(sel.color == null) sel.color = new RGB(RGB.WHITE);
			byte[] arr = sel.color.toByteArray();
			byte colorr = (byte)(field.getIntegerValue() - 128);
			switch(j){
				case 0: sel.color = new RGB(colorr, arr[1], arr[2]); break;
				case 1: sel.color = new RGB(arr[0], colorr, arr[2]); break;
				case 2: sel.color = new RGB(arr[0], arr[1], colorr); break;
			}
			arr = sel.color.toByteArray();
			if(arr[0] == 127 && arr[1] == 127 && arr[2] == 127) sel.color = null;
		} return true;
	}
	
	protected boolean updateTexSize(int axis, Boolean positive){
		return updateTexSize(null, axis, positive);
	}
	
	protected boolean updateTexSize(TextField field, int axis, Boolean positive){
		if(FMTB.MODEL == null) return true; if(field == null) field = (TextField)model.getElement("model_tex" + xyz[axis]);
		int index = getIndex(field.getIntegerValue());
		if(positive && index < (accepted_texsiz.length - 1)) field.applyChange(accepted_texsiz[index + 1]);
		else if(!positive && index > 0) field.applyChange(accepted_texsiz[index - 1]);
		//
		FMTB.MODEL.textureX = ((TextField)model.getElement("model_texx")).getIntegerValue();
		FMTB.MODEL.textureY = ((TextField)model.getElement("model_texy")).getIntegerValue();
		TextureUpdate.updateSizes(); return true;
	}
	
	private int getIndex(int val){
		for(int i = 0; i < accepted_texsiz.length; i++){ if(val == accepted_texsiz[i]) return i; } return 0;
	}
	
	protected boolean updatePos(int axis, Boolean positive){
		return updatePos(null, axis, positive);
	}
	
	protected boolean updatePos(TextField field, int axis, Boolean positive){
		if(FMTB.MODEL == null) return true;
		if(field == null) field = (TextField)model.getElement("model_pos" + xyz[axis]);
		if(FMTB.MODEL.pos == null) FMTB.MODEL.pos = new Vec3f();
		float am = positive == null ? field.getFloatValue() : positive ? FMTB.MODEL.rate : -FMTB.MODEL.rate;
		if(am == 0f) return true;
		switch(axis){
			case 0:{ FMTB.MODEL.pos.xCoord += am; field.applyChange(FMTB.MODEL.pos.xCoord); break; }
			case 1:{ FMTB.MODEL.pos.yCoord += am; field.applyChange(FMTB.MODEL.pos.yCoord); break; }
			case 2:{ FMTB.MODEL.pos.zCoord += am; field.applyChange(FMTB.MODEL.pos.zCoord); break; }
		}
		return true;
	}
	
	protected boolean updateRot(int axis, Boolean positive){
		return updateRot(null, axis, positive);
	}
	
	protected boolean updateRot(TextField field, int axis, Boolean positive){
		GroupCompound compound = FMTB.MODEL; if(compound == null) return true;
		if(field == null) field = (TextField)model.getElement("model_rot" + xyz[axis]);
		if(compound.rot == null) compound.rot = new Vec3f();
		float am = positive == null ? field.getFloatValue() : positive ? FMTB.MODEL.rate : -FMTB.MODEL.rate;
		switch(axis){
			case 0:{
				compound.rot.xCoord += am;
				if(compound.rot.xCoord > 360) compound.rot.xCoord = 360;
				if(compound.rot.xCoord < -360) compound.rot.xCoord = -360;
				field.applyChange(compound.rot.xCoord);
				break;
			}
			case 1:{
				compound.rot.yCoord += am;
				if(compound.rot.yCoord > 360) compound.rot.yCoord = 360;
				if(compound.rot.yCoord < -360) compound.rot.yCoord = -360;
				field.applyChange(compound.rot.yCoord);
				break;
			}
			case 2:{
				compound.rot.zCoord += am;
				if(compound.rot.zCoord > 360) compound.rot.zCoord = 360;
				if(compound.rot.zCoord < -360) compound.rot.zCoord = -360;
				field.applyChange(compound.rot.zCoord);
				break;
			}
		}
		return true;
	}

}