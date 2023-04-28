import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

public class AuthorizedUser extends User {
    String FIO;
    String login;
    String passport;
    String telephone;
    Date birth;
    Map<Integer, Car> cars = new HashMap<>();
   private Map<Integer, Order> myOrders = new HashMap<>();

    public Map<Integer, Order> getMyOrders() {
        if(refreshOrders()) return myOrders;
        else return null;
    }

    public AuthorizedUser(String log, String name, String passport, String telephone, Date birth) {
        this.FIO = name;
        this.login = log;
        this.passport = passport;
        this.telephone = telephone;
        this.birth = birth;
    }

    @Override
    public String toString() {
        return ("Name: \t" + this.FIO + "\nLogin: \t" + this.login + "\nTelephone number: \t" + this.telephone + "\n");
    }

    public void printAllCars() {
        if (cars.size() == 0) {
            System.out.println("LIST OF ADDED CARS IS EMPTY!");
        } else {
            System.out.println("ALL YOUR CARS: ");
            for (Map.Entry<Integer, Car> pair : cars.entrySet()) {
                System.out.println(pair.getKey() + ".\n" + pair.getValue().toString());
            }
        }
    }

    /**
     * Refreshing the list of orders' history and it's data
     * <p>
     *     As it could be created a new order, or old order could be finished, or smth else, before any action with
     *     the user's order's history this method should be called
     * </p>
     */
    public boolean refreshOrders() {
        myOrders.clear();
        Map<Integer, Order> collectedOrders = Order.collectOrders(BaseAdministration.collectOrders(this), this);
        if (collectedOrders != null) {
            myOrders.putAll(collectedOrders);
            return true;
        }
        return false;
    }

//    public void printAllOrders() {
//        refreshOrders();
//        if (myOrders != null && myOrders.size() != 0) {
//            System.out.println("\n\t\t\tYOUR ORDERS:\n");
//            for (Map.Entry<Integer, Order> pair : myOrders.entrySet()) {
//                System.out.println(pair.getKey() + ".\n" + pair.getValue().toString());
//            }
//        } else System.out.println("ORDERS' HISTORY IS EMPTY");
//    }

    public boolean printUnfinishedOrders() {
        refreshOrders();
        if (myOrders != null && myOrders.size() != 0) {
            System.out.println("\n\t\t\tYOUR UNFINISHED ORDERS:\n");
            int i = 0;
            for (Map.Entry<Integer, Order> pair : myOrders.entrySet()) {
                if (pair.getValue().status.equals("not_finished")) {
                    i++;
                    System.out.println("\t\tORDER NUMBER: " + pair.getKey() + ".\n" + pair.getValue() + "\n\n");
                }
            }
            if (i == 0) {
                System.out.println("\n\t\t\tALL YOUR ORDERS ARE FINISHED:\n");
                return false;
            } return true;
        }
        else System.out.println("ORDERS' HISTORY IS EMPTY");
        return false;
    }

    public boolean addCar(String gosNum, String model, String seats) throws IOException {
        Car createdCar = createCar(gosNum, model, seats);
        if (createdCar != null) {
            System.out.println("CAR IS SUCCESSFULLY ADDED!");
            return true;
        } else {
            System.out.println("CAR ADDING IS FAULT. TRY TO REPEAT!");
            return false;
        }
    }

    public Car createCar(String gosNum, String model, String seats) {
        if (!TechnicalInfo.isCarDataCorrect(gosNum, model, seats)) {
            return null;
        }
        boolean carInBase = BaseAdministration.checkingCarInBase(gosNum);
        Car newCar = null;
        int seatsInt = Integer.parseInt(seats);
        if (!carInBase) {
            boolean successInsert = BaseAdministration.addNewCar(gosNum, model, seatsInt, this.login);
            if (!successInsert) {
                System.out.println("CAR ADDING IS FAULT. TRY TO REPEAT!");
                return null;
            } else {
                newCar = new Car(gosNum, model, seatsInt, this);
                this.cars.put(cars.size(), newCar);
            }
        } else System.out.println("Car with such number is already exist!\t\tnumber:\t" + gosNum);
        return newCar;
    }

    public Car findCar(String gosNum) {
        for (Map.Entry<Integer, Car> pair : cars.entrySet()) {
            if (pair.getValue().number.equals(gosNum)) return pair.getValue();
        }
        return null;
    }

