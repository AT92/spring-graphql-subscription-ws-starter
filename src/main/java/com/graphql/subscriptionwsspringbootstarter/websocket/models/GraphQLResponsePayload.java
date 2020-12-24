package com.graphql.subscriptionwsspringbootstarter.websocket.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphQLResponsePayload {
    private Object data;
    private List<GraphQLError> errors;
}
