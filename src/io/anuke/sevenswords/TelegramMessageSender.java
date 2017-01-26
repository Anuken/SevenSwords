package io.anuke.sevenswords;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updateshandlers.SentCallback;

import io.anuke.utils.bots.TimedMessageHandler;

public class TelegramMessageSender extends TimedMessageHandler{
	Bot bot;
	MessageListener listener;
	String token;

	public static void main(String[] args){
		ApiContextInitializer.init();
		new Core(new TelegramMessageSender());
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
		
	}

}
