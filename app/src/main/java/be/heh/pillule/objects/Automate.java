package be.heh.pillule.objects;

/**
 * Created by gquittet on 12/12/17.
 */

public class Automate {

    private String name;
    private String ip;
    private int rack;
    private int slot;
    private int databloc;
    private int type;

    public Automate(String name, String ip, int rack, int slot, int databloc) {
        this.name = name;
        this.ip = ip;
        this.rack = rack;
        this.slot = slot;
        this.databloc = databloc;
    }

    public Automate(String name, String ip, int rack, int slot, int databloc, int type) {
        this(name, ip, rack, slot, databloc);
        this.type = type;
    }

    public void connect() {

    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getRack() {
        return rack;
    }

    public void setRack(int rack) {
        this.rack = rack;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getDatabloc() {
        return databloc;
    }

    public void setDatabloc(int databloc) {
        this.databloc = databloc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
