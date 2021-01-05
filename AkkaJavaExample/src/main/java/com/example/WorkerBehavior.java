package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {
    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    private BigInteger localPrime;
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onAnyMessage(msg -> {
            if(msg.getMessage().equalsIgnoreCase("start")) {
                System.out.println("starting work at " + getContext().getSelf().path());
                if(localPrime == null) {
                    BigInteger value = new BigInteger(2000, new Random());
                    localPrime = value.nextProbablePrime();
                    System.out.println("created local prime at - " + getContext().getSelf().path());
                } else {
                    System.out.println("local prime already exists at - " + getContext().getSelf().path());
                }

                msg.getActorRef().tell(new ManagerBehavior.ResultCommand(localPrime));
            }
            return this;
        }).build();
    }

    public static class Command implements Serializable {
        private static final long serialVersionUID = 1L;

        private String message;
        private ActorRef<ManagerBehavior.Command> actorRef;

        public Command(String message, ActorRef<ManagerBehavior.Command> actorRef) {
            this.message = message;
            this.actorRef = actorRef;
        }

        public String getMessage() {
            return message;
        }

        public ActorRef<ManagerBehavior.Command> getActorRef() {
            return actorRef;
        }
    }
}
