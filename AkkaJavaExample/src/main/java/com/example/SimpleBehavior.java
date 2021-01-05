package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SimpleBehavior extends AbstractBehavior<String> {


    private SimpleBehavior(ActorContext<String> context) {
        super(context);
    }

    public static Behavior<String> create(){

        return Behaviors.setup(SimpleBehavior::new);
    }

    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("say hello", () -> {
                    System.out.println("Hello mate");
                    return this;
                })
                .onMessageEquals("path", () -> {
                    System.out.println("path is " + getContext().getSelf().path());
                    System.out.println("path narrow is " + getContext().getSelf().narrow());
                    return this;
                })
                .onMessageEquals("create another", () -> {
                    ActorRef<String> ref = getContext().spawn(SimpleBehavior.create(), "second-actor");
                    ref.tell("path");
                    return this;
                })
                .onAnyMessage(msg -> {
                    System.out.println("Hello " + msg);
                    return this;
                })
                .build();
    }
}
