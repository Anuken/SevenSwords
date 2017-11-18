package io.anuke.sevenswords.handlers;

import java.util.ArrayList;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Battle;
import io.anuke.sevenswords.entities.EntityInstance;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.Entity;
import io.anuke.sevenswords.objects.Player;

public class CombatHandler extends Handler{
	public static final int roundtime = 2000;

	public CombatHandler(Core world) {
		super(world);
	}

	public void beginBattle(String chatid, Player player, Entity type, int loops){
		player.battle = new Battle(chatid, player, new EntityInstance(type), loops);

		Thread thread = new Thread(new BattleTask(player.battle));
		player.battle.thread = thread;
		thread.setDaemon(true);
		thread.start();
	}

	public void joinBattle(String chatid, Player player, Player other){
		player.battle = other.battle;
		player.battle.players.add(player);
	}

	public void cleanupBattle(Battle battle){
		battle.stopFlag = true;
		battle.thread.interrupt();

		for(Player player : battle.players){
			player.battle = null;
		}
	}
	
	public void leaveBattle(Player player){
		player.battle.players.remove(player);
		
		if(player.battle.players.size() == 0){
			cleanupBattle(player.battle);
		}
		
		Core.core.messages.send(player.name() + " has left the fight.", player.battle.chatid);
		
		player.battle = null;
	}

	private class BattleTask implements Runnable{
		Battle battle;

		public BattleTask(Battle battle) {
			this.battle = battle;
		}

		@Override
		public void run(){
			while(true){
				if(battle.stopFlag){
					Core.core.messages.edit("(_Battle ended prematurely._)", battle.chatid, battle.messageid);
					break;
				}

				if(runRound(battle)){
					break;
				}

				try{
					Thread.sleep(roundtime);
				}catch(Exception e){
				}
			}
		}
	}

	private void runVictory(Battle battle, StringBuilder message){
		
		int s = 0;
		for(Player player : battle.players){
			if(s ++ != 0){
				message.append("\n\n*|========================|*\n");
			}
			
			ArrayList<ItemStack> drops = battle.entity.generateDrops();
			message.append("\n_" + player.name() + " is victorious!_");
			
			if(drops.size() > 0){
				message.append("\n\nDrops: ``` ");
			}
			
			int i = 0;
			for(ItemStack stack : drops){
				if(i++ != 0)
					message.append("\n ");
				message.append("- " + stack);
			}

			player.addItems(drops);

			message.append((drops.size() != 0 ? "```" : "" )+ "\n+`" 
					+ battle.entity.type.exp + "` XP");
			
			player.addXP(battle.entity.type.exp);

			player.energy -= 5;
			if(battle.index >= battle.loops)
				player.battle = null;
		}
	}

	private void runDefeat(Player player, StringBuilder message, boolean energy){
		if(!energy){
			message.append("\n`" + player.name() + " has died!`");
		}else{
			message.append("\n`" + player.name() + " has exhausted their energy and died.`");
		}
		message.append("\nHealth set to 50.");

		player.energy = player.maxenergy;
		player.health = 50;
		player.battle.players.remove(player);
		player.battle = null;
	}

	private boolean runRound(Battle battle){
		StringBuilder message = new StringBuilder("``` [Turn " + ++battle.round + "] ");
		
		if(battle.loops > 0){
			message.append(" | Battle " + (battle.index + 1)  + "/" + battle.loops);
		}
		
		EntityInstance entity = battle.entity;
		
		message.append("```\n");

		boolean finished = true;
		
		for(Player player : battle.players){
			int playerDamage = player.getAttack();

			int playerDamaged = entity.type.attack - player.getDefense();
			int enemyDamaged = playerDamage - entity.type.defence;

			playerDamaged = Math.max(playerDamaged, 0);
			enemyDamaged = Math.max(enemyDamaged, 0);

			player.health -= playerDamaged;
			entity.health -= enemyDamaged;
		}
		
		for(Player player : battle.players){
			message.append("*" + player.name() + " *`[ " + Math.max(player.health, 0) + "/"+ player.maxhealth + " ]`\n");
		}
		
		message.append("\n_ -- VS --_\n\n");
		message.append("*" + entity.type.name() + " *`[ " + Math.max(entity.health, 0) + "/"+ entity.type.health + " ]`\n");
		
		int i = 0;
		
		boolean allDead = true;
		
		for(Player player : battle.players){
			
			if(i ++ != 0)
				message.append("\n\n");
			
			int playerDamage = player.getAttack();

			int playerDamaged = entity.type.attack - player.getDefense();
			int enemyDamaged = playerDamage - entity.type.defence;

			playerDamaged = Math.max(playerDamaged, 0);
			enemyDamaged = Math.max(enemyDamaged, 0);

			if(player.health <= 0){
				runDefeat(player, message, false);
			}else if(player.energy <= 0){
				runDefeat(player, message, true);
			}else if(entity.health <= 0){
				runVictory(battle, message);
				finished = true;
				allDead = false;
				break;
			}else{
				allDead = false;
				message.append("\n_" + player.name() + " hit " + entity.type.uncappedName() + " for " + enemyDamaged + " damage!" 
						+ (entity.type.defence == 0 ? "_" : "_ *( ⛨ " + Math.min(entity.type.defence, playerDamage) + ")*"));
				message.append("\n_" + entity.type.name() + " hit " + player.name() + " for " + playerDamaged + " damage!" 
						+ (player.getDefense() == 0 ? "_" : "_ *( ⛨ " + Math.min(player.getDefense(), entity.type.attack) + ")* "));
				message.append("\n");
				finished = false;
			}
		}

		String string = message.toString();

		if(battle.messageid == null){
			battle.messageid = Core.core.messages.sendRaw(string, battle.chatid);
		}else{
			Core.core.messages.edit(string, battle.chatid, battle.messageid);
		}
		
		if(allDead){
			return true;
		}
		
		if(finished){
			if(battle.loops > 1 && battle.index < battle.loops - 1){
				battle.index ++;
				battle.entity = new EntityInstance(battle.entity.type);
				return false;
			}else{
				return true;
			}
		}else{
			return false;
		}
	}
}
