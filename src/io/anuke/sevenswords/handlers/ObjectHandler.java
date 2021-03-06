package io.anuke.sevenswords.handlers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.*;

public class ObjectHandler extends Handler{
	public static final String DEFAULT_LOCATION = "farmlands";
	
	public final Path basePath = Paths.get(System.getProperty("user.home"), "/SevenSwords/assets");
	public HashMap<Class<? extends Parseable>, HashMap<String, Parseable>> objects = new HashMap<Class<? extends Parseable>, HashMap<String, Parseable>>();
	
	public <T extends Parseable> T get(String name, Class<T> c){
		return c.cast(objects.get(c).get(name));
	}
	
	public Class<Parseable> getClass(String type, String name){
		try{
			return (Class<Parseable>)Class.forName("io.anuke.sevenswords.objects." + type.substring(0, 1).toUpperCase() + type.substring(1));
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public Item getItem(String name){
		return get(name, Item.class);
	}
	
	public Entity getEntity(String name){
		return get(name, Entity.class);
	}
	
	public Location getLocation(String name){
		return get(name, Location.class);
	}
	
	public Collection<Recipe> getRecipes(){
		return (Collection<Recipe>)(Collection)objects.get(Recipe.class).values();
	}
	
	public void reload(){
		objects.clear();
		load();
		for(Player p : core.players.values()){
			p.location = getLocation(p.location.name) == null ? getLocation("default") : getLocation(p.location.name);
			p.location.players.add(p);
			for(ItemStack stack : p.inventory){
				stack.item = getItem(stack.item.name);
			}
		}
	}

	public void load(){
		try{
			loadType(Item.class);
			loadType(Entity.class);
			loadType(Location.class);
			loadType(Recipe.class);
		}catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Path getPath(Class<?> c){
		String name = c.getSimpleName().toLowerCase();
		name = name.endsWith("y") ? name.substring(0, name.length()-1) + "ies" : name + "s";
		return basePath.resolve(name);
	}

	private void add(Parseable object){
		if(!objects.containsKey(object.getClass()))
			objects.put(object.getClass(), new HashMap<String, Parseable>());
			
		objects.get(object.getClass()).put(object.name, object);
	}
	
	private <T extends Parseable> void loadType(Class<T> type) throws Exception{
		Files.walk(basePath.resolve(getPath(type))).forEach((Path file) -> {
			if(Files.isRegularFile(file)){
				System.out.println("Loading "+type.getSimpleName().toLowerCase()+" " + "\"" + file.getFileName() + "\"...");
				try{
					loadObject(type, file);
				}catch(Exception e){
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	public <T extends Parseable> T createObject(Class<T> c, Path path) throws Exception{
		HashMap<String, String> values = loadValues(path);
		T object = c.newInstance();
		object.path = path;
		object.parse(values);
		object.name = values.get("name");
		if(object.name ==null) throw new RuntimeException("No name defined for object: " + path.toString()+"");
		object.values = values;
		return object;
	}

	private <T extends Parseable> T loadObject(Class<T> c, Path path) throws Exception{
		T object = createObject(c, path);
		add(object);
		return object;
	}

	public HashMap<String, String> loadValues(Path path){
		HashMap<String, String> values = new HashMap<String, String>();

		List<String> strings = null;

		try{
			strings = Files.readAllLines(path);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}

		int linenumber = 0;
		for(String line : strings){
			linenumber ++;
			if(line.length() == 0) continue;
			line = line.replace(" ", "").toLowerCase();
			String[] split = line.split(":");

			if(split.length != 2) throw new RuntimeException("Error parsing line " + linenumber + ": \"" + line + "\": invalid statement.");
			values.put(split[0], split[1]);
		}

		return values;
	}

	public ObjectHandler(Core core){
		super(core);
	}
}
