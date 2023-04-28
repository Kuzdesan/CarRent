import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;


public class Order implements Comparable<Order> {
    int id;
    Available avail;
    AuthorizedUser renter;
    double totalPrice;
    Area areaWasReturn;
    LocalDateTime timeWasTakenInRent;
    LocalDateTime timeWasReturnedFromRent;
    String status;

    @Override
    public int compareTo(Order ord) {
        if (this.timeWasReturnedFromRent == null)
            if (ord.timeWasReturnedFromRent == null)
                return 0;
            else
                return -1; // null is before other strings
        else
            if (ord.timeWasReturnedFromRent == null)
                return 1;  // all other strings are after null
            else
                return ord.timeWasReturnedFromRent.compareTo(this.timeWasReturnedFromRent);
    }

    public Order(int id, Available avail, AuthorizedUser renter, LocalDateTime timeWasTakenInRent, LocalDateTime timeWasReturnedFromRent,
                 double totalPrice, Area areaWasReturn, String status) {
        this.id = id;
        this.avail = avail;
        this.renter = renter;
        this.timeWasTakenInRent = timeWasTakenInRent;
        this.timeWasReturnedFromRent = timeWasReturnedFromRent;
        this.totalPrice = totalPrice;
        this.areaWasReturn = areaWasReturn;
        this.status = status;
    }

