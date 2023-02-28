package com.manzhula.finder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinderApplication.class, args);
    }

}


//package com.manzhula.finder;
//
//import com.github.kshashov.telegram.api.MessageType;
//import com.github.kshashov.telegram.api.TelegramMvcController;
//import com.github.kshashov.telegram.api.TelegramRequest;
//import com.github.kshashov.telegram.api.bind.annotation.BotController;
//import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
//import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
//import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest;
//import com.github.kshashov.telegram.handler.processor.response.BotBaseRequestMethodProcessor;
//import com.github.kshashov.telegram.handler.processor.response.BotHandlerMethodReturnValueHandler;
//import com.pengrad.telegrambot.Callback;
//import com.pengrad.telegrambot.model.Chat;
//import com.pengrad.telegrambot.model.Message;
//import com.pengrad.telegrambot.model.User;
//import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
//import com.pengrad.telegrambot.request.BaseRequest;
//import com.pengrad.telegrambot.response.BaseResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.core.MethodParameter;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@BotController
//@SpringBootApplication
//public class FinderApplication implements TelegramMvcController {
//	private static final Logger LOGGER = LoggerFactory.getLogger(FinderApplication.class);
//
//	@Value("${bot.token}")
//	private String token;
//
//	@Override
//	public String getToken() {
//		return token;
//	}
//
//	@MessageRequest("*")
//	public String search(Message message, User user) {
//		LOGGER.info(message.toString());
//
//		return "Пошук товару '" + message.text() + "' триває...";
//	}
//
//	@BotRequest(value = "/hello", type = {MessageType.CALLBACK_QUERY, MessageType.MESSAGE})
//	public BaseRequest hello(User user, Chat chat, Message message) {
//		SendMessage sendMessage = new SendMessage();
//		sendMessage.setChatId(chat.id().toString());
//		sendMessage.setText("Пошук товару '" + message.text() + "' триває...");
//
//
//		return null;
//	}
//
//	@BotRequest(value = "*", type = {MessageType.ANY, MessageType.MESSAGE})
//	public BotHandlerMethodReturnValueHandler search(User user, Chat chat, Message message) {
//		SendMessage sendMessage = new SendMessage();
//		sendMessage.setChatId(chat.id().toString());
//		sendMessage.setText("Пошук товару '" + message.text() + "' триває...");
//		// Create ReplyKeyboardMarkup object
//		// Create the keyboard (list of keyboard rows)
////		List<KeyboardRow> keyboard = new ArrayList<>();
//		// Create a keyboard row
//		KeyboardRow row = new KeyboardRow();
//		// Set each button, you can also use KeyboardButton objects if you need something else than text
//		row.add("Фільтри");
////		row.add("Row 1 Button 2");
////		row.add("Row 1 Button 3");
//		// Add the first row to the keyboard
////		keyboard.add(row);
//		// Create another keyboard row
////		row = new KeyboardRow();
////		// Set each button for the second line
////		row.add("Row 2 Button 1");
////		row.add("Row 2 Button 2");
////		row.add("Row 2 Button 3");
//		// Add the second row to the keyboard
////		keyboard.add(row);
//		// Set the keyboard to the markup
//		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(String.valueOf(row));
////		keyboardMarkup.setKeyboard(keyboard);
//		// Add it to the message
//
//	    sendMessage.setReplyMarkup((ReplyKeyboard) keyboardMarkup);
//
//		return new BotBaseRequestMethodProcessor().supportsReturnType(MethodParameter.forExecutable(ReplyKeyboardMarkup.class, ReplyKeyboardMarkup.class.getMethod("ReplyKeyboardMarkup"));
//	}
//
//	@MessageRequest("/hello {name:[\\S]+}")
//	public String helloWithName(@BotPathVariable("name") String userName) {
//		// Return a string if you need to reply with a simple message
//		return "Hello, " + userName;
//	}
//
//	@MessageRequest("/helloCallback")
//	public String helloWithCustomCallback(TelegramRequest request, User user) {
//		request.setCallback(new Callback() {
//			@Override
//			public void onResponse(BaseRequest request, BaseResponse response) {
//				// TODO
//				LOGGER.info("response");
//
//			}
//
//			@Override
//			public void onFailure(BaseRequest request, IOException e) {
//				// TODO
//				LOGGER.info("failure");
//			}
//		});
//		return "Hello, " + user.firstName() + "!";
//	}
//
//	@MessageRequest("/start")
//	public String start(Message message, User user) {
//		LOGGER.info(message.toString());
//
//		return "Привіт, " + user.firstName() + ", я FiiiinderBooot, і я допоможу тобі знайти будь-що," +
//                                        "просто введи що тобі потрібно і я все зроблю!";
//	}
//
//	@MessageRequest("/*")
//	public String anyCommand(Message message, User user) {
//		LOGGER.info(message.toString());
//
//		return "Нажаль, я не розумію цієї команди. Спробуй /help";
//	}
//
//	public static void main(String[] args) {
//		SpringApplication.run(FinderApplication.class);
//	}
//}
