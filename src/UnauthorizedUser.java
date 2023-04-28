import java.util.Date;

/**
 * Class on unauthorized user.
 * Such type of user in this project realization doesn't have any fields, but it can be added if needed.
 * The methods of this class are only registration and authorization
 */
public class UnauthorizedUser extends User {

    /**
     * Authorization method
     * <p>
     *     Calls the method {@link BaseAdministration#checkLogPass(String, String)} which returns true if user with
     *     such data exists in the Accounts
     * </p>
     * @param login user's input of login
     * @param password user's input of password
     * @return true if user with such data exists in the DataBase
     */
    public boolean authorization(String login, String password) {
        boolean success = BaseAdministration.checkLogPass(login, password);
        if (success) {
            return true;
        } else {
            System.out.println("Wrong login or password!\t\tlogin: '" + login + "'\tpassword: '" + password + "'");
            return false;
        }
    }

    /**
     * Registration (adding data to database).
     * <p>
     *     First, the data entered by the user is checked for correctness by
     *     {@link TechnicalInfo#isRegistrationDataCorrect(String, String, String, String, String, String, String, String)} returns true,
     *     if inputted data is correct.
     *     In this case, some fields converts in integer, some in date.
     *     Then the method {@link BaseAdministration#insertUser(String, String)} is called for inserting user's data in
     *     Accounts table and if this method returned true (insert was successful) the method
     *     {@link BaseAdministration#registrationSuccess(String, String, String, String, Date)} is called for inserting
     *     personal data in Authorized Table, which returns true in the case of success insert.
     *     And if the method of inserting in the table Accounts returns false this mean that the user with such login is already exists
     *     and the registration cannot be continued
     * </p>
     * @param login user's input of login
     * @param password user's input of password
     * @param FIO user's input of FIO
     * @param passport user's input of passport
     * @param telephone user's input of telephone
     * @param birthYear user's input of birthYear
     * @param birthMonth user's input of birthMonth
     * @param birthDay user's input of birthDay
     * @return true if the registration is success
     */
    public boolean registration(String login, String password, String FIO, String passport, String telephone,
                                String birthYear, String birthMonth, String birthDay) {

        if (TechnicalInfo.isRegistrationDataCorrect(login, password, FIO, passport, telephone, birthYear, birthMonth, birthDay)) {
            int birthYearInt = Integer.parseInt(birthYear);
            int birthMonthInt = Integer.parseInt(birthMonth);
            int birthDayInt = Integer.parseInt(birthDay);
            Date data = new Date(birthYearInt - 1900, (birthMonthInt - 1), birthDayInt);
            boolean successAddInAcc = BaseAdministration.insertUser(login, password);
            if (successAddInAcc) {
                boolean successAddInAuth = BaseAdministration.registrationSuccess(login, FIO, passport, telephone, data);
                if (successAddInAuth) {
                    return true;
                } else {
                    System.out.println("\nData adding error");
                    return false;
                }
            } else {
                System.out.println("Such login: '" + login + "' is already exists");
                return false;
            }
        }
        return false;
    }
}
