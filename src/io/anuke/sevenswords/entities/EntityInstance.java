package io.anuke.sevenswords.entities;

import io.anuke.sevenswords.items.Drop;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.Entity;

import java.util.ArrayList;

public class EntityInstance{
	public final Entity type;
	public int health;
	
	public EntityInstance(Entity type){
		this.type = type;
		this.health = type.maxhealth;
	}
	
	public ArrayList<ItemStack> generateDrops(){
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		for(Drop drop : type.drops){
			if(Math.random() < drop.chance){
				list.add(new ItemStack(drop.item));
			}
		}
		return list;
	}
}
