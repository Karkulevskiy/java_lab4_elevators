import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

// Состояния, в которых может быть лифт
enum State {
    IDLE, // Лифт ждет нового вызова
    MOVE //Лифт в процессе движения
}
public class Elevator implements Runnable{
    // Внутрення очередь запросов
    private ConcurrentLinkedQueue<Call> callsQueue = new ConcurrentLinkedQueue<>();

    // Скорость лифта в доме
    private final int DELAY = 700;

    // Общий ресурс, откуда берутся запросы
    private final ConcurrentLinkedQueue<Call> calls;

    // Текущее состояние лифта
    private State state = State.IDLE;

    // Текущий этаж
    private int currentFloor = 0;

    public Elevator(ConcurrentLinkedQueue<Call> calls) {
        this.calls = calls;
    }

    public void run() {
        while (true) {
            switch (state) {
                case IDLE:
                    idle();
                    break;
                case MOVE:
                    try {
                        move();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }
    }

    // move - лифт обрабатывает запрос
    private void move() throws InterruptedException {
        // Берем первый запрос из внутренней очереди
        var currCall = callsQueue.poll();

        // Смотрим на какой этаж ехать за пассажиром(вверх или вниз)
        if (currentFloor < currCall.calledFloor)
            moveUP(currCall.calledFloor);
        else
            moveDOWN(currCall.calledFloor);

        System.out.printf("%s приехал на %d этаж, взял пассажира %d\n",
                Thread.currentThread().getName(),
                currentFloor,
                currCall.id);

        // Смотрим куда на какой этаж везти пассажира
        if (currentFloor < currCall.nextFloor)
            moveUP(currCall.nextFloor);
         else
            moveDOWN(currCall.nextFloor);

        System.out.printf("%s приехал на %d этаж\n", Thread.currentThread().getName(), currentFloor);
        System.out.printf("%s: пассажир %d вышел\n", Thread.currentThread().getName(), currCall.id);

        state = callsQueue.isEmpty() ? State.IDLE : State.MOVE;
        if (state == State.IDLE) {
            return; // Если в лифте больше нету пассажиров, то переводим его в состояние ожидания
        }

        //Если в лифте остались еще пассажиры, то будем их развозить и искать попутчиков
        while(!callsQueue.isEmpty()) {

            // Достаем пассажира из внутренней очереди
            var call = callsQueue.poll();

            // Если попутчик выходит на этаже, где стоит лифт, то выпускаем его
            if (call.nextFloor == currentFloor) {
                System.out.printf("%s: пассажир: %d вышел\n", Thread.currentThread().getName(), call.id);
                continue;
            }

            // Везем пассажира на нужный этаж
            if (currentFloor < call.nextFloor)
                moveUP(call.nextFloor);
            else
                moveDOWN(call.nextFloor);

            System.out.printf("%s приехал на %d этаж\n", Thread.currentThread().getName(), currentFloor);
            System.out.printf("%s: пассажир %d вышел\n", Thread.currentThread().getName(), call.id);
        }

        // Переводим лифт в состояние ожидания
        state = State.IDLE;
    }

    // Лифт движется вверх
    private void moveUP(int nextFloor) throws InterruptedException {
        for (; currentFloor < nextFloor; currentFloor++) {
            System.out.printf("%s: ЭТАЖ %d\n", Thread.currentThread().getName(), currentFloor);
            // Подбираем попутчиков
            getCompanions(currentFloor);
            Thread.sleep(DELAY);
        }
    }

    // Лифт движется вниз
    private void moveDOWN(int nextFloor) throws InterruptedException {
        for (; currentFloor > nextFloor; currentFloor--) {
            System.out.printf("%s: ЭТАЖ %d\n", Thread.currentThread().getName(), currentFloor);
            Thread.sleep(DELAY);
        }
    }

    // Состояние ожидания
    private void idle() {
        Call call = null;

        // Спрашиваем, есть ли новый запрос
        while (call == null) {
            call = calls.poll();
        }

        // Запрос появился, переводим лифт в состояние движения
        state = State.MOVE;

        System.out.printf("%s поехал за пассажиром %d (с %d на %d этаж)\n",
                Thread.currentThread().getName(),
                call.id,
                call.calledFloor,
                call.nextFloor);

        // Добавляем во внутреннюю очередь
        callsQueue.add(call);
    }

    // Подбираем попутчиков
    private void getCompanions(int floor) {
        var tempCalls = new LinkedList<Call>();

        // Здесь мы получаем из очереди всех попутчиков на этаже.
        // Так как у нас идет борьба за ресурс, то я синхронизирую на каком то лифте
        // проверку попутчиков.
        // Также так как я использовал очередь, то мне пришлось создавать временную очередь
        // и перекладывать в нее попутчиков, а потом обратно восстанавливать главную очередь
        // Это плохой подход, но я не придумал как сделать лучше.
        synchronized (calls) {
            while (!calls.isEmpty()) {
                var call = calls.poll();
                if (call.calledFloor == floor) {
                    System.out.printf("%s взял попутчика %d (c %d на %d этаж)\n",
                            Thread.currentThread().getName(),
                            call.id,
                            call.calledFloor,
                            call.nextFloor);

                    callsQueue.add(call);
                } else {
                    tempCalls.add(call);
                }
            }
            while (!tempCalls.isEmpty()) {
                calls.add(tempCalls.poll());
            }
        }
    }
}
