// Call описывает вызов лифта
public class Call {
    public int calledFloor; // Этаж с которого был вызван лифт
    public int nextFloor; // Этаж на который нужно ехать
    public int id; // ID пассажира
    public Call(int calledFloor, int nextFloor, int id) {
        this.calledFloor = calledFloor;
        this.nextFloor = nextFloor;
        this.id = id;
    }
}
