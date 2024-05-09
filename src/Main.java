import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var house = new House();

        // Запускаем работу лифтов и пассажиров
        house.startWorking();
    }
}