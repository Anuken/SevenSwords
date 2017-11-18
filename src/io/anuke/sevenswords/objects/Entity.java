package io.anuke.sevenswords.objects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.items.Drop;

public class Entity extends Parseable{
	public String name;
	public int health, attack, defence, exp;
	public ArrayList<Drop> drops = new ArrayList<Drop>();

	public String toString(){
		return String.format("EntityType: %s, drops: " + drops + ", maxhealth: %d, attack: %d, defence: %d, XP drop: %d", name, health, attack, defence, exp);
	}

	@Override
	public void parse(HashMap<String, String> values){
		name = (String)values.get("name");

		Field[] fields = getClass().getFields();

		for(Field field : fields){
			try{
				if(field.getType() == int.class && values.containsKey(field.getName())){
					field.set(this, Integer.parseInt((String)values.get(field.getName())));
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		if(values.containsKey("drops")){
			drops = parseDrop(values.get("drops"));
		}
		this.values = values;
	}
	
	private ArrayList<Drop> parseDrop(String value){
		String[] drops = value.split(",");
		ArrayList<Drop> list = new ArrayList<Drop>();
		for(String string : drops){
			String[] dsplit = string.split("-");
			String chancestring = null;

			if(dsplit.length == 2){
				chancestring = dsplit[1];
			}else if(dsplit.length == 3){
				chancestring = dsplit[2];
			}else{
				chancestring = "1";
			}

			double chance = 0;
			int amount = 1;

			if(dsplit.length == 3){
				amount = Integer.parseInt(dsplit[1]);
			}

			if(chancestring.contains("/")){
				String[] csplit = chancestring.split("/");
				chance = Double.parseDouble(csplit[0]) / Double.parseDouble(csplit[1]);
			}else{
				chance = 1.0 / Double.parseDouble(chancestring);
			}

			list.add(new Drop(dsplit[0], amount, chance));
		}
		return list;
	}
}
