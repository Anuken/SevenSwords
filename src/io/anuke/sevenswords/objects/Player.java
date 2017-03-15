package io.anuke.sevenswords.objects;

import static io.anuke.sevenswords.items.ItemType.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Battle;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.items.ItemType;

public class Player{
	public static ItemType[] equiptypes = {weapon, helm, armor, boots, amulet, offhand};
	public static double exponent = 3, scale = 100;
	public final String id;
	public Location location;
	public int health = 100, energy = 100, maxhealth = 100, maxenergy = 100;
	public int level = 0;
	public long xp = 0;
	public ArrayList<ItemStack> inventory = new ArrayList<ItemStack>();
	public Battle battle;
	public ItemStack[] equips = new ItemStack[6];
	
	public Player(String id, Location location){
		this.location = location;
		this.id = id;
		location.players.add(this);
	}
	
	public String name(){
		return Core.core.messages.getUserName(id);
	}
	
	public ItemStack findItem(Predicate<ItemStack> pred){
		for(ItemStack stack : inventory){
			if(pred.test(stack)){
				return stack;
			}
		}
		return null;
	}
	
	
	public ItemStack findItem(String name){
		return findItem((stack)->{return stack.nameIs(name);});
	}
	
	public void useItem(String name, Consumer<ItemStack> found, Runnable notFound){
		ItemStack item = findItem((stack)->{return stack.nameIs(name);});
		if(item != null){
			found.accept(item);
		}else{
			notFound.run();
		}
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
			if(stack.item.type == ItemType.valueOf(EquipSlot.values()[i].name())){
				slot = i;
				break;
			}
		}
		if(slot == -1) return "You cannot equip that item.";
		
		if(equips[slot] == stack) return "That item is already equipped.";
		if(equips[slot] != null) inventory.add(equips[slot]);
		equips[slot] = stack;
		return "Item equipped.";
	}
	
	public void setEquip(EquipSlot slot, ItemStack stack){
		equips[slot.ordinal()] = stack;
	}
	
	public ItemStack getEquip(EquipSlot slot){
		return equips[slot.ordinal()];
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
		int def = 0;
		
		for(ItemStack stack : equips){
			if(stack != null)
			def += stack.item.getInt("defense");
		}
		
		return 1 + def;
	}
	
	public int getEndurance(){
		return 1;
	}
	
	public void addXP(int amount){
		xp += amount;
		level = xpToLevel(xp);
	}
	
	public int levelToXP(int i){
		return (int)(Math.pow(i, exponent) * scale);
	}
	
	public int xpToLevel(long xp){
		return (int)Math.pow(xp/scale, 1f/exponent);
	}
	
	public String toString(){
		return name();
	}
	
	static public enum EquipSlot{
		weapon("Weapon"), offhand("Off-Hand"), helm("Helm"), armor("Armor"), boots("Boots"), amulet("Amulet");
		
		public final String name;
		
		private EquipSlot(String name){
			this.name = name;
		}
	}
}
