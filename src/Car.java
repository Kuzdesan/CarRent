
public class Car {
    public String model;
    public String number;
    AuthorizedUser owner;
    String ownerLogin;
    public int numberOfSeats;

    /**
     * Constructor for creating a Car by object of AuthorizedUser class
     */
    public Car(String number, String model, int seats, AuthorizedUser owner) {
        this.owner = owner;
        this.model = model;
        this.numberOfSeats = seats;
        this.number = number;
    }

    /**
     * Constructor for creating a Car by a String login of its owner
     */
    public Car(String number, String model, int seats, String ownerLogin){
        this.ownerLogin = ownerLogin;
        this.model = model;
        this.numberOfSeats = seats;
        this.number = number;
    }

    @Override
    public String toString() {
        return ("Model: \t" + this.model + "\nNumber of seats: \t" + this.numberOfSeats + "\nCar number: \t" + this.number + "\n");
    }
}
