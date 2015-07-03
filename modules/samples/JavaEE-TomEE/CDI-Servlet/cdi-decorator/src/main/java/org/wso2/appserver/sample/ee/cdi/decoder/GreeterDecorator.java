package org.wso2.appserver.sample.ee.cdi.decoder;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
public class GreeterDecorator implements Greeter {

    @Inject
    @Delegate
    Greeter greeter;

    @Override
    public String greet() {
        return greeter.greet() + "\nThis line is generated by the decorator";
    }
}