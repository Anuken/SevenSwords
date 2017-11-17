package io.anuke.sevenswords;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updateshandlers.SentCallback;

import io.anuke.sevenswords.bots.MessageHandler.TimedMessageHandler;


public class TelegramMessageHandler extends TimedMessageHandler{
	Bot bot;
	MessageListener listener;
	String token;
	HashMap<String, User> users = new HashMap<String, User>();

	public static void main(String[] args){
		ApiContextInitializer.init();
		new Core(new TelegramMessageHandler());
	}

	{
		
		try{
			System.out.println("Starting bot...");
			
			
			List<String> list = Files.readAllLines(Paths.get(System.getProperty("user.home"), "Documents/eclipse").resolve("token.dat"));
			token = list.get(0);
			
			TelegramBotsApi api = new TelegramBotsApi();
			bot = new Bot();
			api.registerBot(bot);
			System.out.println("Bot started.");
		}catch(Exception e){
			e.printStackTrace();
			System.exit( -1);
		}
	}
	
	public String getUserName(String id){
		return users.get(id).getFirstName();
	}
	
	public User getUser(String id){
		return users.get(id);
	}

	@Override
	public String sendRaw(String message, String chatid){
		try{
			Message sent = bot.sendMessage(new SendMessage().setChatId(chatid).setText(message).enableMarkdown(true));
			return sent.getMessageId() + "";
		}catch(Exception e){
			if(e.toString().contains("can't parse message text")){
				System.out.println("Bad markdown request. Retrying...");
				
				try{
					Message sent = bot.sendMessage(new SendMessage().setChatId(chatid).setText(message));
					return sent.getMessageId() + "";
				}catch(Exception e2){
					e2.printStackTrace();
				}
			}else{
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void edit(String message, String chatid, String messageid){
		try{
			bot.editMessageTextAsync(new EditMessageText().enableMarkdown(true).setMessageId(Integer.parseInt(messageid)).setText(message).setChatId(chatid), 
				new SentCallback<Message>(){

					@Override
					public void onError(BotApiMethod<Message> arg0, TelegramApiRequestException arg1){
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onException(BotApiMethod<Message> arg0, Exception arg1){
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onResult(BotApiMethod<Message> arg0, Message arg1){
						// TODO Auto-generated method stub
						
					}
				
			});
		}catch(TelegramApiException e){
			e.printStackTrace();
		}
	}

	@Override
	public void setMessageListener(MessageListener listener){
		this.listener = listener;
	}

	class Bot extends TelegramLongPollingBot{

		@Override
		public void onUpdateReceived(Update update){
			if(listener != null){
				users.put(update.getMessage().getFrom().getId() + "", update.getMessage().getFrom());
				listener.onMessageRecieved(update.getMessage().getText(), update.getMessage().getFrom().getUserName(), update.getMessage().getChatId() + "", update.getMessage().getFrom().getId() + "", update.getMessage().getMessageId() + "");
			}
		}

		@Override
		public String getBotUsername(){
			return "DungeonCrawlBot";
		}

		@Override
		public String getBotToken(){
			return token;
		}
	}

	@Override
	public void sendFile(File file, String chatid){
		
	}
}
