import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * This class is used mostly for getting data from the DataBase's ResultSet and transforming it
 * to the this' class object fields.
 * The idea was to make it easier operating with this data later, cause it's more common to work with object,
 * but not with the ResultSet's items.
 * Also sometimes there is no need to have an object of Available or Orders classes, like when we want only to get the info
 * about all the available, we have such an ability by creating an object with such fields in this class,
 * but without creating Car and Area objects.
 */
public class RentalApplication implements Comparable<RentalApplication>{
    int availableId;
    int orderId;
    String orderStatus;
    double pricePerMinute;
    int carSeats;
    String carNumber;
    String carModel;
    int areaToTakeId;
    int areaToReturnId;
    int areaWasReturnedId;
    String renterLogin;
    String ownerLogin;
    double totalPrice;
    Timestamp timeTakeToRent;
    Timestamp timeReturnFromRent;
    Timestamp timeWasTakenInRent;
    Timestamp timeWasReturnedFromRent;
    LocalDateTime timeWhenCarStartsBeBusy;
    LocalDateTime timeWhenCarStopsBeBusy;

    @Override
    public int compareTo(RentalApplication application) {
        return this.timeTakeToRent.compareTo(application.timeTakeToRent);
    }

    private RentalApplication(String carNumber, String model, int seats, LocalDateTime timeStart, LocalDateTime timeStop) {
        this.carNumber = carNumber;
        this.carModel = model;
        this.carSeats = seats;
        this.timeWhenCarStartsBeBusy = timeStart;
        this.timeWhenCarStopsBeBusy = timeStop;
    }

    private RentalApplication(int availId, String carNumber, String model, int seats, int areaToTakeId, int areaToReturnId,
                              double pricePerMinute, Timestamp timeTake, Timestamp timeReturn) {
        this.availableId = availId;
        this.carNumber = carNumber;
        this.carModel = model;
        this.carSeats = seats;
        this.areaToTakeId = areaToTakeId;
        this.areaToReturnId = areaToReturnId;
        this.pricePerMinute = pricePerMinute;
        this.timeTakeToRent = timeTake;
        this.timeReturnFromRent = timeReturn;
    }


