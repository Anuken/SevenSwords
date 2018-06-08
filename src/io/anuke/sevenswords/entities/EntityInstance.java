package io.anuke.sevenswords.entities;

import java.util.ArrayList;

import io.anuke.sevenswords.items.Drop;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.Entity;
import io.anuke.ucore.util.Mathf;

public class EntityInstance{
	public final Entity type;
	public int health;
	
	public EntityInstance(Entity type){
		this.type = type;
		this.health = type.health;
	}
	
	public ArrayList<ItemStack> generateDrops(){
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(Drop drop : type.drops){
			if(Math.random() < drop.chance){
				list.add(new ItemStack(drop.item, Mathf.random(drop.amountMin, drop.amountMax)));
			}
		}
		return list;
	}
}
