package net.fexcraft.app.fmt.wrappers;

public enum ShapeType {
	
	BOX("cuboid"), SHAPEBOX("cuboid"), TEXRECT_B("cuboid"), TEXRECT_A("cuboid"),
	FLEXBOX("flexcuboid"), TRAPEZOID("flexcuboid"), FLEXTRAPEZOID("flexcuboid"),
	CYLINDER("cylinder"), SPHERE("sphere"), OBJ("obj"), MARKER("marker");
	
	private String conversion_group;
	
	ShapeType(String congroup){
		this.conversion_group = congroup;
	}

	public boolean isCuboid(){
		return this == BOX || this == SHAPEBOX || this == TEXRECT_B || this == TEXRECT_A || this == FLEXBOX || this == TRAPEZOID || this == FLEXTRAPEZOID;
	}

	public boolean isShapebox(){
		return this == SHAPEBOX || this == TEXRECT_B || this == TEXRECT_A;
	}

	public boolean isCylinder(){
		return this == CYLINDER;
	}

	public boolean isTexRect(){
		return this == TEXRECT_B || this == TEXRECT_A;
	}

	public boolean isTexRectB(){
		return this == TEXRECT_B;
	}

	public boolean isTexRectA(){
		return this == TEXRECT_A;
	}
	
	public boolean isMarker(){
		return this == MARKER;
	}

	public static ShapeType get(String text){
		text = text.toLowerCase();
		switch(text){
			case "box": return BOX;
			case "shapebox": return SHAPEBOX;
			case "texrect_a": return TEXRECT_A;
			case "texrect_b": return TEXRECT_B;
			case "flexbox": return FLEXBOX;
			case "trapezoid": return TRAPEZOID;
			case "flextrapezoid": return FLEXTRAPEZOID;
			case "cylinder": return CYLINDER;
			case "sphere": return SPHERE;
			case "obj": return OBJ;
			case "marker": return MARKER;
			default: return null;
		}
	}

	public String getConversionGroup(){
		return conversion_group;
	}

	public static ShapeType[] getSupportedValues(){
		return new ShapeType[]{ BOX, SHAPEBOX, TEXRECT_B, TEXRECT_A, CYLINDER, MARKER };
	}

}
