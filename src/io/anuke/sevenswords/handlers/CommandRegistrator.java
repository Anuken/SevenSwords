package io.anuke.sevenswords.handlers;

import java.util.function.Consumer;

import io.anuke.sevenswords.objects.Player;

public class CommandRegistrator{
	public static CommandHandler handler;
	
	public static void register(){
		
	}
	
	static Player player(){
		return handler.core.getPlayer(handler.userid);
	}
	
	protected void cmd(String text, String params, Consumer<String[]> runner){
		handler.command(text, params, runner);
	}
	
	protected void cmd(String text, Consumer<String[]> runner){
		handler.command(text, "", runner);
	}
	
	protected void adminCmd(String text, String params, Consumer<String[]> runner){
		handler.adminCommand(text, params, runner);
	}
}
