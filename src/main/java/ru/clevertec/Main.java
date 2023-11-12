package ru.clevertec;

import ru.clevertec.controller.MessageAssistant;
import ru.clevertec.exception.handler.ExceptionHandler;
import ru.clevertec.factory.BeanFactory;

public class Main {
    public static void main(String[] args) {
        BeanFactory factory = BeanFactory.INSTANCE;
        MessageAssistant messageAssistant = factory.getBean(MessageAssistant.class);
        ExceptionHandler handler = factory.getBean(ExceptionHandler.class);
        try {
            messageAssistant.start();
        } catch (Exception e) {
            handler.handle(e);
        }
        factory.close();
    }
}
