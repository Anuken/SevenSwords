package io.anuke.sevenswords.items;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.objects.Item;

public class Drop{
	public final Item item;
	public final double chance;
	public final int amountMin, amountMax;
	
	public Drop(String item, int amountMin, int amountMax, double chance){
		this.chance = chance;
		this.amountMin = amountMin;
		this.amountMax = amountMax;
		this.item = Core.core.world.getItem(item);
		if(this.item == null){
			throw new RuntimeException("Failure loading drops: item \"" + item + "\" not defined.");
		}
	}
	
	public String toString(){
		return amountMin + ":" + amountMax + "x " + item + " at " + (chance* 100) + " percent";
	}
}
