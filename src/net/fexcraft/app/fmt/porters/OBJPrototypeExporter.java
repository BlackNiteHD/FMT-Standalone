package net.fexcraft.app.fmt.porters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Year;

import net.fexcraft.app.fmt.FMTB;
import net.fexcraft.app.fmt.porters.PorterManager.InternalPorter;
import net.fexcraft.app.fmt.wrappers.GroupCompound;
import net.fexcraft.app.fmt.wrappers.PolygonWrapper;
import net.fexcraft.app.fmt.wrappers.TurboList;
import net.fexcraft.lib.common.math.TexturedPolygon;
import net.fexcraft.lib.common.math.TexturedVertex;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class OBJPrototypeExporter extends InternalPorter {
	
	private static final String[] extensions = new String[]{ ".obj" };
	
	public OBJPrototypeExporter(){}

	@Override
	public GroupCompound importModel(File file){
		return null;
	}
	
	@Override
	public String exportModel(GroupCompound compound, File file){
		StringBuffer buffer = new StringBuffer();
		buffer.append("# FMT-Marker OBJ-1\n#\n");
		buffer.append("# Model exported via the Internal FMT OBJ Exporter\n");
		buffer.append("# FMT (Fex's Modelling Toolbox) v." + FMTB.version + " &copy; " + Year.now().getValue() + " - Fexcraft.net\n");
		buffer.append("# All rights reserved. For this Model's License contact the Author/Creator.\n\n");
		buffer.append("# Model Name\no " + validateName(compound.name) + "\n\n");
		if(compound.creators.size() > 0){
			buffer.append("# Creators/Editors:\n");
			for(String str : compound.creators){
				buffer.append("# - " + str + "\n");
			}
			buffer.append("\n");
		} int faceid = 1;
		//
		for(TurboList list : compound.getCompound().values()){
			buffer.append("# Group Name\n");
			buffer.append("g " + list.id + "\nusemtl fmt_null_material\n");
			for(PolygonWrapper wrapper : list){
				//if(!wrapper.getType().isCuboid()) continue;
				buffer.append("\n");
				if(wrapper.name != null){
					buffer.append("# ID: " + wrapper.name() + "\n");
				}
				//
				TexturedPolygon[] polis = wrapper.getTurboObject(0).getFaces();
				for(TexturedPolygon poly : polis){
					for(TexturedVertex vert : poly.getVertices()){
						buffer.append("v " + (vert.vector.xCoord + wrapper.pos.xCoord)
							+ " " + (vert.vector.yCoord + wrapper.pos.yCoord)
							+ " " + (vert.vector.zCoord + wrapper.pos.zCoord) + "\n");
					}
					for(TexturedVertex vert : poly.getVertices()){
						buffer.append("vt " + vert.textureX + " " + vert.textureY + "\n");
					}
					buffer.append("f"); for(int i = 0; i < poly.getVertices().length; i++){
						buffer.append(" " + (faceid + i) + "/" + (faceid + i));
					} buffer.append("\n"); faceid += poly.getVertices().length;
				}
			}
			buffer.append("\n# FMT-Marker OBJ-END\n");
		}
		//
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.append(buffer); writer.flush(); writer.close();
		}
		catch(IOException e){
			e.printStackTrace();
			return "Error:" + e.getMessage();
		}
		return "Success!";
	}

	private String validateName(String name){
		if(name == null || name.length() == 0) return "Unnamed"; name.replace(" ", ""); return name;
	}

	@Override
	public String getId(){
		return "obj_prototype";
	}

	@Override
	public String getName(){
		return "Prototype OBJ Exporter";
	}

	@Override
	public String[] getExtensions(){
		return extensions;
	}

	@Override
	public boolean isImporter(){
		return false;
	}

	@Override
	public boolean isExporter(){
		return true;
	}

}
