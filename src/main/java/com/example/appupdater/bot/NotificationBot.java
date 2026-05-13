package com.example.appupdater.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class NotificationBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.admin-chat-id}")
    private String adminChatId;

    public NotificationBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

    public void sendNotification(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(adminChatId);
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки в Telegram: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
