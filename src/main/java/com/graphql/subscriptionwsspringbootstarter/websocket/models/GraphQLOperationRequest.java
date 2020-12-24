package com.graphql.subscriptionwsspringbootstarter.websocket.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphQLOperationRequest {
    private GraphQLRequestPayload payload;
    private String id;
    private GraphQLOperationRequestType type;
}
