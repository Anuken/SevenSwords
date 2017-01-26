package io.anuke.sevenswords.entities;

import io.anuke.sevenswords.objects.Player;

public class Battle{
	public final EntityInstance entity;
	public final String chatid;
	public String messageid;
	public int round;
	public Thread thread;
	public boolean stopFlag;
	
	public Battle(String chatid, Player player, Thread thread, EntityInstance entity){
		this.chatid = chatid;
		this.entity = entity;
		this.thread = thread;
	}
}
