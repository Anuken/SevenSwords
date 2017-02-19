package io.anuke.sevenswords;

import java.util.HashMap;

import io.anuke.sevenswords.handlers.CombatHandler;
import io.anuke.sevenswords.handlers.CommandHandler;
import io.anuke.sevenswords.handlers.ObjectHandler;
import io.anuke.sevenswords.objects.Player;
import io.anuke.ucore.bots.MessageHandler.TimedMessageHandler;

public class Core{
	public static Core core;
	public TimedMessageHandler messages;
	public CommandHandler commands;
	public ObjectHandler world;
	public CombatHandler combat;
	public HashMap<String, Player> players = new HashMap<String, Player>();
	
	public Core(TimedMessageHandler handler){
		core = this;
		this.messages = handler;
		
		commands = new CommandHandler(this);
		world = new ObjectHandler(this);
		combat = new CombatHandler(this);
		
		world.load();
		
		handler.setMessageListener(commands);
	}
	
	public Player getPlayer(String id){
		Player player = players.get(id);
		if(player == null){
			player = new Player(id, world.getLocation("default"));
			players.put(id, player);
		}
		return player;
	}

}
