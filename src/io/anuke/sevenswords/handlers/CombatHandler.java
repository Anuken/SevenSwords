package io.anuke.sevenswords.handlers;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Battle;
import io.anuke.sevenswords.entities.EntityInstance;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.Entity;
import io.anuke.sevenswords.objects.Player;

import java.util.ArrayList;

public class CombatHandler extends Handler{
	public final int roundtime = 7000;

	public CombatHandler(Core world){
		super(world);
	}

	public void attack(String chatid, Player player, Entity type){
		player.battle = new Battle(chatid, player, new EntityInstance(type));
		Thread thread = new Thread(new AttackTask(player));
		thread.setDaemon(true);
		thread.start();
	}

	private class AttackTask implements Runnable{
		Player player;

		public AttackTask(Player player){
			this.player = player;
		}

		@Override
		public void run(){
			while(true){
				if(runRound(player)){
					player.battle = null;
					player.energy -= 5;
					break;
				}
				try{
					Thread.sleep(roundtime);
				}catch(Exception e){
				}
			}
		}
	}

	private void runVictory(Player player, StringBuilder message){
		ArrayList<ItemStack> drops = player.battle.entity.generateDrops();
		message.append("\nYou have killed the " + player.battle.entity.type.name() + (drops.size() == 0 ? "!" : "!\nLoot:"));

		for(ItemStack stack : drops){
			message.append("\n- " + stack);
		}

		player.addItems(drops);
		
		message.append("\n+" + player.battle.entity.type.exp + " EXP.");
		player.xp += player.battle.entity.type.exp;
	}

	private void runDefeat(Player player, StringBuilder message){
		message.append("\nYou have died.");

	}

	private boolean runRound(Player player){
		StringBuilder message = new StringBuilder("Round " + ++player.battle.round + ".");
		boolean finished = false;

		EntityInstance entity = player.battle.entity;

		int playerDamaged = entity.type.attack - player.getDefense();
		int enemyDamaged = player.getAttack() - entity.type.defence;

		playerDamaged = Math.max(playerDamaged, 0);
		enemyDamaged = Math.max(enemyDamaged, 0);

		player.health -= playerDamaged;
		entity.health -= enemyDamaged;

		if(player.health <= 0){
			runDefeat(player, message);
			finished = true;
		}else if(entity.health <= 0){
			runVictory(player, message);
			finished = true;
		}else{
			message.append("\nYou hit " + entity.type.name() + " for " + enemyDamaged + " damage!" + (entity.type.defence == 0 ? "" : " ( Blocked " + entity.type.defence + " damage.)"));
			message.append("\n" + entity.type.name() + " hit you for " + playerDamaged + " damage!" + (player.getDefense() == 0 ? "" : " ( Blocked " + player.getDefense() + " damage.)"));
			message.append("\n");
			message.append("\nHealth: " + player.health);
			message.append("\nEnemy Health: " + entity.health);
		}

		String string = message.toString();

		if(player.battle.messageid == null){
			player.battle.messageid = Core.core.messages.sendRaw(string, player.battle.chatid);
		}else{
			Core.core.messages.edit(string, player.battle.chatid, player.battle.messageid);
		}
		return finished;
	}
}
