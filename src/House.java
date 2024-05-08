import java.util.Random;
import java.util.concurrent.*;

public class House {
    private int totalFloors = 10; //total number of floors

    private int totalCalls = 30; //total number of calls

    private int totalElevators = 2; //total number of elevators

    private final ConcurrentLinkedQueue<Call> calls =
            new ConcurrentLinkedQueue<>(); //thread-save queue of calls

    private final ConcurrentHashMap<Call, Boolean> history =
            new ConcurrentHashMap<>();

    public void startWorking() {
        var elevatorFirst = new Elevator(calls, history);
        var elevatorSecond = new Elevator(calls, history);

        var t1 = new Thread(elevatorFirst); //starting first elevator
        t1.setName("Лифт 1");

        var t2 = new Thread(elevatorSecond); //starting second elevator
        t2.setName("Лифт 2");

        var t3 = new Thread(() -> {
            try {
                addCalls();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }); // starting random adding calls (calledFloor, nextFloor)

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            System.out.println("\nЗакончились пассажиры\n");
        }
    }

    private void addCalls() throws InterruptedException {
        var r = new Random();
            for (int i = 0; i < totalCalls; i++) {
                var timePeriod = r.nextInt(1, 100) + 1;
                Thread.sleep(timePeriod * 1000L);

                var calledFloor = r.nextInt(1, totalFloors) + 1;
                var nextFloor = r.nextInt(1, totalFloors) + 1;
                nextFloor = nextFloor == calledFloor ? 0 : nextFloor;

                calls.add(new Call(calledFloor, nextFloor));
                System.out.printf("Новый вызов лифта! С %d на %d\n", calledFloor, nextFloor);
            }
        }
}

