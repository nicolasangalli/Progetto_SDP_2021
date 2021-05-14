package Dronazon;


public class Order {

    private int id;
    private Coordinate startPoint;
    private Coordinate finishPoint;

    public Order(int i, Coordinate sp, Coordinate fp) {
        this.id = i;
        this.startPoint = sp;
        this.finishPoint = fp;
    }

    public int getId() {
        return id;
    }

    public Coordinate getStartPoint() {
        return startPoint;
    }

    public Coordinate getFinishPoint() {
        return finishPoint;
    }

}
