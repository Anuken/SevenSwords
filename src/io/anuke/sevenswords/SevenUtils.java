package io.anuke.sevenswords;

public class SevenUtils{
	
	public static String capitalize(String in){
		char[] chars = in.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		
		for(int i = 0; i < in.length(); i ++){
			if(chars[i] == '_'){
				chars[i] = ' ';
				if(i + 1 < in.length()){
					chars[i+1] = Character.toUpperCase(chars[i+1]);
				}
			}
		}
		
		return new String(chars);
	}
	
	
	public static String merge(Iterable<? extends Object> objects, String seperator){
		String out = "";
		
		int i = 0;
		
		for(Object o : objects){
			if(i ++ != 0){
				out += seperator;
			}
			out += o.toString();
		}
		
		return out;
	}
}
