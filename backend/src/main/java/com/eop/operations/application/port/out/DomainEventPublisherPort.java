package com.eop.operations.application.port.out;

public interface DomainEventPublisherPort {
    void publish(Object event);
}
