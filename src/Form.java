import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * Class for unauthorized user's actions (similar to unauthorized user menu form).
 * <p>
 *     Class is a "first step" of opening a menu and provide the authorization and registration since the user is unauthorized.
 *     This class is used for unauthorized user menu printing and
 *     also for calling methods from this menu depending on the user's choice.
 *     Class is mainly used for getting user's input and calling methods of other classes.
 *     In the case of success authorization or registration an authorized (owner) accounts are opening in cycle for
 *     this authorized user returned from the methods called from the methods of this class.
 * </p>
 */
public class Form extends TechnicalInfo implements DialogType<UnauthorizedUser> {

    /**
     * Method which checks if the user's choice of menu action is correct
     * <p>
     * Method checks if the choice is a number and then checks if the unAuthorized menu contains this key.
     * </p>
     * @param choice a String variable of the user's enter
     * @return true in case of correct choice and false if incorrect
     */
    @Override
    public boolean isCorrectChoice(String choice) {
        if (isNumeric(choice)) {
            int ch = Integer.parseInt(choice);
            if (unAuthorizedMenu.containsKey(ch)) {
                return true;
            }
        }
        System.out.println("\nIncorrect value!\t\tYour value: " + choice);
        return false;
    }

    /**
     * Choosing an action to do depending on the user's input.
     * <p>
     *     Method which calls other methods depending on the entered number;
     *     This method is usually called in a cycle until the user enters a zero (exit case)
     * </p>
     * @param ch user's choice of action to do
     * @param unauthorizedUser object of UnAuthorizedUser class which now works with program
     * @throws IOException when the entered value is incorrect
     * @throws SQLException as methods operating with DataBase are called from here
     */
    @Override
    public void choose(int ch, UnauthorizedUser unauthorizedUser) throws IOException, SQLException {
        switch (ch) {
            case 1 -> authorizationFillData(unauthorizedUser);
            case 2 -> registration(unauthorizedUser);
        }
    }

    /**
     * Authorization data filling and authorization method calling.
     * <p>
     *     Firstly user's input of login and password happens.
     *     Then the method {@link #authorization(UnauthorizedUser, String, String)} is calling.
     *     If the returned value of AuthorizedUser is true, the registration is successful.
     *     In this case {@link #openNewWindow(AuthorizedUser, Map)} is calling - the opening of
     *     Account (OwnerAccount) begins. And finally this form closes by sending an exit code (0) to
     *     {@link #choose(int, UnauthorizedUser)}
     *     If necessary this method can be deleted as just a "dialog" method without a semantic load
     * </p>
     * @param unauthorizedUser object of UnauthorizedUser for which the authorization is completing
     */
    private void authorizationFillData(UnauthorizedUser unauthorizedUser) throws IOException, SQLException {
        String log = inputStr("Enter login: ");
        String pass = inputStr("Enter password: ");
        AuthorizedUser renter = authorization(unauthorizedUser, log, pass);
        if (renter == null) {
            System.out.println("Authorization fault");
            return;
        }
        System.out.println("Successful authorization!\t" + log);
        openNewWindow(renter, authorizedMenu);
        choose(exit, unauthorizedUser);
    }

    /**
     * Authorization methods' calling.
     * <p>
     *     This method calls {@link UnauthorizedUser#authorization(String, String)} method that returns true in the case of
     *     success authorization. If so, calling a method for creating an AuthorizedUser object happens
     *     {@link #createRenter(ResultSet)}
     *     which accepts the result of select query to the data base {@link BaseAdministration#collectRenterDate(String, String)}
     *     for collecting this authorized user's data and
     *     returns an AuthorizedUser object if the authorization is successful
     * </p>
     * @param unauthorizedUser an object for which the authorization is completing
     * @param log an inputted user's login
     * @param pass an inputted user's password
     * @return AuthorizedUser object (would be null if the authorization is fault)
     */
    private AuthorizedUser authorization(UnauthorizedUser unauthorizedUser, String log, String pass) {
        boolean success = unauthorizedUser.authorization(log, pass);
        if (success) {
            return createRenter(BaseAdministration.collectRenterDate(log, pass));
        } else return null;
    }

