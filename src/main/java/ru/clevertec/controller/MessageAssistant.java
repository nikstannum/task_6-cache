package ru.clevertec.controller;

import java.io.Closeable;
import java.util.List;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import ru.clevertec.controller.util.json_parser.JsonParser;
import ru.clevertec.exception.handler.ExceptionHandler;
import ru.clevertec.service.dto.CustomerDto;

@RequiredArgsConstructor
public class MessageAssistant implements Closeable {
    private final static String FUNCTIONAL_MESSAGE = """
            If you want to create customer, press key C.
            If you want to read all customers, press GA.
            If you want to read by id, press G.
            If you want to update customer, press U.
            If you want to delete customer, press D.
            If you want to to finish work with app, press F.
            """;
    private final static String START_MESSAGE = """
            Hello!
            My functional includes next opportunities with customers:
            you can create, get all, get by id, update, delete by id.
            """ + FUNCTIONAL_MESSAGE;
    private static final String MESSAGE_FOR_CREATE = """
            Please, input JSON string of the customer.
            For example:
            {"firstName":"Ivan","lastName":"Ivanov","dateBirth":"01.11.2011","email":"ivanov@gmail.com"}
            """;
    private final static String MESSAGE_FOR_CONTINUE_CHAT = """
            Let's try again.
            """ + FUNCTIONAL_MESSAGE;
    private static final String MESSAGE_TO_INPUT_PAGE_SIZE = """
            Please, input page and size. For example:
            2,3
            It will mean that page is 2 and size is 3.
            """;
    private static final String MESSAGE_TO_INPUT_CUSTOMER_ID = """
            Please, input identifier of customer. For example:
            1
            It will mean that you want to get customer with id = 1.
            """;
    private static final String MESSAGE_TO_INPUT_CUSTOMER_FOR_UPDATE = """
            Please, input JSON string of the customer you want to update. For example:
            {"id":1,"firstName":"Ivan","lastName":"Ivanov","dateBirth":"01.11.2011","email":"ivanov@gmail.com"}
            It will mean that you want to update the customer with id = 1 and set the appropriate properties.
            """;
    private static final String MESSAGE_TO_INPUT_CUSTOMER_ID_FOR_DELETE = """
            Please, input identifier of customer. For example:
            1
            It will mean that you want to delete customer with id = 1.
            """;
    private final Scanner scanner = new Scanner(System.in);
    private final CustomerController customerController;
    private final JsonParser jsonParser;
    private final ExceptionHandler handler;

    public void start() {
        System.out.println(START_MESSAGE);
        while (true) {
            System.out.println(MESSAGE_FOR_CONTINUE_CHAT);
            String input = scanner.next();
            try {
                switch (input) {
                    case "C" -> processCreate();
                    case "GA" -> processGetAll();
                    case "G" -> processGetById();
                    case "U" -> processUpdate();
                    case "D" -> processDeleteById();
                    case "F" -> {
                        return;
                    }
                }
            } catch (Exception e) {
                handler.handle(e);
            }
        }
    }


    private void processDeleteById() {
        System.out.println(MESSAGE_TO_INPUT_CUSTOMER_ID_FOR_DELETE);
        String idStr = scanner.next();
        Long id = Long.parseLong(idStr);
        customerController.deleteById(id);
        System.out.println("You deleted customer with id = " + id + " successfully");
    }

    private void processUpdate() {
        System.out.println(MESSAGE_TO_INPUT_CUSTOMER_FOR_UPDATE);
        String json = scanner.next();
        CustomerDto customerDto = jsonParser.read(json, CustomerDto.class);
        CustomerDto updated = customerController.update(customerDto);
        System.out.println("Here is updated customer\n" + updated);
    }

    private void processGetById() {
        System.out.println(MESSAGE_TO_INPUT_CUSTOMER_ID);
        String idStr = scanner.next();
        Long id = Long.parseLong(idStr);
        CustomerDto customerDto = customerController.findById(id);
        System.out.println("Here customer with id = " + id);
        System.out.println(customerDto);
    }

    private void processGetAll() {
        System.out.println(MESSAGE_TO_INPUT_PAGE_SIZE);
        String pageSizeStr = scanner.next();
        String[] pageSizeArray = pageSizeStr.split(",");
        int page = Integer.parseInt(pageSizeArray[0]);
        int size = Integer.parseInt(pageSizeArray[1]);
        List<CustomerDto> list = customerController.findAll(page, size);
        System.out.println("Here customers you wanted:\n");
        list.forEach(System.out::println);
    }

    private void processCreate() {
        System.out.println(MESSAGE_FOR_CREATE);
        String json = scanner.next();
        CustomerDto customerDto = jsonParser.read(json, CustomerDto.class);
        CustomerDto created = customerController.create(customerDto);
        System.out.println("You created new customer successfully.\nHere it is:\n" + created);
    }

    @Override
    public void close() {
        scanner.close();
    }
}
