package ru.clevertec.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ru.clevertec.factory.BeanFactory;
import ru.clevertec.web.command.Command;

/**
 * Front controller for application
 */
@WebServlet("/")
public class Controller extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Command command = getCommand(req);
        String result = command.execute(req, resp);
        sendResponse(resp, result);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Command command = getCommand(req);
        String result = command.execute(req, resp);
        sendResponse(resp, result);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Command command = getCommand(req);
        String result = command.execute(req, resp);
        sendResponse(resp, result);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Command command = getCommand(req);
        command.execute(req, resp);
    }

    private void sendResponse(HttpServletResponse resp, String result) throws IOException {
        resp.getWriter().print(result);
    }

    private Command getCommand(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String command = uri.replaceAll("^/|/.*$", "");
        BeanFactory factory = BeanFactory.INSTANCE;
        return factory.getCommand(command);
    }
}
