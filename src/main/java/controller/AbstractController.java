package controller;

import util.HttpRequest;
import util.HttpResponse;

public abstract class AbstractController implements Controller {

    abstract void doPost(HttpRequest request , HttpResponse response);

    abstract void doGet(HttpRequest request , HttpResponse response);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if (request.getMethod().equals("GET")) {
            doGet(request, response);
        } else if (request.getMethod().equals("POST")) {
            doPost(request, response);
        }

    }
}
