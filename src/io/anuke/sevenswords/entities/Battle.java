package io.anuke.sevenswords.entities;

import java.util.concurrent.CopyOnWriteArrayList;

import io.anuke.sevenswords.objects.Player;

public class Battle{
	public final String chatid;
	public EntityInstance entity;
	public String messageid;
	public int round;
	public Thread thread;
	public boolean stopFlag;
	public CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();
	public int loops;
	public int index;
	
	public Battle(String chatid, Player player, EntityInstance entity, int loops){
		this.chatid = chatid;
		this.entity = entity;
		this.loops = loops;
		players.add(player);
	}
}
