package com.manzhula.finder.botcontroller;

import com.manzhula.finder.config.UserCache;
import com.manzhula.finder.kafka.KafkaProducerService;
import com.manzhula.finder.models.FoundItems;
import com.manzhula.finder.models.ItemDto;
import com.manzhula.finder.models.ResearchQuery;
import com.manzhula.finder.models.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.games.CallbackGame;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Component
public class FinderController extends TelegramLongPollingBot {
    @Value("${KAFKA_TOPIC_PREFIX}")
    String prefix;

    @Value("${LIST_SIZE}")
    Integer listSize;

    final KafkaProducerService kafkaProducerService;

    Map<String, UserCache> userCacheMap;

    Map<String, UserCache> userFilterCacheMap;

    Map<String, UserCache> userOnFilterCacheMap;

    private static final Logger LOGGER = LoggerFactory.getLogger(FinderController.class);

    private static String BOT_TOKEN = "5303759835:AAFJOxMf5239p_XP6U1qk8pu1A86aHEgDb8";
    private final String maxPrice = "maxPrice";
    private final String minPrice = "minPrice";
    private final String place = "place";

    private final String notEquals = "notEquals";
    private final String parser = "parser";

    public FinderController(KafkaProducerService kafkaProducerService,
                            Map<String, UserCache> userCacheMap,
                            Map<String, UserCache> userFilterCacheMap,
                            Map<String, UserCache> userOnFilterCacheMap) {
        this.kafkaProducerService = kafkaProducerService;
        this.userCacheMap = userCacheMap;
        this.userFilterCacheMap = userFilterCacheMap;
        this.userOnFilterCacheMap = userOnFilterCacheMap;
    }

//    TelegramBot bot;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "Fiiiinder bot";
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }


    @Override
    public void onUpdateReceived(Update update) {
        LOGGER.info(update.hasMessage()?update.getMessage().toString():update.getCallbackQuery().toString());
        if (update.hasMessage() && update.getMessage().hasText() && isNotBlank(update.getMessage().getText())) {
            String query = update.getMessage().getText();
//            List<String> tokens = Arrays.stream(update.getMessage().getText().split(" ")).toList();
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());

            if (query.startsWith("/start")) {
                message.setText("Привіт, \"" + update.getMessage().getChat().getFirstName() + "\", я FiiiinderBooot, і я допоможу тобі знайти " +
                        "будь-що, просто введи що тобі потрібно і я все зроблю!");
                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                row.add("Фільтр");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("Очистити фільтр");
                keyboard.add(row);
                row = new KeyboardRow();
                row.add("Сортування");
                keyboard.add(row);
                keyboardMarkup.setKeyboard(keyboard);
                message.setReplyMarkup(keyboardMarkup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (query.equals("Фільтр")) {
                message.setText("Задайте параметри фільтрації");

                InlineKeyboardButton lowButton = new InlineKeyboardButton();
                lowButton.setText("Мінімальна ціна");
                lowButton.setCallbackData("2%;;№;:4" + minPrice);
                InlineKeyboardButton highButton = new InlineKeyboardButton();
                highButton.setText("Максимальна ціна");
                highButton.setCallbackData("2%;;№;:4" + maxPrice);
                InlineKeyboardButton companyButton = new InlineKeyboardButton();
                companyButton.setText("Торгівельний майданчик");
                companyButton.setCallbackData("2%;;№;:4" + place);
                InlineKeyboardButton notEqualsButton = new InlineKeyboardButton();
                notEqualsButton.setText("Уникати результатів");
                notEqualsButton.setCallbackData("2%;;№;:4" + notEquals);
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
                keyboardButtonsRow.add(lowButton);
                keyboardButtonsRow.add(highButton);
                keyboardButtonsRow1.add(companyButton);
                keyboardButtonsRow1.add(notEqualsButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                rowList.add(keyboardButtonsRow1);
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(rowList);
                message.setReplyMarkup(inlineKeyboardMarkup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (query.equals("Очистити фільтр")) {
                message.setText("Фільтр очищено");
                userFilterCacheMap.remove(update.getMessage().getChatId().toString());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (query.equals("Владислав пес")) {
                message.setText("Марта миш");
//                update.getCallbackQuery().get
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (query.equals("go")) {
                message.setText("Товар з'явився у продажу!");
                SendPhoto tgMessage = new SendPhoto();
                tgMessage.setChatId("439032305");
                tgMessage.setPhoto(new InputFile("https://images.prom.ua/3561350496_w200_h200_gimcat-malt-soft.jpg"));
                tgMessage.setCaption("<a href=\"" + "https://my.prom.ua/remote/context_ads/click_ad_v2?token=v2%3AGDm1EBeg9YMsIDuVrx3b3bJlGrbsuNmro-PqhmQqkceIioJMqOuZy9qHT-IWMgvv6Vy4cUz8W6M04cMOmQDcKmwDbNKDnrgZNA9plqkF3GNXgUrcyw&campaign_id=1448730&product_id=1549756414&source=prom%3Asearch%3Aserp&variant=&locale=uk&prices_param=&from_spa=true"
                        + "\">" + "GimCat Malt - soft Extra 200г Паста для виведення шерсті та покращення моторики шлунку у котів" +
                        "</a>\nЦіна: " + 525 + " грн\nМаркет: <a href=\"" + "https://prom.ua/" +
                        "\">" + "prom.ua" + "</a>");
                tgMessage.setParseMode(ParseMode.HTML);
                try {
                    execute(message);
                    execute(tgMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                if (userOnFilterCacheMap.containsKey(message.getChatId().toString())) {
                    UserCache userCache = userOnFilterCacheMap.get(message.getChatId().toString());
                    if (!userFilterCacheMap.containsKey(message.getChatId().toString())) {
                        userFilterCacheMap.put(message.getChatId().toString(), UserCache.builder().build());
                    }
                    try {
                        if (userCache.getFilterToken().equals(maxPrice)) {
                            if (!isNull(userFilterCacheMap.get(message.getChatId())) &&
                                    !isNull(userFilterCacheMap.get(message.getChatId().toString()).getMinPrice())) {
                                if(userFilterCacheMap.get(message.getChatId().toString()).getMinPrice() <= Double.parseDouble(query)) {
                                    userFilterCacheMap.get(message.getChatId().toString()).setMaxPrice(Double.parseDouble(query));
                                    message.setText("Максимальна ціна: " + query + " грн");
                                    userOnFilterCacheMap.remove(message.getChatId().toString());
                                } else {
                                    message.setText("Максимальна ціна не може бути меншою за мінімальну ціну, введіть корректне значення");
                                }
                            } else {
                                userFilterCacheMap.get(message.getChatId().toString()).setMaxPrice(Double.parseDouble(query));
                                message.setText("Максимальна ціна: " + query + " грн");
                                userOnFilterCacheMap.remove(message.getChatId().toString());
                            }
                        }
                        if (userCache.getFilterToken().equals(minPrice)) {
                            if (!isNull(userFilterCacheMap.get(message.getChatId())) &&
                                    !isNull(userFilterCacheMap.get(message.getChatId().toString()).getMaxPrice())) {
                                if(userFilterCacheMap.get(message.getChatId().toString()).getMaxPrice() >= Double.parseDouble(query)) {
                                    userFilterCacheMap.get(message.getChatId().toString()).setMinPrice(Double.parseDouble(query));
                                    message.setText("Мінімальна ціна: " + query + " грн");
                                    userOnFilterCacheMap.remove(message.getChatId().toString());
                                } else {
                                    message.setText("Мінімальна ціна не може бути більшою за максимальну ціну, введіть корректне значення");
                                }
                            } else {
                                userFilterCacheMap.get(message.getChatId().toString()).setMinPrice(Double.parseDouble(query));
                                message.setText("Мінімальна ціна: " + query + " грн");
                                userOnFilterCacheMap.remove(message.getChatId().toString());
                            }
                        }
                        if (userCache.getFilterToken().equals(place)) {
                            if(query.equals("prom") ||
                                    query.equals("comfy")) {
                                userFilterCacheMap.get(message.getChatId().toString()).setPlace(query);
                                message.setText("Маркетплейс: " + query);
                                userOnFilterCacheMap.remove(message.getChatId().toString());
                            } else {
                                message.setText("Введіть валідну назву маркетплейсу: 'prom', 'comfy'");
                            }
                        }
                        if (userCache.getFilterToken().equals(notEquals)) {
                            userFilterCacheMap.get(message.getChatId().toString()).setNotEquals(query);
                            message.setText("Уникати результатів що містять: " + query);
                            userOnFilterCacheMap.remove(message.getChatId().toString());
                        }

                        try {
                            execute(message);
                        } catch (TelegramApiException j) {
                            j.printStackTrace();
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();

                        message.setText("Необхідно ввести валідну ціну");

                        try {
                            execute(message);
                        } catch (TelegramApiException j) {
                            j.printStackTrace();
                        }
                    }
                } else {
                    message.setText("Пошук товару '" + query + "' триває...");

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    SendMessage tgMessage = new SendMessage();
                    tgMessage.setChatId(message.getChatId().toString());
                    tgMessage.setText("Середня ціна в категорії аналізується");

                    Integer messageToDel = null;
                    try {
                        messageToDel = execute(tgMessage).getMessageId();
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    kafkaProducerService.sendResearchRequest(prefix + "research-request",
                            ResearchQuery.builder()
                                    .chatId(update.getMessage().getChatId())
                                    .query(query)
                                    .messageToDel(messageToDel)
                                    .localDateTime(LocalDateTime.now().toString())
                                    .build());
                }
            }
        } else if(update.hasCallbackQuery()){
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (update.getCallbackQuery().getData().startsWith("%;;№;:4")) {
                UserCache userCache = new UserCache();
                String[] strArgs = update.getCallbackQuery().getData().split(":4");
                if (strArgs[1].equals(maxPrice)) {
                    userCache.setMaxPrice(Double.parseDouble(strArgs[2]));
                } else if (strArgs[1].equals(minPrice)) {
                    userCache.setMinPrice(Double.parseDouble(strArgs[2]));
                }
                userFilterCacheMap.put(chatId.toString(), userCache);
            } else if (update.getCallbackQuery().getData().startsWith("2%")){
                UserCache userCache = new UserCache();
                String[] strArgs = update.getCallbackQuery().getData().split(":4");
                userCache.setOnFilter(true);
                userCache.setFilterToken(strArgs[1]);
                userOnFilterCacheMap.put(chatId.toString(), userCache);

                SendMessage tgMessage = new SendMessage();
                tgMessage.setChatId(chatId.toString());
                tgMessage.setText("Введіть бажане значення: ");

                try {
                    execute(tgMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (update.getCallbackQuery().getData().startsWith("$%@22zho_p")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("Окей, чекайте сповіщення!");

                kafkaProducerService.sendSubscribeRequest(prefix + "subscribe-request", Subscribe.builder()
                                .itemId(update.getCallbackQuery().getData().replace("$%@22zho_p", ""))
                                .userId(chatId.toString())
                        .build());

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                SendMessage message = new SendMessage();
                if (userCacheMap.containsKey(chatId+":"+update.getCallbackQuery().getData()) &&
                        !filter(userCacheMap.get(chatId+":"+update.getCallbackQuery().getData()).getItemList(), chatId.toString()).isEmpty()) {

                    printList(filter(userCacheMap.get(chatId+":"+update.getCallbackQuery().getData()).getItemList(), chatId.toString()), chatId, update.getCallbackQuery().getData());
                } else {
                    message.setChatId(chatId.toString());
                    message.setText("Нових результатів не знайдено, спробуйте змінити запит.");

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @KafkaListener(topics = "${KAFKA_TOPIC_PREFIX}research-response", groupId = "${KAFKA_USERNAME}-consumer", containerFactory = "kafkaListenerContainerFactory")
    public void listenGroupFoo(@Payload FoundItems message, Acknowledgment ack) {
        LOGGER.info("Received message for: " + message.getChatId());

        if (!isNull(message.getAvgPrice()) && message.getAvgPrice() > 0) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setText("Середня ціна в категорії: " + String.format("%.0f", message.getAvgPrice()) + " грн.");
            editMessage.setChatId(message.getChatId().toString());
            editMessage.setMessageId(message.getMessageToDel());

            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(message.getChatId().toString());
            editMessageReplyMarkup.setMessageId(message.getMessageToDel());

            InlineKeyboardButton lowButton = new InlineKeyboardButton();
            lowButton.setText("Хочу дешевше");
            lowButton.setCallbackData("%;;№;:4" + maxPrice + ":4" + message.getAvgPrice());
            InlineKeyboardButton highButton = new InlineKeyboardButton();
            highButton.setText("Шукаю дорожче");
            highButton.setCallbackData("%;;№;:4" + minPrice + ":4" + message.getAvgPrice());
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(lowButton);
            keyboardButtonsRow.add(highButton);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow);
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(rowList);
            editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
            try {
                execute(editMessage);
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            if (Boolean.TRUE.equals(message.getIsSubscribed())) {
                if(!isNull(message.getFoundItems()) && message.getFoundItems().isEmpty()) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setText("Пошук товару " + message.getQuery() +  " досі триває");
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setText("Товар з'явився у продажу");
                    ItemDto itemDto = message.getFoundItems().get(0);
                    SendPhoto tgMessage = new SendPhoto();
                    tgMessage.setChatId(message.getChatId().toString());
                    tgMessage.setPhoto(new InputFile(itemDto.getItemPhotoURL()));
                    tgMessage.setCaption("<a href=\"" + itemDto.getItemURL() + "\">" + itemDto.getItemName() +
                            "</a>\nЦіна: " + itemDto.getItemPrice() + " грн\nМаркет: <a href=\"" + itemDto.getItemSource() +
                            "\">" + itemDto.getItemSource().substring(8, itemDto.getItemSource().length() - 1) + "</a>");
                    tgMessage.setParseMode(ParseMode.HTML);
                    try {
                        execute(sendMessage);
                        execute(tgMessage);
                    } catch (TelegramApiException e) {
                        SendMessage tMessage = new SendMessage();
                        tMessage.setChatId(message.getChatId().toString());
                        tMessage.setText("<a href=\"" + itemDto.getItemURL() + "\">" + itemDto.getItemName() +
                                "</a>\nЦіна: " + itemDto.getItemPrice() + " грн\nМаркет: <a href=\"" + itemDto.getItemSource() +
                                "\">" + itemDto.getItemSource().substring(8, itemDto.getItemSource().length() - 1) + "</a>");
                        tMessage.setParseMode(ParseMode.HTML);

                        try {
                            execute(tMessage);
                        } catch (TelegramApiException j) {
                            j.printStackTrace();
                        }
                    }
                }
            } else {
                List<ItemDto> itemDtos = filter(message.getFoundItems(), message.getChatId().toString());

                if (!itemDtos.isEmpty()) {
                    printList(itemDtos, message.getChatId(), message.getQuery());
                } else {
                    SendMessage tgMessage = new SendMessage();
                    tgMessage.setChatId(message.getChatId().toString());
                    tgMessage.setText("Нажаль за вашим запитом нічого не знайдено.\nСпробуйте змінити фільтри, або змінити запит!");

                    try {
                        execute(tgMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ack.acknowledge();
    }

    public void printList(List<ItemDto> list, Long chatId, String query) {
        for (int i = 0; i < Math.min(list.size(), listSize); i++) {
            ItemDto item = list.get(i);
            SendPhoto tgMessage = new SendPhoto();
            tgMessage.setChatId(chatId.toString());
            tgMessage.setPhoto(new InputFile(item.getItemPhotoURL()));
            tgMessage.setCaption("<a href=\"" + item.getItemURL() + "\">" + item.getItemName() +
                    "</a>\nЦіна: " + item.getItemPrice() + " грн\nМаркет: <a href=\"" + item.getItemSource() +
                    "\">" + item.getItemSource().substring(8, item.getItemSource().length() - 1) + "</a>");
            tgMessage.setParseMode(ParseMode.HTML);

            if (!item.getItemPresence()) {
                InlineKeyboardButton existButton = new InlineKeyboardButton();
                tgMessage.setCaption(tgMessage.getCaption() +"\nНемає в наявності");
                existButton.setText("Повідомити про наявність");
                existButton.setCallbackData("$%@22zho_p" + item.getItemId());
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(existButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(rowList);
                tgMessage.setReplyMarkup(inlineKeyboardMarkup);
            }

            if (i == Math.min(list.size(), listSize)-1) {
                InlineKeyboardButton nextButton = new InlineKeyboardButton();
                nextButton.setText("Наступна сторінка");
                nextButton.setCallbackData(query);
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(nextButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                if (!item.getItemPresence()) {
                    InlineKeyboardButton existButton = new InlineKeyboardButton();
                    tgMessage.setCaption(tgMessage.getCaption().endsWith("Немає в наявності")?tgMessage.getCaption():tgMessage.getCaption() +"\nНемає в наявності");
                    existButton.setText("Повідомити про наявність");
                    existButton.setCallbackData("$%@22zho_p" + item.getItemId());
                    List<InlineKeyboardButton> existButtonsRow = new ArrayList<>();
                    existButtonsRow.add(existButton);
                    rowList.add(existButtonsRow);
                }
                rowList.add(keyboardButtonsRow);
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(rowList);
                tgMessage.setReplyMarkup(inlineKeyboardMarkup);

                userCacheMap.put(chatId+":"+query, UserCache.builder()
                                .itemList(list.subList(i < list.size()-1? i+1: i, list.size()-1))
                                .build());
            }

            try {
                execute(tgMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();

                SendMessage tMessage = new SendMessage();
                tMessage.setChatId(chatId.toString());
                tMessage.setText("<a href=\"" + item.getItemURL() + "\">" + item.getItemName() +
                        "</a>\nЦіна: " + item.getItemPrice() + " грн\nМаркет: <a href=\"" + item.getItemSource() +
                        "\">" + item.getItemSource().substring(8, item.getItemSource().length() - 1) + "</a>");
                tMessage.setParseMode(ParseMode.HTML);

                if (i == Math.min(list.size(), listSize)-1) {
                    InlineKeyboardButton nextButton = new InlineKeyboardButton();
                    nextButton.setText("Наступна сторінка");
                    nextButton.setCallbackData(query);
                    List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                    keyboardButtonsRow.add(nextButton);
                    List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                    if (!item.getItemPresence()) {
                        InlineKeyboardButton existButton = new InlineKeyboardButton();
                        tMessage.setText(tMessage.getText().endsWith("Немає в наявності")?tMessage.getText():tMessage.getText() +"\nНемає в наявності");
                        existButton.setText("Повідомити про наявність");
                        existButton.setCallbackData("$%@22zho_p" + item.getItemId());
                        List<InlineKeyboardButton> existButtonsRow = new ArrayList<>();
                        existButtonsRow.add(existButton);
                        rowList.add(existButtonsRow);
                    }
                    rowList.add(keyboardButtonsRow);
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.setKeyboard(rowList);
                    tMessage.setReplyMarkup(inlineKeyboardMarkup);

                    userCacheMap.put(chatId+":"+query, UserCache.builder()
                            .itemList(list.subList(i, list.size()-1))
                            .build());
                }

                try {
                    execute(tMessage);
                } catch (TelegramApiException j) {
                    j.printStackTrace();
                }
            }
        }
    }

//    public void printItem() {
//
//    }

    public List<ItemDto> filter(List<ItemDto> items, String chatId) {
        List<ItemDto> itemDtos = items;
        if (userFilterCacheMap.containsKey(chatId)) {
            UserCache userCache = userFilterCacheMap.get(chatId);
            if (userCache.getMaxPrice() != null) {
                itemDtos = itemDtos.stream()
                        .filter(i->i.getItemPrice() <= userCache.getMaxPrice())
                        .collect(Collectors.toList());
            }
            if (userCache.getMinPrice() != null) {
                itemDtos = itemDtos.stream()
                        .filter(i->i.getItemPrice() >= userCache.getMinPrice())
                        .collect(Collectors.toList());
            }
            if (userCache.getPlace() != null) {
                itemDtos = itemDtos.stream()
                        .filter(i->i.getItemSource().contains(userCache.getPlace()))
                        .collect(Collectors.toList());
            }
            if (userCache.getNotEquals() != null) {
                itemDtos = itemDtos.stream()
                        .filter(i->!i.getItemName().toLowerCase(Locale.ROOT).contains(userCache.getNotEquals().toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            }
        }

        return itemDtos;
    }

}
