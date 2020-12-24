package com.graphql.subscriptionwsspringbootstarter.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLOperationRequest;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLOperationRequestType;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLOperationResponse;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLOperationResponseType;
import com.graphql.subscriptionwsspringbootstarter.websocket.models.GraphQLQueryKey;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class GraphQLWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final AtomicReference<List<GraphQLWebsocketSubscriber>> subscriptionRefs = new AtomicReference<>(new LinkedList<>());
    private final GraphQL graphQL;

    private static String unescapeString(String response) {
        String responseParsed = StringEscapeUtils
                .unescapeJson(response)
                .replaceAll("\n", "")
                .replaceAll("\r\n", "")
                .replaceAll("\t", "")
                .replaceFirst("\"", "");
        return responseParsed.endsWith("\"") ? responseParsed.substring(0, responseParsed.length() - 1) : responseParsed;
    }


    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws IOException {
        String graphqlQuery = message.getPayload();
        GraphQLOperationRequest graphQLOperationRequest = objectMapper.readValue(graphqlQuery, GraphQLOperationRequest.class);
        if (graphQLOperationRequest == null || graphQLOperationRequest.getType() == null) {
            log.error("Received following request without type {}", graphQLOperationRequest);
            GraphQLOperationResponse operationResponse = GraphQLOperationResponse
                    .builder()
                    .type(GraphQLOperationResponseType.GQL_ERROR)
                    .build();
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(operationResponse)));
        } else if (GraphQLOperationRequestType.GQL_CONNECTION_INIT == graphQLOperationRequest.getType()) {
            GraphQLOperationResponse operationResponse = GraphQLOperationResponse
                    .builder()
                    .type(GraphQLOperationResponseType.GQL_CONNECTION_ACK)
                    .build();
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(operationResponse)));
        } else if (GraphQLOperationRequestType.GQL_START == graphQLOperationRequest.getType()) {
            subscribeToQueryExecutionStream(webSocketSession, graphQLOperationRequest);
        } else if (GraphQLOperationRequestType.GQL_STOP == graphQLOperationRequest.getType()) {
            unsubscribeFromQueryStream(webSocketSession, graphQLOperationRequest);
        } else if (GraphQLOperationRequestType.GQL_CONNECTION_TERMINATE == graphQLOperationRequest.getType()) {
            webSocketSession.close(CloseStatus.NORMAL);
        }
    }

    private void unsubscribeFromQueryStream(WebSocketSession webSocketSession, GraphQLOperationRequest graphQLOperationRequest) throws IOException {
        if (graphQLOperationRequest.getId() == null) {
            GraphQLOperationResponse operationResponse = GraphQLOperationResponse
                    .builder()
                    .type(GraphQLOperationResponseType.GQL_ERROR)
                    .build();
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(operationResponse)));
        } else {
            List<GraphQLWebsocketSubscriber> graphQLWebSocketSubscribers = subscriptionRefs.get();
            GraphQLQueryKey key = GraphQLQueryKey
                    .builder()
                    .queryId(graphQLOperationRequest.getId())
                    .sessionId(webSocketSession.getId())
                    .build();
            graphQLWebSocketSubscribers
                    .stream()
                    .filter(graphQLWebSocketSubscriber -> graphQLWebSocketSubscriber.getGraphQLQueryKey().equals(key))
                    .findFirst()
                    .ifPresent(GraphQLWebsocketSubscriber::shutdown);
            graphQLWebSocketSubscribers = graphQLWebSocketSubscribers
                    .stream()
                    .filter(graphQLWebsocketSubscriber -> !graphQLWebsocketSubscriber.getGraphQLQueryKey().equals(key))
                    .collect(Collectors.toList());
            subscriptionRefs.set(graphQLWebSocketSubscribers);
        }
    }

    private void subscribeToQueryExecutionStream(WebSocketSession webSocketSession, GraphQLOperationRequest graphQLOperationRequest) {
        ExecutionInput executionInput = ExecutionInput
                .newExecutionInput()
                .variables(graphQLOperationRequest.getPayload().getVariables())
                .query(unescapeString(graphQLOperationRequest.getPayload().getQuery()))
                .operationName(graphQLOperationRequest.getPayload().getOperationName())
                .build();
        ExecutionResult executionResult = graphQL.execute(executionInput);
        Publisher<ExecutionResult> valuesStream = executionResult.getData();
        if (valuesStream != null) {
            GraphQLQueryKey graphQLQueryKey = GraphQLQueryKey
                    .builder()
                    .queryId(graphQLOperationRequest.getId())
                    .sessionId(webSocketSession.getId())
                    .build();
            GraphQLWebsocketSubscriber graphQLWebsocketSubscriber = new GraphQLWebsocketSubscriber(graphQLQueryKey, webSocketSession, objectMapper, graphQLOperationRequest.getId());
            List<GraphQLWebsocketSubscriber> graphQLWebsocketSubscribers = subscriptionRefs.get();
            graphQLWebsocketSubscribers.add(graphQLWebsocketSubscriber);
            subscriptionRefs.set(graphQLWebsocketSubscribers);
            valuesStream.subscribe(graphQLWebsocketSubscriber);
        } else {
            log.error("Could not get values stream, got following execution result {}", executionResult);
        }
    }
}
