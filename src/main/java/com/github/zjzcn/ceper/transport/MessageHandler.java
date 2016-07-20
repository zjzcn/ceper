package com.github.zjzcn.ceper.transport;

public interface MessageHandler {

    Response handleRequest(Request request);

    void handleResponse(Response response);
}
