package org.fungover.zipp.graphql.controller;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GraphQLControllerDummy {

    @QueryMapping
    public String hello() {
        return "Hello from Zipp GraphQL!";
    }
}