    /**
     * Renting a car.
     * <p>
     *     Firstly, there is a check if the entered choice is correct : it should be a number and the Map or rental applications
     *     should contain this number.
     *     If this check is successful, a {@link  BaseAdministration#rentCar(AuthorizedUser, int)} method  is called.
     *     This method takes a unique id of chosen available application (it doesn't equal it's key in the Map).
     * </p>
     * @param choice a number of a certain application in which renter is interested in
     * @param avails a Map of Rental Applications (objects which contain an information about the application for taking in rent)
     * @return true in the case of successful rent
     */
    protected boolean rentCar(String choice, Map<Integer, RentalApplication> avails) {
        if (!TechnicalInfo.isNumeric(choice)) {
            System.out.println("\nEnter number!\t\tyour choice:\t" + choice);
            return false;
        }
        int id = Integer.parseInt(choice);
        if (avails.containsKey(id)) {
            int availId = avails.get(id).availableId;
            int successRent = BaseAdministration.rentCar(this, availId);
            if (successRent != 0) {
                return refreshOrders();
            } else {
                System.out.println("\nsomething went wrong\n");
                return false;
            }
        } else {
            System.out.println("\nChoose variant from the list!\t\tyour choice:\t" + choice);
            return false;
        }
    }

    public Map<Integer, Available> seeOwnerAvails() {
        ResultSet rs = BaseAdministration.collectOwnerAvails(this);
        return Available.collectAvailable(rs, this);
    }

    /**
     * Giving car in rent (creating a new rental application).
     * <p>
     *     Firstly all params are checked for it's correctness.
     *     As the Map rentalAppl can contains a lot of applications for the same car, but user chooses a certain car
     *     for giving in rent, a LinkedList for all applications for the chosen car is created (rentalApplicationsForCar) and filled.
     *     Next, there is hard check for the correctness of time for time to start and finish giving this car in rent.
     *     In the RentalApplication object we have the time interval, when the car starts be unable for giving in rent and then when starts.
     *     1. So, firstly, this is a check : current time should be less than time for the beginning and finishing giving in rent.
     *     2. Secondly, time of the begging giving in rent should be before time of the finishing giving in rent.
     *     3. Then, if it is the only application for this car and it's time of beginning and finishing be unable for rent
     *     is null, any time is correct.
     *     4. Then, if time of the begging giving in rent is in the interval, when car starts be unable for giving in rent and when stops,
     *     this time for the begging cannnot be chosen.
     *     5. Then, if the chosen time for the beginning rent is after the time, when car starts be able for giving in rent, but
     *     the time of finishing rent is after time when car starts be unable for rent in the next application, it is incorrect
     *     time for finishing giving car in rent.
     *     6. Finally, if time, when car starts be unable for giving in rent is after time, when user wants to start giving this car
     *     in rent, but chosen time is before time, when car starts be able for giving in rent, it is incorrect time for
     *     finishing rent.
     *
     *     !!!IMPORTANT POINT: the car starts be unable for rent if there exists a rental application, where car can be taken in rent in this time;
     *     tha car starts be able for giving in rent, when the previous application is finished, or at the time for finishing rent in this
     *     application. Car is unable for giving in rent while at least one order on this car is unfinished!!!
     *
     * </p>
     * @param rentalAppl a Map of all variants which can be given in rent
     * @param carsForRent a Map of all cars, which can be given in rent
     * @param carChoice a user's choice of car he wants to give in rent
     */
    public boolean giveCarInRent(Map<Integer, RentalApplication> rentalAppl, Map<Integer, Car> carsForRent, String carChoice, String yearToStartRent, String monthToStartRent, String dayToStartRent,
                                 String hourToStartRent, String minuteToStartRent, String yearToStopRent, String monthToStopRent, String dayToStopRent,
                                 String hourToStopRent, String minuteToStopRent, String areaToTake, String areaToReturn, String pricePerMinute) {

        if (carsForRent == null) return false;
        if (!TechnicalInfo.isRentDataCorrect(carsForRent, carChoice, yearToStartRent, monthToStartRent, dayToStartRent,
                hourToStartRent, minuteToStartRent,
                yearToStopRent, monthToStopRent, dayToStopRent, hourToStopRent, minuteToStopRent, areaToTake, areaToReturn, pricePerMinute)) {
            return false;
        }

        Area areaTake = TechnicalInfo.getArea(Integer.parseInt(areaToTake));
        Area areaReturn = TechnicalInfo.getArea(Integer.parseInt(areaToReturn));
        Car carForRent = carsForRent.get(Integer.parseInt(carChoice));

        LocalDateTime timeToStartRent = LocalDateTime.of(
                Integer.parseInt(yearToStartRent), Integer.parseInt(monthToStartRent),
                Integer.parseInt(dayToStartRent), Integer.parseInt(hourToStartRent),
                Integer.parseInt(minuteToStartRent));

        LocalDateTime timeToStopRent = LocalDateTime.of(
                Integer.parseInt(yearToStopRent), Integer.parseInt(monthToStopRent),
                Integer.parseInt(dayToStopRent), Integer.parseInt(hourToStopRent),
                Integer.parseInt(minuteToStopRent));
        double pricePerMin = Double.parseDouble(pricePerMinute);

        LinkedList<RentalApplication> rentalApplicationsForCar = new LinkedList<>();

        for (Map.Entry<Integer, RentalApplication> pair : rentalAppl.entrySet()) {
            if (pair.getValue().carNumber.equals(carForRent.number)) {
                rentalApplicationsForCar.add(pair.getValue());
            }
        }

        int i = 0;
        for (RentalApplication rental : rentalApplicationsForCar) {
            if (timeToStartRent.isBefore(LocalDateTime.now()) || timeToStopRent.isBefore(LocalDateTime.now())) {
                System.out.println("You can't choose the time interval before current time");
                return false;
            } else if (timeToStartRent.isAfter(timeToStopRent)) {
                System.out.println("time of finishing from car can't be before time of starting rent");
                return false;
            }
            else if (rentalApplicationsForCar.size() == 1 && rental.timeWhenCarStartsBeBusy == null && rental.timeWhenCarStopsBeBusy == null) {
                System.out.println("Correct date");
            } else if ((rental.timeWhenCarStartsBeBusy == null || rental.timeWhenCarStartsBeBusy.isBefore(timeToStartRent))
                    && rental.timeWhenCarStopsBeBusy.isAfter(timeToStartRent)) {
                System.out.println("You can't propose car for rent before it become free");
                return false;
            } else if (rentalApplicationsForCar.size() > (i + 1) &&
                    rental.timeWhenCarStopsBeBusy.isBefore(timeToStartRent) &&
                    rentalApplicationsForCar.get(i + 1).timeWhenCarStartsBeBusy.isBefore(timeToStopRent)) {
                System.out.println("Incorrect time for finishing rent");
                return false;
            } else if (
                  rental.timeWhenCarStartsBeBusy.isAfter(timeToStartRent) &&
                          rental.timeWhenCarStopsBeBusy.isBefore(timeToStopRent)) {
                System.out.println("Incorrect time for finishing rent");
                return false;
            } else {
                break;
            }
            i++;
        }
        return BaseAdministration.giveCarInRent(carForRent, areaTake, areaReturn, pricePerMin, timeToStartRent, timeToStopRent);
    }

