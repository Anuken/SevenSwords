package io.anuke.sevenswords.objects;

import java.util.ArrayList;
import java.util.HashMap;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.items.ItemStack;

public class Recipe extends Parseable{
	public ArrayList<ItemStack> requirements = new ArrayList<>();
	public ItemStack result;

	@Override
	public void parse(HashMap<String, String> values){
		name = values.get("name");
		
		if(!values.containsKey("result")){
			throw new RuntimeException("Recipe \"" + name + "\" is missing a result!");
		}
		
		if(!values.containsKey("items")){
			throw new RuntimeException("Recipe \"" + "is missing the requirement \"items\" property!");
		}
		
		
		
		String resultsp = values.get("result");
		int resultq = 1;
		if(resultsp.split("-").length == 2){
			resultq = Integer.parseInt(resultsp.split("-")[1]);
			resultsp = resultsp.split("-")[0];
		}
		
		if(Core.core.world.getItem(resultsp) == null)
			throw new RuntimeException("Item not found: \""+ resultsp+ "\"");
		
		result = new ItemStack(Core.core.world.getItem(resultsp), resultq);
		
		String[] items = values.get("items").split(",");
		for(String str : items){
			int amount = 1;
			String name = str;
			if(str.split("-").length == 2){
				name = str.split("-")[0];
				amount = Integer.parseInt(str.split("-")[1]);
			}
			if(Core.core.world.getItem(name) == null)
				throw new RuntimeException("Item not found: \""+ name + "\"");
			requirements.add(new ItemStack(Core.core.world.getItem(name), amount));
		}
	}

}
