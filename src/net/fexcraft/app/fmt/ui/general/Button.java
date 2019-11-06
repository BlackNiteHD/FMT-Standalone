package net.fexcraft.app.fmt.ui.general;

import net.fexcraft.app.fmt.ui.Element;
import net.fexcraft.app.fmt.ui.FontRenderer;
import net.fexcraft.app.fmt.ui.FontRenderer.FontType;
import net.fexcraft.lib.common.math.RGB;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class Button extends Element {
	
	protected RGB iconcolor = null;
	protected boolean centered, drawbackground = true;
	protected String text, icon;
	protected int iconsize, texxoff = 2, texyoff = 2;
	
	public Button(Element root, String id, String style, int width, int height, int x, int y){
		super(root, id, style == null ? root instanceof HoverMenu ? "menu:button" : "button" : style);
		this.setPosition(x, y).setSize(width, height).setColor(0xffc7c7c7).setEnabled(true);
		this.setBorder(0xff000000, 0xff000000, 1, root instanceof HoverMenu == false, true, true, true); this.setupSubmenu();
	}
	
	@Override
	public Element repos(){
		x = root.x + xrel; y = root.y + yrel + (root instanceof HoverMenu ? getIndex() * 28 + 1 : 0);
		clearVertexes(); for(Element elm : elements) elm.repos(); return this;
	}
	
	private int getIndex(){
		for(int i = 0; i < root.getElements().size(); i++){
			if(this == root.getElements().get(i)) return i;
		} return -1;
	}

	public Button(Element elm, String id, String style, int w, int h, int x, int y, int hover){
		this(elm, id, style, w, h, x, y); this.setHoverColor(hover, false);
	}
	
	public Button(Element elm, String id, String style, int w, int h, int x, int y, int hover, int dis){
		this(elm, id, style, w, h, x, y, hover); this.setHoverColor(dis, true);
	}
	
	public Button setBackgroundless(boolean bool){
		this.drawbackground = !bool; return this;
	}

	/** To be overridden. **/
	public void setupSubmenu(){}

	public Button setText(String string, boolean centered){
		text = string; this.centered = centered;
		if(this.root instanceof HoverMenu){
			int leng = FontRenderer.getWidth(text, FontType.BOLD);
			if(leng + 10 > this.width) this.width = leng + 10;
			for(Element  elm : this.root.getElements()){
				if(elm.width < width) elm.width = this.width;
			}
			if(this.root.width - 4 < this.width) this.root.width = this.width + 4;
			if(this.width + 4 < this.root.width) this.width = this.root.width - 4;
		}
		if(root instanceof Toolbar){
			int length = FontRenderer.getWidth(text, FontType.BOLD);
			if(length + 8 > width) width = length + 8;
		}
		return this;
	}

	public Button setText(String string, int texxoff, int texyoff){
		text = string; this.centered = false; this.texxoff = texxoff; this.texyoff = texyoff; return this;
	}
	
	public Button setIcon(String texture, int size){
		this.icon = texture; this.iconsize = size; return this;
	}
	
	public Button setIcon(String texture, int size, RGB color){
		this.iconcolor = color; return this.setIcon(texture, size);
	}

	@Override
	public void renderSelf(int rw, int rh){
		if(drawbackground){
			if(hovered) (enabled ? hovercolor : discolor).glColorApply();
			this.renderSelfQuad();
			if(hovered) RGB.glColorReset();
		}
		if(text != null){
			RGB color = !drawbackground ? hovered ? hovercolor : !enabled ? discolor : RGB.BLACK : RGB.BLACK;
			if(centered){
				int x = width / 2 - (FontRenderer.getWidth(text, FontType.BOLD) / 2), y = height / 2 - 10;
				FontRenderer.drawText(text, this.x + x + (icon == null ? 0 : iconsize + 2), this.y + y, FontType.BOLD, color);
			}
			else{
				FontRenderer.drawText(text, x + texxoff + (icon == null ? 0 : iconsize + 2), y + texyoff, FontType.BOLD, color);
			}
		}
		if(icon != null){
			if(iconcolor != null) iconcolor.glColorApply();
			float y = (height - iconsize) * 0.5f;
			this.renderIcon(x + 2, this.y + y, iconsize, icon);
			if(iconcolor != null) RGB.glColorReset();
		}
	}
	
	@Override
	public void hovered(float mx, float my){
		super.hovered(mx, my); if(this.hovered){ for(Element elm : elements) elm.setVisible(true); }
	}

	public String getText(){
		return text;
	}

}
