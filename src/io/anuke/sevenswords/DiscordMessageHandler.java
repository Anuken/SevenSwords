package io.anuke.sevenswords;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import io.anuke.utils.bots.TimedMessageHandler;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class DiscordMessageHandler extends TimedMessageHandler{
	String token;
	IDiscordClient client;
	HashMap<String,IUser> users = new HashMap<String, IUser>();
	MessageListener listener;
	String lastchannel;

	public static void main(String[] args){
		try{
			new Core(new DiscordMessageHandler());
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public DiscordMessageHandler() throws Exception{
		List<String> list = Files.readAllLines(Paths.get(System.getProperty("user.home"), "Documents/eclipse").resolve("token-discord.dat"));
		token = list.get(0);

		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);

		client = clientBuilder.build();

		EventDispatcher event = client.getDispatcher();
		event.registerListener(this);

		System.out.println("Discord bot up.");
	}
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event){
		IMessage m = event.getMessage();
		users.put(m.getAuthor().getID(), m.getAuthor());
		lastchannel = event.getMessage().getChannel().getID();
	}

	@Override
	public void sendFile(File file, String chatid){
		//nothing
	}

	@Override
	public void edit(String message, String chatid, String messageid){
		try{
			client.getChannelByID(chatid).getMessageByID(message).edit(message);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}

	@Override
	public String getUserName(String id){
		return users.get(id).getName();
	}

	@Override
	public String sendRaw(String message, String chatid){
		try{
			return client.getChannelByID(chatid).sendMessage(message).getID();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
