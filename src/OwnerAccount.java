import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OwnerAccount extends Account {

    /**
     * Checking the correctness of choice in ownerMenu
     * <p>
     * if choice is numeric and ownerMenu contains this key, returns true
     * <p>
     *
     * @param choice a String variable of user's input
     * @see TechnicalInfo#ownerMenu
     */
    @Override
    public boolean isCorrectChoice(String choice) {
        if (isNumeric(choice)) {
            int ch = Integer.parseInt(choice);
            if (ownerMenu.containsKey(ch)) {
                return true;
            }
        }
        System.out.println("\nIncorrect choice!\t\tYour choice: " + choice);
        return false;
    }

    @Override
    public void choose(int ch, AuthorizedUser owner) throws IOException {
        switch (ch) {
            case 1 -> printAuthorizedInfo(owner);
            case 2 -> addCar(owner);
            case 3 -> seeAllAvailables();
            case 4 -> seeAvailablesByPrice();
            case 5 -> seeAvailablesByAreaToTake();
            case 6 -> takeCarInRent(owner);
            case 7 -> printAllOrders(owner);
            case 8 -> finishRent(owner);
            case 9 -> owner.printAllCars();
            case 10 -> seeOwnerAvailables(owner);
            case 11 -> giveInRent(owner);
        }
    }

    private void seeOwnerAvailables(AuthorizedUser owner) {
        Map<Integer, Available> foundAvails = owner.seeOwnerAvails();
        if (foundAvails != null) {
            for (Map.Entry<Integer, Available> avail : foundAvails.entrySet()) {
                System.out.println("APPLICATION NUMBER: " + avail.getKey() + ".\n" + avail.getValue().toString());
            }
        } else System.out.println("\n\t\tYOU DON'T HAVE CARS PROPOSED FOR RENT\n");
    }


    public Map<Integer, Car> seeCanBeGivenInRent(AuthorizedUser owner, Map<Integer, RentalApplication> foundCars) {
        try {
            if (foundCars != null) {
                Map<Integer, Car> cars = new HashMap<>();
                int count = 0;
                for (Map.Entry<Integer, RentalApplication> rentCars : foundCars.entrySet()) {
                    Car carToPut = owner.findCar(rentCars.getValue().carNumber);
                    if (cars.size() == 0 || !cars.containsValue(carToPut)) {
                        cars.put(count, carToPut);
                        count++;
                    }
                }
                cars.forEach((a, b) -> System.out.println("\n\t\t\tCAR VARIANT: " + a + ".\n " + b.toString()));
                return cars;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void giveInRent(AuthorizedUser owner) throws IOException {
        Map<Integer, RentalApplication> foundCars = Available.seeCarsToGiveInRent(BaseAdministration.collectCarsCouldBeGivenInRent(owner));
        if (foundCars != null) {
            foundCars.forEach((a, b) -> System.out.println("\n\t\t\tVARIANT: " + a + ".\n " + b.toStringRent()));
            Map<Integer, Car> cars = seeCanBeGivenInRent(owner, foundCars);
            if (cars != null) {
                String carChoice = inputStr("Choose car you would like to propose for rent");
                String yearToStartRent = inputStr("Enter year for starting rent");
                String monthToStartRent = inputStr("Enter month for starting rent");
                String dayToStartRent = inputStr("Enter day for starting rent");
                String hourToStartRent = inputStr("Enter hour for starting rent");
                String minuteToStartRent = inputStr("Enter minute for starting rent");


                String yearToStopRent = inputStr("Enter year for finishing rent");
                String monthToStopRent = inputStr("Enter month for finishing rent");
                String dayToStopRent = inputStr("Enter day for finishing rent");
                String hourToStopRent = inputStr("Enter hour for finishing rent");
                String minuteToStopRent = inputStr("Enter minute for finishing rent");

                areasInfo();
                String areaToTake = inputStr("Choose zone to take car in rent");
                String areaToReturn = inputStr("Choose zone to return car from rent");
                String pricePerMinute = inputStr("Enter price per minute for rent");

                boolean successApple = owner.giveCarInRent(foundCars, cars, carChoice, yearToStartRent,
                        monthToStartRent, dayToStartRent,
                        hourToStartRent, minuteToStartRent,
                        yearToStopRent, monthToStopRent, dayToStopRent,
                        hourToStopRent, minuteToStopRent, areaToTake,
                        areaToReturn, pricePerMinute);


                if (successApple) System.out.println("\n\nCAR IS SUCCESSFULLY GIVEN IN RENT!\n");
                else System.out.println("\nCAR RENT IS FAULT\n");
            } else System.out.println("There are no available cars");
        } else System.out.println("There are no available cars");
    }

}
