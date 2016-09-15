package io.anuke.sevenswords.entities;

import java.nio.file.Path;
import java.util.HashMap;

import net.pixelstatic.utils.MiscUtils;

public abstract class Parseable{
	public HashMap<String, String> values;
	public String name;
	public Path path;

	public abstract void parse(HashMap<String, String> values);
	
	public String name(){
		return MiscUtils.capitalize(name);
	}

}
