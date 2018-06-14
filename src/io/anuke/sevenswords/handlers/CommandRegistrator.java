package io.anuke.sevenswords.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.SevenUtils;
import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.handlers.CommandHandler.Command;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.items.ItemType;
import io.anuke.sevenswords.objects.*;
import io.anuke.sevenswords.objects.Player.EquipSlot;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Strings;

public class CommandRegistrator{
	public static CommandHandler handler;

	public static void register(){

		cmd("location", () -> {
			send("*Location:* _" + player().location.name() + "_");
		});

		cmd("locations", () -> {
			send("| *Locations* |");
			for(Parseable loc : core().world.objects.get(Location.class).values()){
				send("- `" + loc.name() + "`");
			}
		});

		cmd("move", "<location>", (args) -> {
			String string = args[0];
			Parseable loc = core().world.get(string, Location.class);
			if(loc != null){
				if(loc.name.equals(string)){

					if(loc == player().location){
						send("_You are already at that location!_");
						return;
					}

					send("- Now at *" + SevenUtils.capitalize(string) + "* -");
					if(((Location) loc).entities != null){
						send("_There are_ `" + ((Location) loc).entities.length + "` _enemies here._");
					}

					player().location.players.remove(player());

					player().location = (Location) loc;

					if(player().location.players.size() > 0){
						send("_There are_ `" + player().location.players.size() + "` _players here._");
					}

					player().location.players.add(player());
					return;
				}
			}else{
				send("_Location not found. Type `-locations` for a list of locations._");
			}
		});

		cmd("look", () -> {
			send("| *" + player().location.name() + "*: enemies |");
			for(Entity type : player().location.entities){
				send("- `" + type.name() + "`");
			}

			int size = player().location.players.size() - 1;

			if(size > 0){
				send("\n| *Players* |");
				for(Player player : player().location.players){
					if(player != player())
					send("- `" + player.name() + "`");
				}
			}
		});

		cmd("equips", () -> {
			send("--*Equipment* _[" + player().name() + "]_--");
			for(int i = 0; i < player().equips.length; i++){
				send(EquipSlot.values()[i].name + ": " + (player().equips[i] == null ? "`None`" : "*" + player().equips[i].item.name() + "*"));
			}
		});

		cmd("stats", () -> {
			send("Level: `" + player().level + "`");
			send("Experience: `" + format().format(player().xp) + "`");
			send("Health: `" + player().health + "/" + player().maxhealth + "`");
			send("Energy: `" + player().energy + "/" + player().maxenergy + "`");
			send("Defense: `" + player().getDefense() + "`");
		});

		cmd("inventory", () -> {
			if(player().inventory.size() == 0){
				send("_Your inventory is empty._");
			}else{
				send("Inventory _[" + player().name() + "]_:");
				int i = 0;
				for(ItemStack item : player().inventory){
					send((i == 0 ? "``` " : " ") + "- " + item.toString() + " [" + i++ + "]");
				}
				send("```");
			}
		});

		cmd("level", () -> {
			int lxp = player().levelToXP(player().level);
			int tolevel = player().levelToXP(player().level + 1) - lxp;
			long from = (player().xp - lxp);

			String string = "";
			int len = 15;
			for(int i = 0; i < len; i++)
				string += ((float) from / tolevel * len) > i ? '█' : '░';

			send("```{|" + string + "|}```");

			send("Level: `" + player().level + "`");
			send("XP: `" + format().format((player().xp - lxp)) + "`/`" + format().format(tolevel) + "`");
		});

		cmd("help", () -> {
			send("| *Commands* |");
			for(Command command : handler.getCommandList()){
				send("-`" + command.text + "`_ " + command.params + "_");
			}
		});

		cmd("equip", "<item-name>", (args) -> {
			player().useItem(args[0], (stack) -> {
				send(player().tryEquip(stack));
			}, () -> {
				send("You don't have that item in your inventory.");
			});
		});
		
		cmd("craft", "<recipe-name>", (args) -> {
			String name = args[0];
			Recipe r = null;
			for(Parseable recipe : Core.core.world.objects.get(Recipe.class).values()){
				if(recipe.name.equals(name)){
					r = (Recipe)recipe;
					break;
				}
			}
			
			if(r == null){
				send("_No recipe with that name found._");
			}else{
				if(player().hasItems(r.requirements)){
					player().addItem(r.result);
					player().removeItems(r.requirements);
					send("_Crafted *" + r.result.amount + "x " + r.result.item.name() + "*._");
				}else{
					send("_You don't have the requirements for this recipe!_");
				}
			}
		});
		
		cmd("recipes", ()->{
			for(Recipe recipe : Core.core.world.getRecipes()){
				send("- *" + recipe.name() + "*: " + recipe.result + " _(" 
			+ SevenUtils.merge(recipe.requirements, ",") + ")_");
			}
		});
		
		cmd("examine", "<item-name>", (args) -> {
			player().useItem(args[0], (stack) -> {
				send("Item: *"+stack.item.name()+"*");
				if(stack.item.has("description"))
					send("_\"" + stack.item.get("description") + "\"_");
				if(stack.item.has("defense"))
					send("+`" + stack.item.getInt("defense") + "` Defense");
				if(stack.item.has("attack"))
					send("+`" + stack.item.getInt("attack") + "` Attack");
			}, () -> {
				send("You don't have that item in your inventory.");
			});
		});

		cmd("eat", "<item-name>", (args) -> {
			player().useItem(args[0], (stack) -> {
				if(stack.item.type != ItemType.consumable){
					send("You cannot eat that item!");
					return;
				}

				int energy = stack.item.getInt("energy");
				int health = stack.item.getInt("health");

				send("_You eat the " + stack.item.name() + "_.");
				if(energy != 0)
					send("`" + (energy > 0 ? "+" : "") + energy + "` *Energy*");
				if(health != 0)
					send("`" + (health > 0 ? "+" : "") + health + "` *HP*");
				player().energy = Mathf.clamp(player().energy + energy, 0, player().maxenergy);
				player().health = Mathf.clamp(player().health + health, 0, player().maxhealth);

				player().inventory.remove(stack);
			}, () -> {
				send("No item with that name found.");
			});
		});

		cmd("attack", "<enemy>", (args) -> {
			if(player().attacking()){
				send("You are already in battle! Use -leave to stop the fight.");
				return;
			}

			String string = args[0];

			for(Entity type : player().location.entities){
				if(type.name.equalsIgnoreCase(string)){
					core().combat.beginBattle(handler.getLastChat(), player(), type, 1);
					return;
				}
			}

			send("You can't see an enemy like that anywhere. You could -look to see the types of enemies here.");
		});
		
		cmd("attack-amount", "<enemy> <amount>", (args) -> {
			if(player().attacking()){
				send("You are already in battle! Use -leave to stop the fight.");
				return;
			}
			
			if(!Strings.canParsePostiveInt(args[1])){
				send("Amount must be a positive number!");
				return;
			}
			int amount = Strings.parseInt(args[1]);

			String string = args[0];

			for(Entity type : player().location.entities){
				if(type.name.equalsIgnoreCase(string)){
					core().combat.beginBattle(handler.getLastChat(), player(), type, amount);
					return;
				}
			}

			send("You can't see an enemy like that anywhere. You could -look to see the types of enemies here.");
		});
		
		cmd("join", "<player>", (args) -> {
			if(player().attacking()){
				send("You are already in battle! Use -leave to stop the fight.");
				return;
			}

			String string = args[0];

			for(Player player : player().location.players){
				if(player.name().equalsIgnoreCase(string)){
					if(player.attacking()){
						send("Joining fight with _" + player.name() + "_...");
						core().combat.joinBattle(handler.getLastChat(), player(), player);
					}else{
						send("That player is not in battle!");
					}
					return;
				}
			}

			send("You can't see that player here.");
		});

		cmd("leave", () -> {
			if(!player().attacking()){
				send("You are not in battle!");
			}else{
				core().combat.leaveBattle(player());
			}
		});

		admincmd("adminhelp", "", (args) -> {
			send("| *Admin Commands* |");
			for(Command command : handler.getAdminCommandList()){
				send("-`" + command.text + "`_ " + command.params + "_");
			}
		});
		
		admincmd("restart", "", (args) -> {
			send("*This command is disabled.*");
		});

		admincmd("spawn", "<item-type>", (args) -> {
			Item item = core().world.getItem(args[0]);
			if(item != null){
				player().addItem(new ItemStack(item));
				send("Spawning in `1x " + item.uncappedName() + "`.");
			}else{
				send("Item type not found.");
			}
		});

		admincmd("getitem", "<item-type>", (args) -> {
			String string = args[0];
			if(core().world.getItem(string) != null){
				send((core().world.getItem(string).values).toString());
			}else{
				send("Item type not found.");
			}
		});

		admincmd("getentity", "<entity-type>", (args) -> {
			String string = args[0];
			if(core().world.getEntity(string) != null){
				send((core().world.getEntity(string).values).toString());
			}else{
				send("Entity type not found.");
			}
		});

		admincmd("objects", "", (args) -> {
			for(HashMap<?, Parseable> map : core().world.objects.values())
				for(Parseable p : map.values())
					send("-" + p.name.replace("_", " ") + " (`" + p.getClass().getSimpleName() + "`)");
		});

		admincmd("reload", "", (args) -> {
			core().world.reload();
			send("Reloaded world succesfully.");
		});

		admincmd("sendobject", "<type>", (args) -> {
			String type = args[0];
			handler.filetype = core().world.getClass(type, type);
			if(handler.filetype != null){
				handler.waitingForFile = true;
				send("Ready to recieve object.");
			}else{
				send("Invalid object type.");
			}
		});

		admincmd("deleteobject", "<type> <name>", (args) -> {
			String type = args[0];
			Class<Parseable> c = core().world.getClass(type, args[1]);
			if(c != null){
				Parseable p = core().world.get(args[1], c);
				if(p == null){
					send("No object with that name found.");
				}else{
					try{
						Files.copy(p.path, Paths.get("trash", p.path.getFileName() + "-" + (int) (Math.random() * 999999)));
						Files.delete(p.path);
						send("Object \"" + p.name + "\" deleted.");
						core().world.reload();
					}catch(Exception e){
						e.printStackTrace();
						send("Error deleting file.");
					}
				}
			}else{
				send("Invalid object type.");
			}
		});

		admincmd("getobject", "<type> <name>", (args) -> {
			String type = args[0];
			Class<Parseable> c = core().world.getClass(type, args[1]);
			if(c != null){
				Parseable p = core().world.get(args[1], c.asSubclass(Parseable.class));
				if(p == null){
					send("No object with that name found.");
				}else{
					try{
						Stream<String> stream = Files.lines(p.path);
						stream.forEach((String line) -> {
							send(line);
						});
						stream.close();
					}catch(Exception e){
						e.printStackTrace();
						send("Error reading lines.");
					}
				}
			}else{
				send("Invalid object type.");
			}
		});

		admincmd("set", "<type> <name> <value-name> <value>", (args) -> {
			String type = args[0];
			Class<Parseable> c = core().world.getClass(type, args[1]);
			if(c != null){
				Parseable p = core().world.get(args[1], c.asSubclass(Parseable.class));
				if(p == null){
					send("No object with that name found.");
				}else{
					String valuename = args[2];
					String value = args[3];
					try{
						if(p.values.containsKey(valuename)){
							StringBuilder lines = new StringBuilder();
							Stream<String> stream = Files.lines(p.path);
							stream.forEach((String line) -> {
								if(line.toLowerCase().startsWith(valuename)){
									line = valuename + ": " + value;
								}
								lines.append(line + "\n");
							});
							stream.close();
							Files.write(p.path, lines.toString().getBytes());
						}else{
							Files.write(p.path, ("\n" + valuename + ": " + value).getBytes(), StandardOpenOption.APPEND);
						}
						try{
							core().world.createObject(c, p.path);
							core().world.reload();
							send("Value \"`" + valuename + "`\" set to `" + value + "`.");
						}catch(Exception e){
							send("Error parsing input: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
							e.printStackTrace();
						}
					}catch(IOException e){
						e.printStackTrace();
						send("Error writing to file.");
					}
				}
			}else{
				send("Invalid object type.");
			}
		});

		admincmd("unset", "<type> <name> <value-name>", (args) -> {
			String type = args[0];
			Class<Parseable> c = core().world.getClass(type, args[1]);
			if(c != null){
				Parseable p = core().world.get(args[1], c.asSubclass(Parseable.class));
				if(p == null){
					send("No object with that name found.");
				}else{
					String valuename = args[2];
					try{
						if(p.values.containsKey(valuename)){
							StringBuilder lines = new StringBuilder();
							Stream<String> stream = Files.lines(p.path);
							stream.forEach((String line) -> {
								if(!line.toLowerCase().startsWith(valuename)){
									lines.append(line + "\n");
								}
							});
							stream.close();
							Files.write(p.path, lines.toString().getBytes());
						}

						try{
							core().world.createObject(c, p.path);
							core().world.reload();
							send("Value \"`" + valuename + "`\" removed.");
						}catch(Exception e){
							send("Error parsing input: " + e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
							e.printStackTrace();
						}
					}catch(IOException e){
						e.printStackTrace();
						send("Error writing to file.");
					}
				}
			}else{
				send("Invalid object type.");
			}
		});
	}

	static NumberFormat format(){
		return NumberFormat.getNumberInstance(Locale.US);
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
		handler.command(text, "", (s) -> {
			runner.run();
		});
	}

	private static void admincmd(String text, String params, Consumer<String[]> runner){
		handler.adminCommand(text, params, runner);
	}

	private static void send(String message){
		handler.send(message);
	}
}
