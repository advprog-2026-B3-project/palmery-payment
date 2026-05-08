package id.ac.ui.cs.advprog.palmerypayment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerConfig {

    @Bean
    TopicExchange palmeryExchange(@Value("${app.broker.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue paymentEventsQueue(@Value("${app.broker.payment-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding paymentEventsBinding(
            Queue paymentEventsQueue,
            TopicExchange palmeryExchange,
            @Value("${app.broker.payment-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(paymentEventsQueue).to(palmeryExchange).with(routingKey);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
