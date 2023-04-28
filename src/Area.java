/**
 * Class is used to define zones where car can be given or taken in rent
 */
public class Area {
    int id;
    String location;

    /**
     * @param id a numeric identifier which equals zone's identifier in table Areas of DataBase
     * @param loc a String name of zone
     */
    public Area(int id, String loc) {
        this.id = id;
        this.location = loc;
    }

    @Override
    public String toString() {
        return (this.location);
    }
}
