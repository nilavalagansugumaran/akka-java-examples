
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    public interface Command extends Serializable{}

    public static class StartCommand implements RaceController.Command {
        private static final long serialVersionUID = 1L;

        public StartCommand() {
        }
    }

    public static class RacerUpdateCommand implements RaceController.Command {
        private static final long serialVersionUID = 1L;

        private ActorRef<Race.Command> racer;
        private int position;

        public RacerUpdateCommand(ActorRef<Race.Command> racer, int position) {
            this.racer = racer;
            this.position = position;
        }

        public ActorRef<Race.Command> getRacer() {
            return racer;
        }

        public int getPosition() {
            return position;
        }
    }

    private  class GetPositionsCommand implements RaceController.Command {
        private static final long serialVersionUID = 1L;
    }

    private Map<ActorRef<Race.Command>, Integer>  currentPositions;
    private long start;
    private int raceLength = 100;
    private Object TIMER_KEY;

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<RaceController.Command> create() {
        return Behaviors.setup(RaceController::new);
    }

    private void displayRace() {
         int displayLength = 160;
        for (int i = 0; i < 50; ++i) System.out.println();
        System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
        System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
        int i =0;
        for(ActorRef<Race.Command> racer: currentPositions.keySet()){
            System.out.println(i + " : "  + new String (new char[currentPositions.get(racer) * displayLength / 100]).replace('\0', '*'));
            i++;
        }
    }

    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, msg -> {
                    start = System.currentTimeMillis();
                    currentPositions = new HashMap<>();
                    for (int i =0; i<10; i++) {
                        ActorRef<Race.Command> ref = getContext().spawn(Race.create(), "race-"+i);
                        currentPositions.put(ref, 0);
                        ref.tell(new Race.StartCommand(raceLength));
                    }
                    return Behaviors.withTimers(timer -> {
                        timer.startTimerAtFixedRate(TIMER_KEY, new GetPositionsCommand(), Duration.ofSeconds(1));
                        return this;
                    });
                })
                .onMessage(GetPositionsCommand.class, msg -> {
                    for(ActorRef<Race.Command> racer: currentPositions.keySet()){
                        racer.tell(new Race.PositionCommand(getContext().getSelf()));
                        displayRace();
                    }
                    return this;
                })
                .onMessage(RacerUpdateCommand.class, msg -> {
                    currentPositions.put(msg.getRacer(), msg.getPosition());
                    return this;
                })
                .build();
    }


}
