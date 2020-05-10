package dlx;

public class QNode {

    public int indicator;

    public QNode leftQNode;
    public QNode rightQNode;
    public QNode upperQNode;
    public QNode lowerQNode;
    public HeaderQNode headerQNode;

    public QNode(){}

    public QNode(int indicator){
        this.indicator = indicator;
    }
}
