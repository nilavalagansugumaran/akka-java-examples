import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class Race extends AbstractBehavior<Race.Command> {
    public interface Command extends Serializable{}

    public static class StartCommand implements Race.Command {
        private static final long serialVersionUID = 1L;

        private int raceLength;

        public StartCommand(int raceLength) {
            this.raceLength = raceLength;
        }
        public int getRaceLength() {
            return raceLength;
        }

    }

    public static class PositionCommand implements Race.Command {
        private static final long serialVersionUID = 1L;

        private ActorRef<RaceController.Command> controller;

        public PositionCommand(ActorRef<RaceController.Command> controller) {
            this.controller = controller;
        }

        public ActorRef<RaceController.Command> getController() {
            return controller;
        }

    }

    private Race(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Race::new);
    }

    private final double defaultAverageSpeed = 48.2;
    private int averageSpeedAdjustmentFactor;
    private Random random;

    private double currentSpeed = 0;
    private double currentPosition = 0;
    private int raceLength;

    private double getMaxSpeed() {
        return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return currentSpeed * 1000 / 3600;
    }

    private void determineNextSpeed() {
        if (currentPosition < (raceLength / 4)) {
            currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
        }
        else {
            currentSpeed = currentSpeed * (0.5 + random.nextDouble());
        }

        if (currentSpeed > getMaxSpeed())
            currentSpeed = getMaxSpeed();

        if (currentSpeed < 5)
            currentSpeed = 5;

        if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
            currentSpeed = getMaxSpeed() / 2;
        }
    }

    public Receive<Race.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> {
                    this.raceLength = msg.getRaceLength();
                    this.random = new Random();
                    averageSpeedAdjustmentFactor = random.nextInt(30) - 10;

                    return this;
                })
                .onMessage(PositionCommand.class, msg -> {

                    determineNextSpeed();
                    currentPosition += getDistanceMovedPerSecond();
                    if (currentPosition > raceLength )
                        currentPosition  = raceLength;

                    msg.getController().tell(new RaceController.RacerUpdateCommand(getContext().getSelf(), (int)currentPosition));
                    return this;

                })
                .build();
    }

}
