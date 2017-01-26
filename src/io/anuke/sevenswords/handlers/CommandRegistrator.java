package io.anuke.sevenswords.handlers;

import java.util.HashMap;
import java.util.function.Consumer;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.handlers.CommandHandler.Command;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.items.ItemType;
import io.anuke.sevenswords.objects.Entity;
import io.anuke.sevenswords.objects.Location;
import io.anuke.sevenswords.objects.Player;
import io.anuke.sevenswords.objects.Player.EquipSlot;
import io.anuke.ucore.UCore;
import io.anuke.utils.MiscUtils;

public class CommandRegistrator{
	public static CommandHandler handler;
	
	public static void register(){
		
		cmd("location", ()->{
			send("Location: " + player().location.name());
		});
		
		cmd("look", ()->{
			send("Monsters: ");
			for(Entity type : player().location.entities){
				send("- " + type.name());
			}
		});
		
		cmd("equips", ()->{
			send("Equipment: ");
			for(int i = 0; i < player().equips.length; i++){
				send(EquipSlot.values()[i].name + ": " + (player().equips[i] == null ? "None" : player().equips[i].item.name()));
			}
		});
		
		cmd("stats", ()->{
			send("Level " + player().level + ".");
			send(player().xp + " XP.");
			send("Health: " + player().health + "/" + player().maxhealth);
			send("Energy: " + player().energy + "/" + player().maxenergy);
		});
		
		cmd("inventory", ()->{
			if(player().inventory.size() == 0){
				send("Your inventory is empty.");
			}else{
				send("Inventory: ");
				int i = 0;
				for(ItemStack item : player().inventory){
					send("- " + item.toString() + " [" + i++ + "]");
				}
			}
		});
		
		cmd("level", ()->{
			int lxp = player().levelToXP(player().level);
			int tolevel = player().levelToXP(player().level + 1) - lxp;
			send("Level: " + player().level);
			send("XP: " + (player().xp - lxp) + "/" + tolevel);
		});
		
		cmd("help", ()->{
			send("Commands:");
			for(Command command : handler.getCommandList()){
				send("-" + command.text + " " + command.params);
			}
		});
		
		cmd("locations", ()->{
			send("Locations: ");
			for(Parseable loc : core().world.objects.get(Location.class).values()){
				send("- " + loc.name());
			}
		});
		
		cmd("move", "<location>", (args)->{
			String string = args[0];
			Parseable loc = core().world.get(string, Location.class);
			if(loc != null){
				if(loc.name.equals(string)){

					if(loc == player().location){
						send("You already are at that location.");
						return;
					}

					send("Moving to " + MiscUtils.capitalize(string) + ".");
					player().location = (Location) loc;
					return;
				}
			}else{
				send("Location not found. Type -locations for a list of locations.");
			}
		});
		
		cmd("equip", "<item-name>", (args)->{
			player().useItem(args[0], (stack)->{
				send(player().tryEquip(stack));
			}, ()->{
				send("You don't have that item in your inventory.");
			});
		});
		
		cmd("eat", "<item-name>", (args)->{
			player().useItem(args[0], (stack)->{
				if(stack.item.type != ItemType.consumable){
					send("You cannot eat that item!");
					return;
				}
				
				int energy = stack.item.getInt("energy");
				int health = stack.item.getInt("health");

				send("You eat the " + stack.item.name() + ".");
				if(energy != 0)
					send((energy > 0 ? "+" : "") + energy + " Energy.");
				if(health != 0)
					send((health > 0 ? "+" : "") + health + " HP.");
				player().energy = UCore.clamp(player().energy + energy, 0, player().maxenergy);
				player().health = UCore.clamp(player().health + health, 0, player().maxhealth);

				player().inventory.remove(stack);
			}, ()->{
				send("No item with that name found.");
			});
		});
		
		cmd("attack", "<monster>", (args)->{
			if(player().attacking()){
				send("You are already in battle!");
				return;
			}
			
			String string = args[0];
			
			for(Entity type : player().location.entities){
				if(type.name.equalsIgnoreCase(string)){
					core().combat.attack(handler.getLastChat(), player(), type);
					return;
				}
			}
			
			send("You can't see a monster like that anywhere. You could -look to see the types of monsters here.");
		});
		
		
		admincmd("ivalues", "<item-type>", (args)->{
			String string = args[0];
			if(core().world.getItem(string) != null){
				send((core().world.getItem(string).values).toString());
			}else{
				send("Item type not found.");
			}
		});
		
		
		admincmd("evalues", "<entity-type>", (args)->{
			String string = args[0];
			if(core().world.getEntity(string) != null){
				send((core().world.getEntity(string).values).toString());
			}else{
				send("Entity type not found.");
			}
		});
		
		
		admincmd("objects", "", (args)->{
			for(HashMap<?, Parseable> map : core().world.objects.values())
				for(Parseable p : map.values())
					send("-" + p.name + " [" + p.getClass().getSimpleName() + "]");
		});
	}
	
	static Core core(){
		return handler.core;
	}
	
	static Player player(){
		return handler.getCurrentPlayer();
	}
	
	private static void cmd(String text, String params, Consumer<String[]> runner){
		handler.command(text, params, runner);
	}
	
	private static void cmd(String text, Consumer<String[]> runner){
		handler.command(text, "", runner);
	}
	
	private static void cmd(String text, Runnable runner){
		handler.command(text, "", (s)->{runner.run();});
	}
	
	private static void admincmd(String text, String params, Consumer<String[]> runner){
		handler.adminCommand(text, params, runner);
	}
	
	private static void send(String message){
		handler.send(message);
	}
}
