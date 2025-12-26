package com.order.apachecamel;

import com.order.model.Order;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderQueueConsumer extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(OrderQueueConsumer.class);

    @Override
    public void configure() {

        from("activemq:queue:ORDER.CREATED.QUEUE")
            .routeId("queue-consumer")

            // JSON -> Order (with JavaTime support)
            .unmarshal("orderJacksonDataFormat")

            // Process order
            .process(exchange -> {
                Order order = exchange.getIn().getBody(Order.class);

                log.info(
                    "Order processed | OrderId={} | CustomerId={} | Amount={}",
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getAmount()
                );
            });
    }
}
