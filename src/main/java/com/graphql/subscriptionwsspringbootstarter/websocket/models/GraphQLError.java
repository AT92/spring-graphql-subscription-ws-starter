package com.graphql.subscriptionwsspringbootstarter.websocket.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLError {
    private Integer status;
    private String message;
}
