import java.util.Random;
import java.util.concurrent.*;

//House описывает дом, в котором находятся 2 лифта
public class House {
    // Всего домов в доме
    private int totalFloors = 10; //total number of floors

    // Сколько всего будет вызовов
    private int totalCalls = 30; //total number of calls

    // Сколько всего будет лифтов
    private int totalElevators = 2; //total number of elevators

    // Конкуррентная очередь, состоящая из вызовов
    private final ConcurrentLinkedQueue<Call> calls =
            new ConcurrentLinkedQueue<>(); //thread-save queue of calls

    public void startWorking() throws InterruptedException {
        // Создаем наши лифты
        var elevatorFirst = new Elevator(calls);
        var elevatorSecond = new Elevator(calls);

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
        System.out.println("Лифт 1 начал работать");
        t2.start();
        System.out.println("Лифт 2 начал работать");
        t3.start();
        System.out.println("Пассажиры начали вызывать лифты!");

        t1.join();
        t2.join();
        t3.join();
    }

    // addCalls добавляет вызовы в очередь с рандомными временными промежутками и рандомными этажами
    private void addCalls() throws InterruptedException {
        var r = new Random();
            for (int i = 0; i < totalCalls; i++) {
                var timePeriod = r.nextInt(1, 10) + 1;
                Thread.sleep(timePeriod * 500);

                // Получаем рандомные этажи
                var calledFloor = r.nextInt(1, totalFloors) + 1;
                var nextFloor = r.nextInt(1, totalFloors) + 1;
                nextFloor = nextFloor == calledFloor ? 0 : nextFloor;

                System.out.printf("Новый вызов лифта! С %d на %d этаж\n", calledFloor, nextFloor);
                // Добавляем в очередь
                calls.add(new Call(calledFloor, nextFloor, i));
            }

        }
}

