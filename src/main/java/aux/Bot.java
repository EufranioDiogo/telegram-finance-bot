package aux;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    public static final Logger LOGGER = LogManager.getLogger();
    private String botToken;
    private String username;
    private boolean screaming = false;
    private InlineKeyboardMarkup keyboardM1;
    private InlineKeyboardMarkup keyboardM2;
    InlineKeyboardButton next = InlineKeyboardButton.builder()
            .text("Next").callbackData("next")
            .build();

    InlineKeyboardButton back = InlineKeyboardButton.builder()
            .text("Back").callbackData("back")
            .build();

    InlineKeyboardButton url = InlineKeyboardButton.builder()
            .text("Tutorial")
            .url("https://core.telegram.org/bots/api")
            .build();

    {
        keyboardM1 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(next))
                .build();

        keyboardM2 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(back))
                .keyboardRow(List.of(url))
                .build();
    }

    public Bot() {
        LOGGER.info("INITIALIZING VARIABLES");
        initializeEnvVariables();
        LOGGER.info("INITIALIZED VARIABLES");
    }

    private void initializeEnvVariables() {
        LOGGER.info("LOADING TELEGRAM KEYS");
        String telegramBotToken = System.getenv("TELEGRAM_BOT_TOKEN");
        LOGGER.info("LOADED TELEGRAM KEYS");


        if (telegramBotToken == null) {
            LOGGER.error("No TELEGRAM KEYS found in environment variables");
            throw new RuntimeException("No TELEGRAM_BOT_TOKEN found in environment variables");
        }

        this.botToken = telegramBotToken;

        try {
            Properties properties = new Properties();
            URL resource = Bot.class.getClassLoader().getResource("bot.properties");

            if (resource == null) {
                throw new RuntimeException("Could not find properties file");
            }

            properties.load(Bot.class.getClassLoader().getResourceAsStream("bot.properties"));
            this.username = properties.getProperty("username");


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        if (message.isCommand()) {
            executeCommand(update);
        } else {
            sendMessage(update);
        }
    }

    private void sendMessage(Update update) {
        Message msg = update.getMessage();
        User user = msg.getFrom();

        System.out.println(user.getFirstName() + " wrote " + msg.getText());
        String responseMessage = msg.getText();

        if (this.screaming) {
            responseMessage = (responseMessage + " screaming").toUpperCase();
            sendText(user.getId(), responseMessage);
            return;
        }

        if (msg.getText().contains("ola")) {
            sendText(user.getId(), "Como estás meu amigo?");
        } else {
            sendText(user.getId(), msg.getText().toLowerCase() + "----------------");
        }
    }

    private void executeCommand(Update update) {
        Message msg = update.getMessage();
        if (msg.isCommand()) {
            if (msg.getText().equals("/scream"))         //If the command was /scream, we switch gears
                screaming = true;
            else if (msg.getText().equals("/whisper"))  //Otherwise, we return to normal
                screaming = false;
            else if (msg.getText().equals("/menu")) {
                sendMenu(update.getMessage().getFrom().getId(), "<b>Menu 1</b>", keyboardM1);
            } //We don't want to echo commands, so we exit
        }

    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(kb).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {

        return this.username;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public String toString() {
        return "aux.Bot{" +
                "botToken='" + botToken + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
