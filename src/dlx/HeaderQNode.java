package dlx;

public class HeaderQNode extends QNode {

    public String name;
    public int cnt = 0;
    public boolean primary;

    public HeaderQNode leftQNode;
    public HeaderQNode rightQNode;

    public HeaderQNode(){}

    public HeaderQNode(String name){
        this.name = name;
        super.indicator = 0;
        this.primary = true;
    }

    public HeaderQNode(String name, int indicator, boolean primary){
        this.name = name;
        super.indicator = indicator;
        this.primary = primary;
    }
}
