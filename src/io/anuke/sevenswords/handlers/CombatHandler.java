package io.anuke.sevenswords.handlers;

import java.util.ArrayList;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Battle;
import io.anuke.sevenswords.entities.EntityInstance;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.Entity;
import io.anuke.sevenswords.objects.Player;

public class CombatHandler extends Handler{
	public final int roundtime = 7000;

	public CombatHandler(Core world){
		super(world);
	}

	public void attack(String chatid, Player player, Entity type){
		Thread thread = new Thread(new AttackTask(player));
		player.battle = new Battle(chatid, player, thread, new EntityInstance(type));
		thread.setDaemon(true);
		thread.start();
	}
	
	public void stopBattle(Player player){
		player.battle.stopFlag = true;
		player.battle.thread.interrupt();
	}

	private class AttackTask implements Runnable{
		Player player;

		public AttackTask(Player player){
			this.player = player;
		}

		@Override
		public void run(){
			while(true){
				if(player.battle.stopFlag){
					Core.core.messages.edit("Battle ended prematurely - you have left the fight.", player.battle.chatid, player.battle.messageid);
					player.battle = null;
					break;
				}
				
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
		message.append("\nYou have killed the " + player.battle.entity.type.uncappedName() + (drops.size() == 0 ? "." : ".\nLoot:"));

		for(ItemStack stack : drops){
			message.append("\n- " + stack);
		}

		player.addItems(drops);
		
		message.append("\n+" + player.battle.entity.type.exp + " EXP.");
		player.addXP(player.battle.entity.type.exp);
	}

	private void runDefeat(Player player, StringBuilder message){
		message.append("\nYou have died.");

	}

	private boolean runRound(Player player){
		StringBuilder message = new StringBuilder("Round " + ++player.battle.round + ".");
		boolean finished = false;

		EntityInstance entity = player.battle.entity;
		
		int playerDamage = player.getAttack();

		int playerDamaged = entity.type.attack - player.getDefense();
		int enemyDamaged = playerDamage- entity.type.defence;

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
			message.append("\nYou hit " + entity.type.name() + " for " + enemyDamaged + " damage!" + (entity.type.defence == 0 ? "" : " ( Blocked " + Math.min(entity.type.defence, playerDamage) + " damage.)"));
			message.append("\n" + entity.type.name() + " hit you for " + playerDamaged + " damage!" + (player.getDefense() == 0 ? "" : " ( Blocked " + Math.min(player.getDefense(), entity.type.attack) + " damage.)"));
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
