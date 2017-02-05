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
	
	public void join(String chatid, Player player, Player other){
		Thread thread = new Thread(new AttackTask(player));
		player.battle = other.battle;
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
					Core.core.messages.edit("Battle ended prematurely - "+player.name()+" has left the fight.", player.battle.chatid, player.battle.messageid);
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
		message.append("\n_" + player.name() + " is victorious!_" + (drops.size() == 0 ? "" : "\n\nDrops: ``` "));
		
		int i = 0;
		for(ItemStack stack : drops){
			if(i++ != 0) message.append("\n ");
			message.append("- " + stack);
		}
		
		player.addItems(drops);
		
		message.append("```\n+`" + player.battle.entity.type.exp + "` XP");
		player.addXP(player.battle.entity.type.exp);
	}

	private void runDefeat(Player player, StringBuilder message){
		message.append("\n`"+player.name()+" has died.`");
		message.append("\nEnergy depleted.");
		
		player.energy = 5;
		player.health = 5;
	}

	private boolean runRound(Player player){
		StringBuilder message = new StringBuilder("``` [Round " + ++player.battle.round + "] ```");
		
		boolean finished = false;

		EntityInstance entity = player.battle.entity;
		
		message.append("--*"+player.name() + "*  |  *" + entity.type.name() + "*--\n");
		
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
			message.append("\n_You hit " + entity.type.uncappedName() + " for " + enemyDamaged + " damage!" + (entity.type.defence == 0 ? "" : "_ *( ⛨ " + Math.min(entity.type.defence, playerDamage) + ")*"));
			message.append("\n" + entity.type.name() + " hit you for " + playerDamaged + " damage!" + (player.getDefense() == 0 ? "" : "_ *( ⛨ " + Math.min(player.getDefense(), entity.type.attack)+")* "));
			message.append("\n");
			message.append("\nHealth: `" + player.health + "`");
			message.append("\nEnemy Health: `" + entity.health + "`");
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
