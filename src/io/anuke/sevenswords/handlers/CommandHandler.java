package io.anuke.sevenswords.handlers;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.anuke.sevenswords.Core;
import io.anuke.sevenswords.entities.Parseable;
import io.anuke.sevenswords.objects.Player;
import io.anuke.utils.bots.MessageHandler.MessageListener;

public class CommandHandler extends Handler implements MessageListener{
	private String lastid;
	protected boolean waitingForFile;
	protected Class<?> filetype;
	private List<String> admins = Arrays.asList("TheRealTux", "Anuken", "uw0tm8y");
	private List<Command> commands = new ArrayList<Command>();
	private List<Command> adminCommands = new ArrayList<Command>();
	private Player currentPlayer;

	public CommandHandler(Core world) {
		super(world);
		
		CommandRegistrator.handler = this;
		CommandRegistrator.register();
	}

	@Override
	public void onMessageRecieved(String message, String username, String chatid, String userid, String messageid){

		if(waitingForFile && (admins.contains(username))){
			lastid = chatid;
			if(message != null)
				fileRecieved(message);
		}

		if(message == null || (!message.startsWith("-")))
			return;

		message = message.substring(1);
		message = message.toLowerCase();

		lastid = chatid;

		String[] args = message.split(" ");
		message = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		
		currentPlayer = core.getPlayer(userid);
		
		handleCommand(chatid, username, message, args, userid);
	}
	
	public Player getCurrentPlayer(){
		return currentPlayer;
	}
	
	public String getLastChat(){
		return lastid;
	}
	
	public List<Command> getCommandList(){
		return commands;
	}
	
	public List<Command> getAdminCommandList(){
		return adminCommands;
	}

	private void fileRecieved(String string){
		send("Recieved object, parsing...");
		Path path = Paths.get("downloads", "temp");
		try{
			Files.write(path, string.getBytes());
			Parseable p = core.world.createObject(filetype.asSubclass(Parseable.class), path);
			if(p.name == null){
				throw new RuntimeException("The object must have a name!");
			}
			Files.copy(path, core.world.getPath(filetype).resolve(p.name), StandardCopyOption.REPLACE_EXISTING);
			core.world.objects.clear();
			core.world.load();
			send("Object \"" + p.name + "\" parsed successfully.");
		}catch(Exception e){
			send("Error parsing input: " + e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
			System.out.println("Error parsing input: " + e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
			e.printStackTrace();
		}
		waitingForFile = false;
	}

	private void handleCommand(String chatid, String username, String message, String[] args, String userid){
		
		for(Command command : commands){
			if(command.text.equals(message)){
				if(args.length == command.paramLength){
					command.runner.accept(args);
				}else{
					send("Usage: -"+ command.text +" "+command.params);
				}
				break;
			}
		}
		if(admins.contains(username)){
			for(Command command : adminCommands){
				if(command.text.equals(message)){
					if(args.length == command.paramLength){
						command.runner.accept(args);
					}else{
						send("Usage: -"+ command.text +" "+command.params);
					}
					break;
				}
			}
		}
	}
	
	protected void command(String text, String params, Consumer<String[]> runner){
		commands.add(new Command(text, params, runner));
	}
	
	protected void adminCommand(String text, String params, Consumer<String[]> runner){
		adminCommands.add(new Command(text, params, runner));
	}

	public void send(String message){
		core.messages.send(message, lastid);
	}

	@Override
	public void onFileRecieved(String userid, String chatid, String fileid){
		//haha
	}
	
	static class Command{
		public final String text;
		public final String params;
		public final int paramLength;
		public final Consumer<String[]> runner;
		
		public Command(String text, String params, Consumer<String[]> runner){
			this.text = text;
			this.params = params;
			this.runner = runner;
			
			paramLength = params.length() == 0 ? 0 : (params.length() - params.replaceAll(" ", "").length() + 1);
		}
	}
}
