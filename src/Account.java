import java.io.IOException;
import java.sql.ResultSet;
import java.util.Map;

/**
 * Class of AuthorizedUser dialog
 * <p>
 *     This class provides such user's actions like:
 *      1. Adding a new Car
 *      2. See all available rental applications
 *      3. Renting a car
 *      4. Finishing rent
 *      5. See orders' history
 * </p>
 */
public class Account extends TechnicalInfo implements DialogType<AuthorizedUser> {

    /**
     * Checking the correctness of choice in authorizedMenu.
     * <p>
     *     if choice is numeric and authorizedMenu contains such key, method returns true
     * </p>
     * @param choice a String variable of user's input
     * @see TechnicalInfo#authorizedMenu
     */
    @Override
    public boolean isCorrectChoice(String choice) {
        if (isNumeric(choice)) {
            int ch = Integer.parseInt(choice);
            if (authorizedMenu.containsKey(ch)) {
                return true;
            }
        }
        System.out.println("\nYour choice is incorrect!\t\tYour choice: " + choice);
        return false;
    }

    /**
     * Opening an OwnerAccount dialog
     * @param owner an object of AuthorizedUser class, but with cars in own
     * @param menu a type of "dialog" menu, which would be printed and used to choose the action
     */
    @Override
    protected void openNewWindow(AuthorizedUser owner, Map<Integer, String> menu) throws IOException {
        int ptr = 1;
        OwnerAccount ownAcc = new OwnerAccount();
        do {
            out(menu);
            String choice = inputStr("");
            if (ownAcc.isCorrectChoice(choice)) {
                ptr = Integer.parseInt(choice);
                ownAcc.choose(ptr, owner);
            }
        } while (ptr != 0);
    }

    @Override
    public void choose(int ch, AuthorizedUser renter) throws IOException {
        switch (ch) {
            case 1 -> printAuthorizedInfo(renter);
            case 2 -> addCar(renter);
            case 3 -> seeAllAvailables();
            case 4 -> seeAvailablesByPrice();
            case 5 -> seeAvailablesByAreaToTake();
            case 6 -> takeCarInRent(renter);
            case 7 -> printAllOrders(renter);
            case 8 -> finishRent(renter);
        }
    }

    /**
     * Print info about all available rental applications.
     * <p>
     *     First method {@link #collectAllAvailables()} which returns a Map of all rental applications is called.
     *     Then this Map is printing if it is not null.
     *     This methods returns a Map as this method is used also for renting a car and it is more common to
     *     print all applications like a numbered list and then ask a user which one number he prefers.
     *     So it gives an opportunity to get the needed application just by user's input of identifier.
     *     This method is just a dialog method and can be deleted if needed.
     * </p>
     * @return a Map of all found rental applications
     */
    protected Map<Integer, RentalApplication> seeAllAvailables() {
        Map<Integer, RentalApplication> avails = collectAllAvailables();
        if (avails != null) {
            avails.forEach((a, b) -> System.out.println("\n\t\t\tVARIANT: " + a + ".\n " + b.toString()));
            return avails;
        } else System.out.println("\nTHERE ARE NO CARS FOR RENT AT THE MOMENT\n");
        return null;
    }

    /**
     * Collecting all available rental applications.
     * <p>
     *     This method calls a {@link Available#collectAllAvailables(ResultSet)} method, where the argument is a result of the
     *     select query to DataBase for collecting all current available {@link BaseAdministration#collectAvailables()}
     * </p>
     */
    protected Map<Integer, RentalApplication> collectAllAvailables() throws NumberFormatException {
        return Available.collectAllAvailables(BaseAdministration.collectAvailables());
    }

    /**
     * Print info about available rental applications by price interval.
     * @see #seeAllAvailables()
     */
    protected Map<Integer, RentalApplication> seeAvailablesByPrice() throws IOException {
        String strMinPrice = inputStr("\nEnter minimum price");
        String strMaxPrice = inputStr("\nEnter maximum price");
        Map<Integer, RentalApplication> avails = collectAllAvailables(strMinPrice, strMaxPrice);
        if (avails != null) {
            avails.forEach((a, b) -> System.out.println("\n\t\t\tVARIANT: " + a + ".\n " + b.toString()));
            return avails;
        } else System.out.println("\n\tTHERE ARE NO CARS FOR RENT AT THE MOMENT\t\n");
        return null;

    }

    /**
     * Collecting available rental application by price interval.
     * <p>
     *     Instead of {@link BaseAdministration#collectAvailables()} in {@link #collectAllAvailables()}
     *     here it is {@link BaseAdministration#collectAvailables(double, double)}.
     *     Also there is a check whether the inputted price values are correct
     * </p>
     * @param strMinPrice minimum price per minute of rent
     * @param strMaxPrice maximum price per minute of rent
     */
    protected Map<Integer, RentalApplication> collectAllAvailables(String strMinPrice, String strMaxPrice) throws NumberFormatException {
        double minPrice;
        double maxPrice;
        if (isCorrectDouble(strMinPrice)) {
            minPrice = Double.parseDouble(strMinPrice);
            if (isCorrectDouble(strMaxPrice)) {
                maxPrice = Double.parseDouble(strMaxPrice);
            } else {
                System.out.println("\nIncorrect maximum price!\t\tmaximum price:\t" + strMaxPrice);
                return null;
            }
        } else {
            System.out.println("\nIncorrect minimum price!\t\tminimum price:\t" + strMinPrice);
            return null;
        }
        return Available.collectAllAvailables(BaseAdministration.collectAvailables(minPrice, maxPrice));
    }

