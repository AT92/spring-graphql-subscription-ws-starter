
# spring-graphql-subscription-ws-starter

This project enables graphql ws protocol (https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md#gql_data) over websockets for spring boot.

So if you want to use graphql subscriptions over websockets with apollo client or the urql client and spring boot on the backend, 
you just have to include this dependency and assign your datafetchers to the RuntimeWiring. 
The datafetchers has to return a Publisher or a Flux containing the payload.

```
private RuntimeWiring buildWiring() {
        return RuntimeWiring
                .newRuntimeWiring()
                .type(newTypeWiring("Subscription")
                        .dataFetcher("measurementsBy", measurementDataFetcher.getMeasurementPublisher()))
                .build();
```

## Configuration
```
graphql.websockets.path=/path # is the path to open websocket (defualt is set to to /graphql-ws)
```
