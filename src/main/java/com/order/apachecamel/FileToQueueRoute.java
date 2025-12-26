package com.order.apachecamel;

import com.order.model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileToQueueRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(FileToQueueRoute.class);

    @Override
    public void configure() {

        // Move invalid files to error folder
        errorHandler(deadLetterChannel("file:error/orders")
                .logHandled(true));

        from("file:input/orders?noop=false")
            .routeId("file-to-queue")
            .log("Processing file: ${header.CamelFileName}")

            // JSON (file) -> Order
            .unmarshal("orderJacksonDataFormat")

            // Validate order
            .process(exchange -> {
                Order order = exchange.getIn().getBody(Order.class);

                if (order.getOrderId() == null ||
                    order.getCustomerId() == null ||
                    order.getAmount() <= 0) {
                    throw new IllegalArgumentException("Invalid Order Data");
                }

                log.info("Validated OrderId={}", order.getOrderId());
            })

            // Order -> JSON (IMPORTANT)
            .marshal().json()

            // Send JSON as TextMessage (CRITICAL FIX)
            .to("activemq:queue:ORDER.CREATED.QUEUE?jmsMessageType=Text")

            .log("Order sent to queue | OrderId=${body}");
    }
}