    /**
     * Print info about available rental application by a zone, where car can be rented
     */
    protected Map<Integer, RentalApplication> seeAvailablesByAreaToTake() throws IOException {
        areasInfo();
        String areaChoice = inputStr("\nEnter zone");
        Map<Integer, RentalApplication> avails= collectAllAvailables(areaChoice);
        if (avails != null) {
            avails.forEach((a, b) -> System.out.println("\n\t\t\tVARIANT: " + a + ".\n " + b.toString()));
            return avails;
        } else System.out.println("\nTHERE ARE NO CARS FOR RENT AT THE MOMENT\n");
        return null;
    }

    /**
     * Collecting available rental application by zone, where car can be rented.
     * <p>
     *     Instead of {@link BaseAdministration#collectAvailables()} in {@link #collectAllAvailables()}
     *     here it is {@link BaseAdministration#collectAvailables(int)}.
     *     Also here is a check whether the inputted value of area id is correct
     * </p>
     * @param areaToTakeId identifier of chosen area to rent a car
     */
    protected Map<Integer, RentalApplication> collectAllAvailables(String areaToTakeId) throws NumberFormatException {
        if (!isCorrectAreaId(areaToTakeId)) {
            return null;
        }
        Area areaToTake = getArea(Integer.parseInt(areaToTakeId));
        if (areaToTake != null) {
            return Available.collectAllAvailables(BaseAdministration.collectAvailables(areaToTake.id));
        } else System.out.println("\nEnter correct zone!");
        return null;
    }

    protected void printAuthorizedInfo(AuthorizedUser user) {
        System.out.println("Your personal data:\n" + user.toString());
    }

    /**
     * Adding car in own.
     * <p>
     *     First, the user's input of car data happens.
     *     Then the method {@link AuthorizedUser#addCar(String, String, String)} returns true in the case of success adding
     *     is called. If so, in the case it is the first car in own for this user, the method {@link #openNewWindow(AuthorizedUser, Map)}
     *     is called and the OwnerAccount opens.
     *     This check is needed as this method is used in child-class OwnerAccount, but it is clear, that there it is a car owner,
     *     so we have no need to open OwnerAccount for him again after adding one more car in own
     *     </p>
     * @param user an Authorized user, who adds a car
     */
    protected void addCar(AuthorizedUser user) {
        try {
            String gosNum = TechnicalInfo.inputStr("Enter car number by format A123AA");
            String model = TechnicalInfo.inputStr("Enter car model");
            String seats = TechnicalInfo.inputStr("Enter number of seats:  2  ,  4  ,  6");
            boolean successAdd = user.addCar(gosNum, model, seats);
            if (successAdd && user.cars.size() == 1) {
                openNewWindow(user, ownerMenu);
                this.choose(exit, user);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Renting a car.
     * <p>
     *     First, the method {@link #seeAllAvailables()} returns a Map of found rental applications is called.
     *     Then it is a user's choice of the application by which he would like to rent a car.
     *     Second, the method {@link AuthorizedUser#rentCar(String, Map)} returns true in the case of success rent is called.
     *     This method is just a dialog method and can be deleted is needed.
     * </p>
     * @param renter an AuthorizedUser, who rents a car
     * @throws IOException if the input is incorrect
     */
    protected void takeCarInRent(AuthorizedUser renter) throws IOException {
        Map<Integer, RentalApplication> avails = seeAllAvailables();
        if (avails != null) {
            String choice = inputStr("\n\nCHOOSE YOUR VARIANT\n");
            boolean successRent = renter.rentCar(choice, avails);
            if (successRent) {
                System.out.println("\nSUCCESSFULLY\n");
            } else System.out.println("\nSOMETHING WENT WRONG\n");
        }
    }

    /**
     * Finishing order.
     * <p>
     *     Firstly, it is a print of all unfinished user's orders {@link AuthorizedUser#printUnfinishedOrders()}
     *     (returns false if there is no unfinished orders).
     *     Then it is user's input of data to finish order.
     *     Then the method {@link AuthorizedUser#finishOrder(String, String)} returns true in the case of success finished is called.
     *     As always, it is just a dialog method which can be deleted if needed.
     * </p>
     * @param user an AuthorizedUser, who finishes an order
     */
    protected void finishRent(AuthorizedUser user) throws IOException {
        if (!user.printUnfinishedOrders()) {
            return;
        }
        String choice = inputStr("Enter number of order choose\n");
        areasInfo();
        String areaChoice = inputStr("Choose zone for finishing order:\n");
        boolean successFinished = user.finishOrder(choice, areaChoice);
        if (successFinished) {
            System.out.println("\nORDER IS SUCCESSFULLY FINISHED\n");
        } else System.out.println("\nORDER ISN'T FINISHED\n");
    }

    protected void printAllOrders(AuthorizedUser renter){
        Map<Integer, Order> rentersOrders = renter.getMyOrders();
        if(rentersOrders != null){
        rentersOrders.forEach((a, b) -> System.out.println("\n\t\t\tVARIANT: " + a + ".\n " + b.toString()));}
        else System.out.println("\nORDERS' HISTORY IS EMPTY\n");
    }

}
