package controllers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.BoundRequestBuilder;
import play.shaded.ahc.org.asynchttpclient.ListenableFuture;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocket;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocketListener;
import play.shaded.ahc.org.asynchttpclient.ws.WebSocketUpgradeHandler;

public class WebSocketClient {

    private AsyncHttpClient client;

    public WebSocketClient(AsyncHttpClient c) {
        this.client = c;
    }

    public CompletableFuture<WebSocket> call(String url, String origin, WebSocketListener listener) throws ExecutionException, InterruptedException {
        final BoundRequestBuilder requestBuilder = client.prepareGet(url).addHeader("Origin", origin);

        final WebSocketUpgradeHandler handler = new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build();
        final ListenableFuture<WebSocket> future = requestBuilder.execute(handler);
        final CompletableFuture<WebSocket> completableFuture = future.toCompletableFuture();
        return completableFuture;
    }

    static class LoggingListener implements WebSocketListener {

        private Throwable throwableFound = null;

        public Throwable getThrowable() {
            return throwableFound;
        }

        public void onOpen(WebSocket websocket) {}

        public void onClose(WebSocket websocket) {}

        public void onError(Throwable t) {
            throwableFound = t;
        }
    }

}
