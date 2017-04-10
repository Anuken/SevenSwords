package io.anuke.sevenswords;

import org.telegram.telegrambots.ApiContextInitializer;

public class DualMessageHandler{
	public static void main(String[] args) throws Exception{
		ApiContextInitializer.init();
		new Core(new TelegramMessageHandler());
		new Core(new DiscordMessageHandler());
	}
}