    public boolean finishOrder(String choice, String areaChoice) throws IOException {
        if (!TechnicalInfo.isNumeric(choice)) {
            System.out.println("\nEnter a number!\t\tyour value\t" + choice);
            return false;
        }
        int ch = Integer.parseInt(choice);
        if (!myOrders.containsKey(ch) || !myOrders.get(ch).status.equals("not_finished")) {
            System.out.println("\nChoose order from the proposed list!\t\tyour choice\t" + ch);
            return false;
        }
        if (!TechnicalInfo.isNumeric(areaChoice)) {
            System.out.println("\nEnter a number for zone choice!\t\tyour value\t" + areaChoice);
            return false;
        }
        int areaId = Integer.parseInt(areaChoice);
        Area areaToReturn = TechnicalInfo.getArea(areaId);
        if (areaToReturn == null) {
            System.out.println("\nChoose zone from the proposed list!\t\tyour choice\t" + areaId);
            return false;
        }
        Order choosedOrder = myOrders.get(ch);
        int fine = 0;
        if (!areaToReturn.equals(choosedOrder.avail.areaBring)) {
            System.out.println("\nChosen zone for finishing rent os different from the zone in rent application \nfine 1000.0 rubles\n");
            String continueChoice = TechnicalInfo.inputStr("Continue?\nYES\nNO\n");
            if (!continueChoice.equals("YES")) {
                return false;
            }
        }
        fine = 1000;
        refreshOrders();
        if (LocalDateTime.now().isAfter(choosedOrder.avail.timeToReturn)) {
            System.out.println("\nYou are finishing an order after the proposed time. FINE 5.000 \n");
            fine = fine + 5000;
        }
        return choosedOrder.finishRent(choosedOrder.id, areaId, fine);
    }


}