    @Override
    public String toString() {
        Area areaTake = TechnicalInfo.getArea(areaToTakeId);
        Area areaReturn = TechnicalInfo.getArea(areaToReturnId);
        String str;
        try {
            if (areaTake != null && areaReturn != null && timeTakeToRent!=null) {
                str = "Model:\t" + carModel + "\nNumber of seats:\t" + carSeats + "\nCar number:\t" + carNumber +
                        "\nZone for starting a rent:\t\t" + TechnicalInfo.getArea(areaToTakeId).toString() + "\nZone fro finishing a rent:\t\t"
                        + TechnicalInfo.getArea(areaToReturnId).toString() + "\nЦена за минуту (Rub):\t\t" + pricePerMinute +
                        "\nTime of starting a rent:\t\t" +
                        timeTakeToRent.toString() +
                        "\nTime of finishing a rent:\t\t" + timeReturnFromRent.toString() + "\n\n";
                return str;
            }
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RentalApplication collectAvailableDate(ResultSet rs) {
        try {
            int availId = rs.getInt("available_id");
            String carNumber = rs.getString("car_gos_number");
            String model = rs.getString("model");
            int seats = rs.getInt("seats");
            int areaToTakeId = rs.getInt("area_to_take_id");
            int areaToReturnId = rs.getInt("area_to_return_id");
            double pricePerMinute = rs.getDouble("price_per_minute");
            Timestamp timeTake = rs.getTimestamp("time_to_take");
            Timestamp timeReturn = rs.getTimestamp("time_to_return");
            return new RentalApplication(availId, carNumber, model, seats, areaToTakeId, areaToReturnId, pricePerMinute, timeTake, timeReturn);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RentalApplication collectCanBeGivenInRent(ResultSet rs) {
        try {
            String carNumber = rs.getString("gos_number");
            String carModel = rs.getString("car_model");
            int seats = rs.getInt("seats");
            Timestamp timeUnable = rs.getTimestamp("time_car_is_unable_to_give_in_rent");
            Timestamp timeAble = rs.getTimestamp("time_to_start_giving_in_rent");
            LocalDateTime timeUnableJava = null;
            LocalDateTime timeAbleJava = null;
            if (timeUnable != null) {
                timeUnableJava = timeUnable.toLocalDateTime();
            }
            if (timeAble != null) {
                timeAbleJava = timeAble.toLocalDateTime();
            }
            return new RentalApplication(carNumber, carModel, seats, timeUnableJava, timeAbleJava);


        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RentalApplication collectOrderDate(ResultSet rs) {
        try {
            RentalApplication collectedAvailDate = collectAvailableDate(rs);
            if (collectedAvailDate == null) return null;
            collectedAvailDate.orderId = rs.getInt("order_id");
            collectedAvailDate.renterLogin = rs.getString("renter_login");
            collectedAvailDate.ownerLogin = rs.getString("owner_login");
            Object areaWasReturned = rs.getObject("area_was_returned_id");
            if (areaWasReturned != null) {
                collectedAvailDate.areaWasReturnedId = rs.getInt("area_was_returned_id");
            } else {
                collectedAvailDate.areaWasReturnedId = -1;
            }
            collectedAvailDate.timeWasTakenInRent = rs.getTimestamp("time_was_taken");
            Object timeWasReturned = rs.getObject("time_was_returned");
            if (timeWasReturned != null) {
                collectedAvailDate.timeWasReturnedFromRent = rs.getTimestamp("time_was_returned");
            } else collectedAvailDate.timeWasReturnedFromRent = null;
            Object totPrice = rs.getObject("total_price");
            if (totPrice != null) {
                collectedAvailDate.totalPrice = rs.getDouble("total_price");
            } else collectedAvailDate.totalPrice = -1;
            collectedAvailDate.orderStatus = rs.getString("finished_status");
            return collectedAvailDate;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toStringRent() {
        String str;
        if (timeWhenCarStartsBeBusy != null) {
            if (timeWhenCarStopsBeBusy != null) {
                str = "Model:\t" + carModel + "\nNumber of seats:\t" + carSeats + "\nCar number:\t" + carNumber +
                        "\nTime when car beginning to be unavailable for rent:\t\t" +
                        TechnicalInfo.dateFormat(timeWhenCarStartsBeBusy) +
                        "\nTime when car beginning to be available for rent:\t\t" + TechnicalInfo.dateFormat(timeWhenCarStopsBeBusy) + "\n";
            } else {
                str = "Model:\t" + carModel + "\nNumber of seats:\t" + carSeats + "\nCar number:\t" + carNumber +
                        "\nTime when car beginning to be unavailable for rent:\t\t" +
                        TechnicalInfo.dateFormat(timeWhenCarStartsBeBusy) +
                        "\nTime when car beginning to be available for rent:\t\t" + "any\n";
            }
        } else if (timeWhenCarStopsBeBusy != null) {
            str = "Model:\t" + carModel + "\nNumber of seats:\t" + carSeats + "\nCar number:\t" + carNumber +
                    "\nTime when car beginning to be unavailable for rent:\t\t" +
                    "any" +
                    "\nTime when car beginning to be available for rent:\t\t" + TechnicalInfo.dateFormat(timeWhenCarStopsBeBusy) + "\n";
        } else {
            str = "Model:\t" + carModel + "\nNumber of seats:\t" + carSeats + "\nCar number:\t" + carNumber +
                    "\nTime when car beginning to be unavailable for rent:\t\t" +
                    "any" +
                    "\nTime when car beginning to be available for rent:\t\t" + "any\n";
        }
        return str;
    }
}
