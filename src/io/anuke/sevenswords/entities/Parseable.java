package io.anuke.sevenswords.entities;

import io.anuke.utils.MiscUtils;

import java.nio.file.Path;
import java.util.HashMap;

public abstract class Parseable{
	public HashMap<String, String> values;
	public String name;
	public Path path;

	public abstract void parse(HashMap<String, String> values);
	
	public String name(){
		return MiscUtils.capitalize(name.replace("_", " "));
	}
	
	public int getInt(String name){
		if(!values.containsKey(name)) return 0;
		return Integer.parseInt(values.get(name));
	}
	
	public boolean has(String name){
		return values.containsKey(name);
	}

}
