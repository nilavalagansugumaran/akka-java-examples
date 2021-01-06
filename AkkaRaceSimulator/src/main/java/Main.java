import akka.actor.typed.ActorSystem;

public class Main {

    public static void main(String[] args) {
        ActorSystem<RaceController.Command> system = ActorSystem.create(RaceController.create(), "main");
        system.tell(new RaceController.StartCommand());
    }
}
