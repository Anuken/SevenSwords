package io.anuke.sevenswords.objects;

import static io.anuke.sevenswords.items.ItemType.amulet;
import static io.anuke.sevenswords.items.ItemType.armor;
import static io.anuke.sevenswords.items.ItemType.boots;
import static io.anuke.sevenswords.items.ItemType.head;
import static io.anuke.sevenswords.items.ItemType.offhand;
import static io.anuke.sevenswords.items.ItemType.weapon;
import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Battle;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.items.ItemType;

import java.util.ArrayList;
import java.util.Collection;

public class Player{
	public static String[] equipnames = {"Weapon","Head", "Armor", "Boots", "Amulet", "Off-Hand"};
	public static ItemType[] equiptypes = {weapon, head, armor, boots, amulet, offhand};
	public Location location;
	public int health = 100, energy = 100, maxhealth = 100, maxenergy = 100;
	public int level = 1, xp = 0;
	public ArrayList<ItemStack> inventory = new ArrayList<ItemStack>();
	public Battle battle;
	public ItemStack[] equips = new ItemStack[6];
	
	public Player(Location location){
		this.location = location;
	}
	
	public boolean tryEquip(int slot, ItemStack stack){
		if(stack.item.type == equiptypes[slot]){
			equips[slot] = stack;
			return true;
		}
		return false;
	}
	
	public String tryEquip(ItemStack stack){
		int slot = -1;
		for(int i = 0; i < equiptypes.length; i ++){
			if(stack.item.type == equiptypes[i]){
				slot = i;
				break;
			}
		}
		if(slot == -1) return "You cannot equip that item.";
		
		if(equips[slot] != null) inventory.add(equips[slot]);
		equips[slot] = stack;
		return "Item equipped.";
	}
	
	public ItemStack getWeapon(){
		return equips[0];
	}
	
	public ItemStack getHead(){
		return equips[1];
	}
	
	public ItemStack getArmor(){
		return equips[2];
	}
	
	public ItemStack getBoots(){
		return equips[3];
	}
	
	public ItemStack getAmulet(){
		return equips[4];
	}
	
	public ItemStack getOffhand(){
		return equips[5];
	}
	
	public void setWeapon(ItemStack stack){
		equips[0] = stack;
	}
	
	public void setHead(ItemStack stack){
		equips[1] = stack;
	}
	
	public void setArmor(ItemStack stack){
		equips[2] = stack;
	}
	
	public void setBoots(ItemStack stack){
		equips[3] = stack;
	}
	
	public void setAmulet(ItemStack stack){
		equips[4] = stack;
	}
	
	public void setOffhand(ItemStack stack){
		equips[5] = stack;
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
