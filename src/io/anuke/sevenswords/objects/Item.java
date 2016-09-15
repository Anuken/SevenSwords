package io.anuke.sevenswords.objects;

import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.items.ItemType;

import java.util.HashMap;


public class Item extends Parseable{
	public String name;
	public ItemType type;
	
	public String toString(){
		return name + ", type: [" + type.name() + "]";
	}

	@Override
	public void parse(HashMap<String, String> values){
		String name = null;
		ItemType type = ItemType.weapon;

		name = (String)values.get("name");
		
		if(values.containsKey("type")) type = ItemType.valueOf((String)values.get("type"));
		
		this.values = values;
		this.name = name;
		this.type = type;
	}
}
