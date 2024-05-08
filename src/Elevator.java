import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

enum State {
    UP, DOWN, IDLE
}
public class Elevator implements Runnable{
    private ConcurrentLinkedQueue<Call> callsQueue = new ConcurrentLinkedQueue<>();

    private final int DELAY = 500;

    private ConcurrentLinkedQueue<Call> calls;

    private ConcurrentHashMap<Call, Boolean> history;

    private State state = State.IDLE;

    private int currentFloor = 0;

    public Elevator(ConcurrentLinkedQueue<Call> calls,
                    ConcurrentHashMap<Call, Boolean> history) {
        this.history = history;
        this.calls = calls;
    }

    public void run() {
        while (true) {
            switch (state) {
                case IDLE:
                    idle();
                    break;
                case UP:
                    up();
                    break;
                case DOWN:
                    try {
                        down();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        }
    }

    private void down() throws InterruptedException {
        var currCall = callsQueue.poll();

        for (; currentFloor > currCall.nextFloor; currentFloor--) {
            System.out.printf("%s: ЭТАЖ %d\n", Thread.currentThread().getName(), currentFloor);
            Thread.sleep(DELAY);
        }

        System.out.printf("%s: ЗАКОНЧИЛ\n", Thread.currentThread().getName());
        state = State.IDLE;
    }

    private void idle() {
        Call call = null;
        while (call == null) {
            call = calls.poll();

            if (call != null && call.calledFloor == -1) {
                call = null;
            }
        }

        if (call.calledFloor > currentFloor) {
            state = State.UP;
        } else if (call.calledFloor < currentFloor) {
            state = State.DOWN;
        } else {
            // Если вызвали на этаже, где был лифт и никуда не едем
            if (call.nextFloor == currentFloor) return;
            state = call.calledFloor < call.nextFloor ? State.UP : State.DOWN;
        }
        System.out.printf("%s взял заказ\n", Thread.currentThread().getName());
        callsQueue.add(call);
    }

    private void up() {
       // var companions = getCompanions(currentFloor);
    }

    private void getCompanions(int floor) {
        for (var call : calls) {
            if (call.calledFloor == floor) {
                callsQueue.add(call);
            }
            else calls.remove(call);
        }
    }
}
