package com.eop.operations.infrastructure.messaging;

import com.eop.operations.application.port.out.DomainEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SpringDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher, "applicationEventPublisher cannot be null");
    }

    @Override
    public void publish(Object event) {
        Objects.requireNonNull(event, "Domain event to publish cannot be null");
        applicationEventPublisher.publishEvent(event);
    }
}
