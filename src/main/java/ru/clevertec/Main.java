package ru.clevertec;

import ru.clevertec.controller.MessageAssistant;
import ru.clevertec.factory.BeanFactory;

public class Main {
    public static void main(String[] args) {
        BeanFactory factory = BeanFactory.INSTANCE;
        MessageAssistant messageAssistant = factory.getBean(MessageAssistant.class);
        messageAssistant.start();
        factory.close();
    }
}
