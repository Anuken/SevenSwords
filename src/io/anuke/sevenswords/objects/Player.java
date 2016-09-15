package io.anuke.sevenswords.objects;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Battle;
import io.anuke.sevenswords.items.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class Player{
	public Location location;
	public int health = 100, energy = 100, maxhealth = 100, maxenergy = 100;
	public int level = 1, xp = 0;
	public ArrayList<ItemStack> inventory = new ArrayList<ItemStack>();
	public Battle battle;
	
	public Player(Location location){
		this.location = location;
	}
	
	public void addItems(Collection<ItemStack> items){
		for(ItemStack stack : items){
			addItem(stack);
		}
	}
	
	public void addItem(ItemStack stack){
		for(int i = 0; i < inventory.size(); i ++){
			if(inventory.get(i).item == stack.item){
				inventory.get(i).amount += stack.amount;
				return;
			}
		}
		inventory.add(stack);
	}
	
	public void send(String string){
		Core.core.messages.send(string, battle.chatid);
	}
	
	public boolean attacking(){
		return battle != null;
	}
	
	public int getAttack(){
		return 10 + (int)(Math.random()*10);
	}
	
	public int getDefense(){
		return 1;
	}
	
	public int getEndurance(){
		return 1;
	}
}
