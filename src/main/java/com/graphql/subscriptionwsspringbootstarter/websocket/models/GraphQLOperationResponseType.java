package com.graphql.subscriptionwsspringbootstarter.websocket.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GraphQLOperationResponseType {
    @JsonProperty("connection_error")
    GQL_CONNECTION_ERROR,
    @JsonProperty("connection_ack")
    GQL_CONNECTION_ACK,
    @JsonProperty("data")
    GQL_DATA,
    @JsonProperty("error")
    GQL_ERROR,
    @JsonProperty("complete")
    GQL_COMPLETE
}
