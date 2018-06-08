package io.anuke.sevenswords;

import java.io.File;
import java.util.HashMap;

import io.anuke.sevenswords.bots.MessageHandler.TimedMessageHandler;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class DiscordMessageHandler extends TimedMessageHandler{
	String token;
	IDiscordClient client;
	HashMap<String, IUser> users = new HashMap<String, IUser>();
	MessageListener listener;
	String lastchannel;

	public static void main(String[] args) throws Exception{
		new Core(new DiscordMessageHandler());
	}

	public DiscordMessageHandler() {
		token = System.getProperty("token");

		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);

		client = clientBuilder.login();

		EventDispatcher event = client.getDispatcher();
		event.registerListener(this);

		System.out.println("Discord bot up.");
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event){
		IMessage m = event.getMessage();
		users.put(m.getAuthor().getLongID() + "", m.getAuthor());
		lastchannel = event.getMessage().getChannel().getLongID() + "";
		
		listener.onMessageRecieved(m.getContent(), m.getAuthor().getName(), 
				m.getChannel().getLongID() + "", 
				m.getAuthor().getLongID() + "", 
				m.getLongID() + "");
	}

	@Override
	public void sendFile(File file, String chatid){
		//nothing
	}

	@Override
	public void edit(String message, String chatid, String messageid){
		try{
			client.getChannelByID(Long.parseLong(chatid)).getMessageByID(Long.parseLong(messageid)).edit(process(message));
		}catch(Exception e){
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
	public void send(String message, final String id){
		String out = process(message);
		
		super.send(out, id);
	}

	@Override
	public String sendRaw(String message, String chatid){
		
		try{
			if(message.contains("*") && !message.contains("**")) message = process(message);
			return client.getChannelByID(Long.parseLong(chatid)).sendMessage(message).getLongID() + "";
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	String process(String message){
		String out = "";
		for(char c : message.toCharArray()){
			if(c == '*'){
				out += "**";
			}else{
				out += c;
			}
		}

		return out;
	}
}
