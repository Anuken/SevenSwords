package io.anuke.sevenswords.items;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.objects.Item;

public class Drop{
	public final Item item;
	public final double chance;
	public final int quantity;
	
	public Drop(String item, int quantity, double chance){
		this.chance = chance;
		this.quantity = quantity;
		this.item = Core.core.world.getItem(item);
	}
	
	public String toString(){
		return quantity + "x " + item + " at " + (chance* 100) + " percent";
	}
}
