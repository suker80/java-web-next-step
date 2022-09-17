package controller;

import util.HttpMethod;
import util.HttpRequest;
import util.HttpResponse;

public abstract class AbstractController implements Controller {

    abstract void doPost(HttpRequest request, HttpResponse response);

    abstract void doGet(HttpRequest request, HttpResponse response);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        if (httpMethod == HttpMethod.GET) {
            doGet(request, response);
        } else if (httpMethod == HttpMethod.POST) {
            doPost(request, response);
        }

    }
}