    /**
     * Collecting a history of orders for this user.
     * <p>
     *     The work begins from the collecting order data from the Rental Application object.
     *     For this we are calling a {@link RentalApplication#collectOrderDate(ResultSet)} method, which gets a current string
     *     of the ResultSet and returns an object, made from this string's fields.
     *     Then we are checking, if the renter's login (a person, who asks for this orders' history) equals
     *     the owner's login (we get it from the Rental application object) we can make a conclusion, that the person,
     *     who collects the statistics is the same person, who created that order, which is checked by us now.
     *     In this case we have no need to create a new Car object as it is already exists in the user's map of cars in own.
     *     Otherwise we should create a new Car object with the fields collected from the Rental Application object.
     *     Then we are checking, if the created in the begging of the method map called orderAvails contains
     *     current Available id, we shouldn't put it there again (we can have a variety of orders which were taken
     *     on the current available application). Otherwise we're creating an Available object.
     *     Then we're creating an Area's object's from this' id and transforming data format.
     *     And finally we're creating a new Order object and add this into the orders' list.
     *     As the cycle is finished we sort our orders' list from the last data of finishing to the first
     *     (in this case user will firstly see his unfinished orders and his last orders).
     *     Then we're transforming this sorted list to the Map, where the key is only the
     *     sequence number for the convenient output later.
     * </p>
     * @param rs the ResultSet of all orders (both finished and finished) returned from the DataBase
     * @param renter a user who asks for his orders' history
     * @return a Map of the collected Order objects
     */
    public static Map<Integer, Order> collectOrders(ResultSet rs, AuthorizedUser renter) {
        Map<Integer, Order> myOrders = null;
        Map<Integer, Available> orderAvails = null;
        List<Order> ordersList = null;
        try {
            if (rs != null && rs.next()) {
                myOrders = new HashMap<>();
                ordersList = new ArrayList<>();
                orderAvails = new HashMap<>();
                do {
                    RentalApplication orderData = RentalApplication.collectOrderDate(rs);
                    if (orderData != null) {
                        Car foundCar;
                        if (renter.login.equals(orderData.ownerLogin)) {
                            foundCar = renter.findCar(orderData.carNumber);
                        } else {
                            foundCar = new Car(orderData.carNumber, orderData.carModel, orderData.carSeats, orderData.ownerLogin);
                        }
                        if (!orderAvails.containsKey(orderData.availableId)) {
                            orderAvails.put(orderData.availableId, new Available(orderData.availableId, foundCar,
                                    TechnicalInfo.getArea(orderData.areaToTakeId),
                                    TechnicalInfo.getArea(orderData.areaToReturnId),
                                    orderData.pricePerMinute,
                                    orderData.timeTakeToRent.toLocalDateTime(),
                                    orderData.timeReturnFromRent.toLocalDateTime()));
                        }

                        Timestamp timeWasReturnedSQL = orderData.timeWasReturnedFromRent;
                        LocalDateTime timeWasReturned = null;
                        if (timeWasReturnedSQL != null) {
                            timeWasReturned = timeWasReturnedSQL.toLocalDateTime();
                        }

                        Area areaWasReturned = null;
                        int areaReturnedId = orderData.areaWasReturnedId;
                        if (areaReturnedId != -1) {
                            areaWasReturned = TechnicalInfo.getArea(areaReturnedId);
                        }

                        Order order = new Order(orderData.orderId,
                                orderAvails.get(orderData.availableId),
                                renter, orderData.timeWasTakenInRent.toLocalDateTime(), timeWasReturned,
                                orderData.totalPrice, areaWasReturned, orderData.orderStatus
                        );
                        ordersList.add(order);
                    }

                } while (rs.next());
                Collections.sort(ordersList);
                int count = 0;
                for (Order order : ordersList) {
                    myOrders.put(count, order);
                    count++;
                }
            }
            return myOrders;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Finishing the chosen order
     * <p>
     *     Firstly we're checking if the order's time is run (in the available application for this order
     *     there was a time, when the rent should be finished, so, if the current order completion time is after that time time,
     *     a renter has some problems). With this information we should change the car's and available application's statuses.
     *     Then there is a calling of a {@link BaseAdministration#finishOrder(int, int, int, String, String)} method.
     *     Then, if the DataBase's updating info was successful, we are calling a {@link AuthorizedUser#refreshOrders()} method
     *     to update user's orders history.
     * </p>
     * @param orderId the id of the order to finish
     * @param areaToReturnId the place where the car is returned
     * @param fine can be 0, if the time of finishing rent in available application is after the current time and the area of returning
     *             equals the area where car should be returned in the application
     * @return the success of the order finishing
     */
    public boolean finishRent(int orderId, int areaToReturnId, int fine) {
        boolean isAvailFinished;
        String carStatus;
        String availStatus;
        isAvailFinished = avail.timeToReturn.isBefore(LocalDateTime.now());
        if (isAvailFinished) {
            carStatus = "free";
            availStatus = "time-run";
        } else {
            carStatus = "available_for_rent";
            availStatus = "available";
        }
        boolean successFinish = BaseAdministration.finishOrder(orderId, areaToReturnId, fine, carStatus, availStatus);
        if (successFinish) {
            renter.refreshOrders();
            return true;
        }
        return false;
    }
    public String toString() {
        boolean areaWasReturned = areaWasReturn != null;
        boolean timeWasReturned = timeWasReturnedFromRent != null;
        if (areaWasReturned && timeWasReturned) {
            return avail.toString() + "\nrenter's login:\t\t" + renter.login + "\norder start time:\t\t" + TechnicalInfo.dateFormat(timeWasTakenInRent) +
                    "\nprice per order:\t\t" + totalPrice + "\nplace of return of the car:\t\t" + areaWasReturn.toString() +
                    "\norder end time:\t\t" + TechnicalInfo.dateFormat(timeWasReturnedFromRent) + "\norder status:\t\t" + status;
        }
        if (!areaWasReturned && timeWasReturned) {
            return avail.toString() + "\nrenter's login:\t\t" + renter.login + "\norder start time:\t\t" + TechnicalInfo.dateFormat(timeWasTakenInRent) +
                    "\nprice per order:\t\t" + totalPrice + "\nplace of return of the car:\t\t" + "------" +
                    "\norder end time:\t\t" + TechnicalInfo.dateFormat(timeWasReturnedFromRent) + "\norder status:\t\t" + status;
        }
        if (areaWasReturned && !timeWasReturned) {
            return avail.toString() + "\nrenter's login:\t\t" + renter.login + "\norder start time:\t\t" + TechnicalInfo.dateFormat(timeWasTakenInRent) +
                    "\nprice per order:\t\t" + totalPrice + "\nplace of return of the car:\t\t" + areaWasReturn.toString() +
                    "\norder end time:\t\t" + "------" + "\norder status:\t\t" + status;
        } else {
            return avail.toString() + "\nrenter's login:\t\t" + renter.login + "\norder start time:\t\t" + TechnicalInfo.dateFormat(timeWasTakenInRent) +
                    "\nprice per order:\t\t" + totalPrice + "\nplace of return of the car:\t\t" + "------" +
                    "\norder end time:\t\t" + "------" + "\norder status0:\t\t" + status;
        }
    }

}
