package io.anuke.sevenswords;

import io.anuke.utils.bots.TimedMessageHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.json.JSONObject;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.updateshandlers.SentCallback;

public class TelegramMessageSender extends TimedMessageHandler{
	Bot bot;
	MessageListener listener;
	String token;

	public static void main(String[] args){
		new Core(new TelegramMessageSender());
	}

	{
		
		try{
			System.out.println("Starting bot...");
			
			
			List<String> list = Files.readAllLines(Paths.get(System.getProperty("user.home"), "workspace").resolve("token"));
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

	@Override
	public String sendRaw(String message, String chatid){
		try{
			Message sent = bot.sendMessage(new SendMessage().setChatId(chatid).setText(message));
			return sent.getMessageId() + "";
		}catch(TelegramApiException e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void edit(String message, String chatid, String messageid){
		try{
			bot.editMessageTextAsync(new EditMessageText().setMessageId(Integer.parseInt(messageid)).setText(message).setChatId(chatid), 
				new SentCallback<Message>(){

					@Override
					public void onResult(BotApiMethod<Message> method, JSONObject jsonObject){
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onError(BotApiMethod<Message> method, JSONObject jsonObject){
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onException(BotApiMethod<Message> method, Exception exception){
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
			if(listener != null) listener.onMessageRecieved(update.getMessage().getText(), update.getMessage().getFrom().getUserName(), update.getMessage().getChatId() + "", update.getMessage().getFrom().getId() + "", update.getMessage().getMessageId() + "");
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
		// TODO Auto-generated method stub
		
	}

}