    /**
     * Registration data filling and registration methods calling.
     * <p>
     *     Firstly unauthorized user's input of registration data is happening.
     *     Then the method {@link UnauthorizedUser#registration(String, String, String, String, String, String, String, String)}
     *     is calling.
     *     If the returned value is true, the inputted data is correct and this unauthorized user's data was
     *     successfully added to DataBase.
     *     Then method {@link #createRenter(ResultSet)} is calling :
     *     if the returned object is null the registration is fault;
     *     if the returned object is distinct from null - the registration is success and
     *     methods {@link #openNewWindow(AuthorizedUser, Map)} and {@link #choose(int, UnauthorizedUser)} are calling
     *     (opening an Account(OwnerAccount) and closing current Form are happening)
     * </p>
     * @param unauthorizedUser UnAuthorizedUser object for which registration is completing
     */
    private void registration(UnauthorizedUser unauthorizedUser) throws IOException, SQLException {
        String login = inputStr("Enter login: ");
        String password = inputStr("Enter password: ");
        String FIO = inputStr("Enter name");
        String passport = inputStr("Enter passport serial and number without spaces");
        String telephone = inputStr("Enter phone number like 88005553535");
        String birthYear = inputStr("Enter birth year");
        String birthMonth = inputStr("Enter birth month");
        String birthDay = inputStr("Enter birth day");

        boolean success = unauthorizedUser.registration(login, password, FIO, passport, telephone, birthYear, birthMonth, birthDay);
        if (success) {
            AuthorizedUser renter = createRenter(BaseAdministration.collectRenterDate(login, password));
            if (renter == null) {
                System.out.println("\nRegistration is fault");
                return;
            }
            System.out.println("\nSUCCESSFUL REGISTRATION");
            openNewWindow(renter, authorizedMenu);
            choose(exit, unauthorizedUser);
        }
    }

    /**
     * Opening an Account (OwnerAccount).
     * <p>
     *     When the registration or authorization was successfully finished
     *     this method is called for opening a new type of "dialog".
     *     Here is a check if an authorized user has cars in own :
     *     if he has, then a method
     *     {@link Account#openNewWindow(AuthorizedUser, Map)} is called for opening an owner's "dialog"-ownerMenu;
     *     if he doesn't have cars in own then here in cycle Account opening is beginning :
     *     {@link #out(Map)} accepts authorizedMenu is called and
     *     {@link Account#choose(int, AuthorizedUser)} accepts this authorized user is called
     * </p>
     * @param renter an AuthorizedUser for which the Account(OwnerAccount) will be opened
     * @param menu a type of menu for this class - unAuthorizedMenu
     */
    @Override
    protected void openNewWindow(AuthorizedUser renter, Map<Integer, String> menu) throws IOException {
        int ptr = 1;
        Account acc = new Account();
        if (renter.cars != null && renter.cars.size() != 0) {
            acc.openNewWindow(renter, ownerMenu);
        }
        do {
            out(menu);
            String choice = inputStr("");
            if (acc.isCorrectChoice(choice)) {
                ptr = Integer.parseInt(choice);
                acc.choose(ptr, renter);
            }
        } while (ptr != exit);
    }

    /**
     * Creating authorised user object by its data in DataBase.
     * <p>
     *     This method collects the user's data returned from the dataBase.
     *     Firstly an AuthorizedUser object creating happens.
     *     Then if this object is distinct from null, the method {@link BaseAdministration#collectCars(AuthorizedUser)}
     *     is calling : here is an attempt to collect authorized user's cars from Data Base.
     *     If the cars were found, for each record in rsCars the Car object is creating and adding to authorizedUser cars
     *     {@link AuthorizedUser#cars}.
     *     Then here is an attempt to collect authorized user's orders history
     *     by calling a method {@link AuthorizedUser#refreshOrders()}
     * </p>
     * @param rsUser the ResultSet returned from DataBase, which contains this user's personal data
     * @return AuthorizedUser renter (would be null if the collecting its personal data from DataBase is fault)
     */
    public AuthorizedUser createRenter(ResultSet rsUser) {
        AuthorizedUser renter = null;
        try {
            while (rsUser.next()) {
                String log = rsUser.getString("login");
                String name = rsUser.getString("fio");
                String passport = rsUser.getString("passport");
                String telephone = rsUser.getString("telephone");
                Date birth = rsUser.getDate("birth");
                renter = new AuthorizedUser(log, name, passport, telephone, birth);
            }
            try {
                if (renter != null) {
                    ResultSet rsCars = BaseAdministration.collectCars(renter);
                    while (rsCars.next()) {
                        String carNumber = rsCars.getString("gos_number");
                        String model = rsCars.getString("model");
                        int seats = rsCars.getInt("seats");
                        renter.cars.put(renter.cars.size(), new Car(carNumber, model, seats, renter));
                        try {
                            renter.refreshOrders();
                        } catch (NullPointerException e) {
                            System.out.println(e);
                        }
                    }
                    rsCars.close();
                }
                rsUser.close();
                return renter;
            } catch (SQLException e) {
                System.out.println("CAR'S DATA GETTING IS FAULT" + e);
                return null;
            }
        } catch (SQLException e) {
            System.out.println("USER'S DATA GETTING IS FAULT" + e);
            return null;
        }
    }

}
