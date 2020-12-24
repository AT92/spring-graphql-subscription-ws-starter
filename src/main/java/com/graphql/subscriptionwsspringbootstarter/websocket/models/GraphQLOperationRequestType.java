package com.graphql.subscriptionwsspringbootstarter.websocket.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GraphQLOperationRequestType {
    @JsonProperty("connection_init")
    GQL_CONNECTION_INIT,
    @JsonProperty("start")
    GQL_START,
    @JsonProperty("stop")
    GQL_STOP,
    @JsonProperty("connection_terminate")
    GQL_CONNECTION_TERMINATE
}
