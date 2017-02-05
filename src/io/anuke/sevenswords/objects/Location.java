package io.anuke.sevenswords.objects;

import java.util.HashMap;
import java.util.HashSet;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Parseable;


public class Location extends Parseable{
	public Entity[] entities;
	public HashSet<Player> players = new HashSet<Player>();

	@Override
	public void parse(HashMap<String, String> values){
		if(values.containsKey("entities")){
			String string = values.get("entities");
			String[] types = string.contains(",") ? string.split(",") : new String[]{string};
			entities = new Entity[types.length];
			for(int i = 0; i< types.length; i ++){
				entities[i] = Core.core.world.getEntity(types[i]);
				if(entities[i] == null){
					throw new RuntimeException("Failure loading location: entity \"" + types[i] + "\" not defined.");
				}
			}
		}
	}
}
