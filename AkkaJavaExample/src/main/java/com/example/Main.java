package com.example;

import akka.actor.typed.ActorSystem;

public class Main {
    //    public static void main(String[] args) {
//      ActorSystem<String> system =  ActorSystem.create(SimpleBehavior.create(), "simple-actor");
//      system.tell("Nila");
//      system.tell("Akil");
//        system.tell("say hello");
//        system.tell("path");
//        system.tell("create another");
//      system.terminate();
//    }
    public static void main(String[] args) {

        ActorSystem<ManagerBehavior.Command> sys = ActorSystem.create(ManagerBehavior.create(), "manager");
        sys.tell(new ManagerBehavior.InstructionCommand("start"));
        //sys.terminate();
    }
}
