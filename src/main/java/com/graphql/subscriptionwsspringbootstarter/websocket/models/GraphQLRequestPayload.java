package com.graphql.subscriptionwsspringbootstarter.websocket.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphQLRequestPayload {
    private Map<String, Object> variables;
    private String query;
    private String operationName;
}
