package io.anuke.sevenswords.items;

import io.anuke.sevenswords.objects.Item;

public class ItemStack{
	public Item item;
	public int amount;
	
	public ItemStack(Item item, int amount){
		this.item = item;
		this.amount = amount;
	}
	
	public ItemStack(Item item){
		this(item, 1);
	}
	
	public String toString(){
		return item.name() + (amount == 1 ? "" : " x" +amount);
	}
}
