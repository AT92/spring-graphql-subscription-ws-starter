package com.graphql.subscriptionwsspringbootstarter.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLOperationResponse;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLOperationResponseType;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLQueryKey;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLResponsePayload;
import graphql.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
public class GraphQLWebsocketSubscriber implements Subscriber<ExecutionResult> {
    private final GraphQLQueryKey graphQLQueryKey;
    private final WebSocketSession webSocketSession;
    private final ObjectMapper objectMapper;
    private final String queryId;
    private AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

    GraphQLQueryKey getGraphQLQueryKey() {
        return graphQLQueryKey;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscriptionRef.set(subscription);
        request();
    }

    @Override
    public void onNext(ExecutionResult executionResult) {
        try {
            Object executionResultData = executionResult.getData();
            log.trace("Sending values {} to session with id {}", executionResultData, webSocketSession.getId());
            if (webSocketSession.isOpen()) {
                GraphQLOperationResponse graphQLOperationResponse = GraphQLOperationResponse
                        .builder()
                        .id(queryId)
                        .type(GraphQLOperationResponseType.GQL_DATA)
                        .payload(GraphQLResponsePayload
                                .builder()
                                .data(executionResultData)
                                .build())
                        .build();
                webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(graphQLOperationResponse)));
            } else {
                shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        request();
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Subscription threw an exception", throwable);
        try {
            shutdown();
            webSocketSession.close();
        } catch (IOException e) {
            log.error("Unable to close websocket session", e);
        }
    }

    @Override
    public void onComplete() {
        log.debug("Websocket subscription for session {} completed", webSocketSession.getId());
        try {
            shutdown();
            webSocketSession.close();
        } catch (IOException e) {
            log.error("Unable to close websocket session {} {}", webSocketSession.getId(), e);
        }
    }

    void shutdown() {
        subscriptionRef.get().cancel();
    }

    private void request() {
        this.subscriptionRef.get().request(1);
    }
}
