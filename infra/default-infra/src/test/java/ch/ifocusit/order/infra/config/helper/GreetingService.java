package ch.ifocusit.order.infra.config.helper;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GreetingService {
    public String sayHello() {
        return "Hello World!";
    }
}
