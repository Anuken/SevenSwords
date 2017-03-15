package io.anuke.sevenswords.handlers;

import java.util.ArrayList;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.SevenUtils;
import io.anuke.sevenswords.entities.Battle;
import io.anuke.sevenswords.entities.EntityInstance;
import io.anuke.sevenswords.items.ItemStack;
import io.anuke.sevenswords.objects.Entity;
import io.anuke.sevenswords.objects.Player;

public class CombatHandler extends Handler{
	public static final int roundtime = 10000;

	public CombatHandler(Core world) {
		super(world);
	}

	public void beginBattle(String chatid, Player player, Entity type){
		player.battle = new Battle(chatid, player, new EntityInstance(type));

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
		for(Player player : battle.players){
			ArrayList<ItemStack> drops = player.battle.entity.generateDrops();
			message.append("\n_" + player.name() + " is victorious!_" + (drops.size() == 0 ? "" : "\n\nDrops: ``` "));

			int i = 0;
			for(ItemStack stack : drops){
				if(i++ != 0)
					message.append("\n ");
				message.append("- " + stack);
			}

			player.addItems(drops);

			message.append("```\n+`" + player.battle.entity.type.exp + "` XP");
			player.addXP(player.battle.entity.type.exp);

			player.energy -= 5;
			player.battle = null;
		}
	}

	private void runDefeat(Player player, StringBuilder message){
		message.append("\n`" + player.name() + " has died.`");
		message.append("\nEnergy depleted, health set to 50.");

		player.energy = 5;
		player.health = 50;
		player.battle = null;
		player.battle.players.remove(player);
	}

	private boolean runRound(Battle battle){
		StringBuilder message = new StringBuilder("``` [Round " + ++battle.round + "] ```");

		boolean finished = true;

		EntityInstance entity = battle.entity;

		String playerstr = SevenUtils.merge(battle.players, ", ");

		message.append("--*" + playerstr + "*  |  *" + entity.type.name() + "*--\n");
		
		int i = 0;
		
		for(Player player : battle.players){
			
			if(i ++ != 0)
				message.append("\n\n");
			
			int playerDamage = player.getAttack();

			int playerDamaged = entity.type.attack - player.getDefense();
			int enemyDamaged = playerDamage - entity.type.defence;

			playerDamaged = Math.max(playerDamaged, 0);
			enemyDamaged = Math.max(enemyDamaged, 0);

			player.health -= playerDamaged;
			entity.health -= enemyDamaged;

			if(player.health <= 0){
				runDefeat(player, message);
			}else if(entity.health <= 0){
				runVictory(battle, message);
				finished = true;
			}else{
				message.append("\n_" + player.name() + " hit " + entity.type.uncappedName() + " for " + enemyDamaged + " damage!" + (entity.type.defence == 0 ? "" : "_ *( ⛨ " + Math.min(entity.type.defence, playerDamage) + ")*"));
				message.append("\n" + entity.type.name() + " hit " + player.name() + " for " + playerDamaged + " damage!" + (player.getDefense() == 0 ? "" : "_ *( ⛨ " + Math.min(player.getDefense(), entity.type.attack) + ")* "));
				message.append("\n");
				message.append("\nHealth: `" + player.health + "`");
				finished = false;
				
				if(i == battle.players.size())
					message.append("\nEnemy Health: `" + entity.health + "`");
			}
			
			
		}

		String string = message.toString();

		if(battle.messageid == null){
			battle.messageid = Core.core.messages.sendRaw(string, battle.chatid);
		}else{
			Core.core.messages.edit(string, battle.chatid, battle.messageid);
		}

		return finished;
	}
}
