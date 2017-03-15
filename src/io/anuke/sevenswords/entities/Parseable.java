package io.anuke.sevenswords.entities;

import java.nio.file.Path;
import java.util.HashMap;

import io.anuke.sevenswords.SevenUtils;

public abstract class Parseable{
	public HashMap<String, String> values;
	public String name;
	public Path path;

	public abstract void parse(HashMap<String, String> values);
	
	public String name(){
		return SevenUtils.capitalize(name);
	}
	
	public String uncappedName(){
		return name.replace("_", " ");
	}
	
	public String get(String name){
		return values.get(name);
	}
	
	public int getInt(String name){
		if(!values.containsKey(name)) return 0;
		return Integer.parseInt(values.get(name));
	}
	
	public boolean has(String name){
		return values.containsKey(name);
	}

}
