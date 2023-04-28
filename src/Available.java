import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;


public class Available implements Comparable<Available> {
    int id;
    Car car;
    Area areaTake;
    Area areaBring;
    double pricePerMinute;
    LocalDateTime timeToTake;
    LocalDateTime timeToReturn;

    public Available(int id, Car car, Area areaTake, Area areaBring, double price, LocalDateTime timeToTake, LocalDateTime timeToReturn) {
        this.car = car;
        this.areaTake = areaTake;
        this.areaBring = areaBring;
        this.pricePerMinute = price;
        this.timeToTake = timeToTake;
        this.timeToReturn = timeToReturn;
        this.id = id;
    }

    @Override
    public int compareTo(Available avail) {
        return Double.compare(this.pricePerMinute, avail.pricePerMinute);
    }

    @Override
    public String toString() {
        return (car.toString() + "Zone for renting a car:\t\t" + areaTake.toString() + "\nZone for finishing rent:\t\t"
                + areaBring.toString() + "\nPrice per minute(RUB):\t\t" + pricePerMinute +
                "\nTime of starting a rent:\t\t" +
                TechnicalInfo.dateFormat(timeToTake) +
                "\nTime of finishing rent:\t\t" + TechnicalInfo.dateFormat(timeToReturn) + "\n\n");
    }


    public static Map<Integer, RentalApplication> listToMap(List<RentalApplication> list) {
        int count = 0;
        Map<Integer, RentalApplication> convertedMapa = new HashMap<>();
        for (RentalApplication appl : list) {
            convertedMapa.put(count++, appl);
        }
        return convertedMapa;
    }

    /**
     * Collecting available applications data
     * <p>
     *     If the ResultSet, returned from the DataBase is null - there is no available cars now.
     *     Otherwise, for each string in the ResultSet a {@link RentalApplication#collectAvailableDate(ResultSet)} method
     *     is called and as a result we have a Rental Application object with the fields we're interested in.
     *     Then every of this objects is put to the applications List.
     *     Then this sorted by price per hour list is converting into the Map.
     * </p>
     * @param rs the ResultSet returned from the DataBase (it can be a ResultSet of all applications, or applications
     *           which were chosen by some parameter.
     * @return a Map of the collected applications
     */
    protected static Map<Integer, RentalApplication> collectAllAvailables(ResultSet rs) {
        Map<Integer, RentalApplication> avails;
        try {
            if (rs != null && rs.next()) {
                System.out.println("\n\tCARS AVAILABLE FOR RENT\t\n");
                avails = new HashMap<>();
                int count = 0;
                do {
                    RentalApplication availDate = RentalApplication.collectAvailableDate(rs);
                    if (availDate != null) {
                        avails.put(count, availDate);
                        count++;
                    }
                } while (rs.next());
                List<RentalApplication> applications = new ArrayList<>(avails.values());
                Collections.sort(applications);
                return listToMap(applications);
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Collecting applications of this user (for car owners).
     * <p>
     *     Firstly a {@link RentalApplication#collectAvailableDate(ResultSet)} method is called.
     *     Then for each Rental Application object an Available object creates and puts into the list of collected
     *     objects. Then the sorted list converts to the Map.
     * </p>
     * @param rs the ResultSet of applications' data returned from the DataBase
     * @param owner a current user, who wants to see his available applications
     * @return a Map of the Available objects
     */
    protected static Map<Integer, Available> collectAvailable(ResultSet rs, AuthorizedUser owner) {
        Map<Integer, Available> myAvailables = null;
        List<Available> avails;
        try {
            if (rs != null && rs.next()) {
                myAvailables = new HashMap<>();
                avails = new ArrayList<>();
                do {
                    RentalApplication availDate = RentalApplication.collectAvailableDate(rs);
                    if (availDate != null) {
                        avails.add(new Available(availDate.availableId,
                                owner.findCar(availDate.carNumber),
                                TechnicalInfo.getArea(availDate.areaToTakeId),
                                TechnicalInfo.getArea(availDate.areaToReturnId),
                                availDate.pricePerMinute,
                                availDate.timeTakeToRent.toLocalDateTime(),
                                availDate.timeReturnFromRent.toLocalDateTime()
                        ));
                    } else return null;
                } while (rs.next());
                Collections.sort(avails);
                int count = 0;
                for (Available av : avails) {
                    myAvailables.put(count, av);
                    count++;
                }
            }
            return myAvailables;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Collecting cars which can be given in rent.
     * <p>
     *     In this method not only the cars' info is collecting,
     *     but also the time, when this or another car cannot be given in rent
     * </p>
     * @see RentalApplication#collectCanBeGivenInRent(ResultSet) 
     * @param rs the ResultSet returned from the DataBase and contains all the info
     * @return a Map of the Rental Application objects (each object responds one (if it's possible)
     * available application for each time interval, when the car cannot be given in rent)
     */
    protected static Map<Integer, RentalApplication> seeCarsToGiveInRent(ResultSet rs) {
        Map<Integer, RentalApplication> rents;
        try {
            if (rs != null && rs.next()) {
                rents = new HashMap<>();
                int count = 0;
                do {
                    RentalApplication rentDate = RentalApplication.collectCanBeGivenInRent(rs);
                    if (rentDate != null) {
                        rents.put(count, rentDate);
                        count++;
                    }
                } while (rs.next());
                return rents;
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
