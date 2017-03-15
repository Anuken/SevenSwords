package io.anuke.sevenswords.entities;

import java.util.ArrayList;

import io.anuke.sevenswords.objects.Player;

public class Battle{
	public final EntityInstance entity;
	public final String chatid;
	public String messageid;
	public int round;
	public Thread thread;
	public boolean stopFlag;
	public ArrayList<Player> players = new ArrayList<Player>();
	
	public Battle(String chatid, Player player, EntityInstance entity){
		this.chatid = chatid;
		this.entity = entity;
		players.add(player);
	}
}
