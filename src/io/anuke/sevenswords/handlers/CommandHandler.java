package io.anuke.sevenswords.handlers;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.Entity;
import io.anuke.sevenswords.objects.Location;
import io.anuke.sevenswords.objects.Player;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import net.pixelstatic.utils.MiscUtils;
import net.pixelstatic.utils.bots.MessageHandler.MessageListener;

public class CommandHandler extends Handler implements MessageListener{
	private String lastid;
	private boolean waitingForFile;
	private Class<?> filetype;

	public CommandHandler(Core world){
		super(world);
	}

	@Override
	public void onMessageRecieved(String message, String username, String chatid, String userid, String messageid){

		if(waitingForFile && (username.equals("TheRealTux") || username.equals("Anuken"))){
			lastid = chatid;
			if(message != null) fileRecieved(message);
		}

		if(message == null || ( !message.startsWith("-"))) return;

		message = message.substring(1);
		message = message.toLowerCase();

		lastid = chatid;

		String[] args = message.split(" ");
		message = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);

		handleCommand(chatid, username, message, args, userid);
	}

	private void fileRecieved(String string){
		send("Recieved object, parsing...");
		Path path = Paths.get("downloads", "temp");
		try{
			Files.write(path, string.getBytes());
			Parseable p = core.world.createObject(filetype.asSubclass(Parseable.class), path);
			if(p.name == null){
				throw new RuntimeException("The object must have a name!");
			}
			Files.copy(path, core.world.getPath(filetype).resolve(p.name), StandardCopyOption.REPLACE_EXISTING);
			core.world.objects.clear();
			core.world.load();
			send("Object \"" + p.name + "\" parsed successfully.");
		}catch(Exception e){
			send("Error parsing input: " + e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
			e.printStackTrace();
		}
		waitingForFile = false;
	}

	private void handleCommand(String chatid, String username, String message, String[] args, String userid){
		Player player = core.getPlayer(userid);

		if(username.equals("TheRealTux") || username.equals("Anuken")){
			if( !waitingForFile && message.equals("sendobject")){
				if(args.length == 1){
					String type = args[0];
					filetype = core.world.getClass(type, type);
					if(filetype != null){
						waitingForFile = true;
						send("Ready to recieve object.");
					}else{
						send("Invalid object type.");
					}
				}else{
					send("Usage: -sendobject <type>");
				}
			}else if(message.equals("deleteobject")){
				if(args.length == 2){
					String type = args[0];
					Class<Parseable> c = core.world.getClass(type, args[1]);
					if(c != null){
						Parseable p = core.world.get(args[1], c);
						if(p == null){
							send("No object with that name found.");
						}else{
							try{
								Files.copy(p.path, Paths.get("trash", p.path.getFileName() + "-" + (int)(Math.random() * 999999)));
								Files.delete(p.path);
								send("Object \"" + p.name + "\" deleted.");
								core.world.reload();
							}catch(Exception e){
								e.printStackTrace();
								send("Error deleting file.");
							}
						}
					}else{
						send("Invalid object type.");
					}
				}else{
					send("Usage: -deleteobject <type> <name>");
				}
			}else if(message.equals("fetchobject")){
				if(args.length == 2){
					String type = args[0];
					Class<Parseable> c = core.world.getClass(type, args[1]);
					if(c != null){
						Parseable p = core.world.get(args[1], c.asSubclass(Parseable.class));
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
				}else{
					send("Usage: -fetchobject <type> <name>");
				}
			}else if(message.equals("setvalue")){
				if(args.length == 4){
					String type = args[0];
					Class<Parseable> c = core.world.getClass(type, args[1]);
					if(c != null){
						Parseable p = core.world.get(args[1], c.asSubclass(Parseable.class));
						if(p == null){
							send("No object with that name found.");
						}else{
							String valuename = args[2];
							String value = args[3];
							try{
								if(p.values.containsKey(valuename)){
									//String lines = "";
									StringBuilder lines = new StringBuilder();
									Stream<String> stream = Files.lines(p.path);
									stream.forEach((String line)-> {
										if(line.toLowerCase().startsWith(valuename)){
											line = valuename + ": " + value;
										}
										lines.append(line + "\n");
									});
									stream.close();
									Files.write(p.path, lines.toString().getBytes());
								}else{
									Files.write(p.path, ("\n"+valuename + ": " + value).getBytes(), StandardOpenOption.APPEND);
								}
								core.world.reload();
								send("Value \"" + valuename + "\" set to " + value + ".");
							}catch(IOException e){
								e.printStackTrace();
								send("Error writing to file.");
							}
						}
					}else{
						send("Invalid object type.");
					}
				}else{
					send("Usage: -setvalue <type> <name> <value name> <value>");
				}
			}
		}

		if(message.equals("location")){
			send("Location: " + player.location.name());
		}else if(message.equals("locations")){
			send("Locations: ");
			for(Parseable loc : core.world.objects.get(Location.class).values()){
				send("- " + loc.name());
			}
		}else if(message.equals("move")){
			if(args.length > 0){
				String string = args[0];
				Parseable loc = core.world.get(string, Location.class);
				if(loc != null){
					if(loc.name.equals(string)){

						if(loc == player.location){
							send("You already are at that location.");
							return;
						}

						send("Moving to " + MiscUtils.capitalize(string) + ".");
						player.location = (Location)loc;
						return;
					}
				}else{
					send("Location not found. Type -locations for a list of locations.");
				}
			}else{
				send("Usage: -move [location].");
			}
		}else if(message.equals("look")){
			send("Monsters: ");
			for(Entity type : player.location.entities){
				send("- " + type.name());
			}
		}else if(message.equals("stats")){
			send("Level " + player.level + ".");
			send(player.xp + " XP.");
			send("Health: " + player.health + "/" + player.maxhealth);
			send("Energy: " + player.energy + "/" + player.maxenergy);
		}else if(message.equals("inventory")){
			if(player.inventory.size() == 0){
				send("Your inventory is empty.");
			}else{
				send("Inventory: ");
				for(ItemStack item : player.inventory){
					send("- " + item.toString());
				}
			}
		}else if(message.equals("help")){
			send("Commands: - [look/location/move/stats/locations/attack/inventory]");
		}else if(message.equals("ivalues")){
			if(args.length > 0){
				String string = args[0];
				if(core.world.getItem(string) != null){
					send((core.world.getItem(string).values).toString());
				}else{
					send("Item type not found.");
				}
			}else{
				send("Usage: -ivalues [item type].");
			}
		}else if(message.equals("evalues")){
			if(args.length > 0){
				String string = args[0];
				if(core.world.getEntity(string) != null){
					send((core.world.getEntity(string).values).toString());
				}else{
					send("Entity type not found.");
				}
			}else{
				send("Usage: -evalues [entity type].");
			}
		}else if(message.equals("levels")){
			for(int i = 1;i < 100;i += 5){
				int xp = (int)(Math.pow(i, 1.5 + i * 0.005) * 100);
				send("Level " + i + ": " + xp + " XP");
			}
		}else if(message.equals("objects")){
			for(HashMap<?, Parseable> map : core.world.objects.values())
				for(Parseable p : map.values())
					send("-" + p.name + " [" + p.getClass().getSimpleName() + "]");

		}else if(message.equals("attack")){
			if(player.attacking()){
				send("You are already in battle!");
				return;
			}
			if(args.length > 0){
				String string = args[0];
				for(Entity type : player.location.entities){
					if(type.name.equalsIgnoreCase(string)){
						core.combat.attack(chatid, player, type);
						return;
					}
				}
				send("You can't see a monster like that anywhere. You could -look to see the types of monsters here.");
			}else{
				send("Usage: -attack [monster].");
			}
		}
	}

	public void send(String message){
		core.messages.send(message, lastid);
	}

}
