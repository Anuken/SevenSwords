package io.anuke.sevenswords.entities;

import io.anuke.sevenswords.objects.Player;

public class Battle{
	public final EntityInstance entity;
	public final String chatid;
	public String messageid;
	public int round;
	
	public Battle(String chatid, Player player, EntityInstance entity){
		this.chatid = chatid;
		this.entity = entity;
	}
}
