package com.graphql.subscriptionwsspringbootstarter.websocket.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphQLOperationResponse {
    private GraphQLOperationResponseType type;
    private String id;
    private GraphQLResponsePayload payload;
}
