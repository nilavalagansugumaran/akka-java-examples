package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private SortedSet<BigInteger> primes = new TreeSet<>();

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, cmd -> {
                    if (cmd.getMessage().equalsIgnoreCase("start")) {
                        for (int i = 0; i < 20; i++) {
                            ActorRef<WorkerBehavior.Command> ref = getContext().spawn(WorkerBehavior.create(), "worker-" + i);
                            System.out.println("path = " + ref.path());
                            WorkerBehavior.Command command = new WorkerBehavior.Command("start", getContext().getSelf());
                            ref.tell(command);
                            ref.tell(command);
                        }
                    }
                    return this;
                }).onMessage(ResultCommand.class, cmd -> {
                    primes.add(cmd.getPrime());
                    System.out.println("number of primes = " + primes.size());
                    if (primes.size() == 20) {
                        primes.forEach(System.out::println);
                    }
                    return this;
                }).build();
    }

    public interface Command extends Serializable {
    }

    public static class InstructionCommand implements Command {
        public static final long serialVersionUID = 1L;
        private String message;

        public InstructionCommand(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ResultCommand implements Command {
        public static final long serialVersionUID = 1L;
        private BigInteger prime;

        public ResultCommand(BigInteger prime) {
            this.prime = prime;
        }

        public BigInteger getPrime() {
            return prime;
        }
    }

}
