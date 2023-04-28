import java.io.*;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class is used for storage some technical information like list of areas, different types of menu.
 * It also contains a lot of methods to the check the correctness of user's inputted values.
 * The most interesting here are
 * {@link #out(Map)},
 * {@link #openNewWindow(AuthorizedUser, Map)} and
 * {@link #isCorrectChoice(String)} methods
 */
public abstract class TechnicalInfo {
    /**
     * A code to exit from dialog froms
     */
    public final static int exit = 0;
    public static InputStream inputStream = System.in;
    public static Reader inputStreamReader = new InputStreamReader(inputStream);
    public static BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    /**
     * Zones where car can be given and taken in rent
     */
    public static LinkedList<Area> areas = new LinkedList<>();
    public static Map<Integer, String> unAuthorizedMenu = new HashMap<>();
    public static Map<Integer, String> authorizedMenu = new HashMap<>();
    public static Map<Integer, String> ownerMenu = new HashMap<>();


    public static String inputStr(String str) throws IOException {
        System.out.println(str);
        return (bufferedReader.readLine());
    }

    public static String inputStr() throws IOException {
        return (bufferedReader.readLine());
    }

    /**
     * Filling the different types of menu.
     * <p>
     * This method is called from the very beginning of the program to prepare the technical data;
     * This method is used for the filling of menus of actions;
     * </p>
     *
     * @see #unAuthorizedMenu
     * @see #authorizedMenu
     * @see #ownerMenu
     */
    public static void fillMenu() {
        unAuthorizedMenu.put(0, "exit");
        unAuthorizedMenu.put(1, "Authorization");
        unAuthorizedMenu.put(2, "Registration");

        authorizedMenu.put(0, "exit");
        authorizedMenu.put(1, "Personal data");
        authorizedMenu.put(2, "Add car");
        authorizedMenu.put(3, "See all cars available for rent");
        authorizedMenu.put(4, "See all cars available for rent by interval of price per minute");
        authorizedMenu.put(5, "See all cars available for rent by zone where car can be taken in rent");
        authorizedMenu.put(6, "Rent car");
        authorizedMenu.put(7, "See my orders");
        authorizedMenu.put(8, "Finish order");

        ownerMenu.putAll(authorizedMenu);
        ownerMenu.put(9, "See my cars");
        ownerMenu.put(10, "See my cars proposed for rent");
        ownerMenu.put(11, "Give car in rent");
    }

    /**
     * Print menu of available actions.
     * <p>
     * Depending on the acceptable type of menu this method will print the relevant variants of available actions
     * </p>
     *
     * @param menu type of the menu (unAuthorizedMenu, AuthorizedMenu, ownerMenu)
     */
    public static void out(Map<Integer, String> menu) {
        System.out.println("\nChoose action: ");
        for (Map.Entry<Integer, String> pair : menu.entrySet()) {
            System.out.println(pair.getKey() + ". " + pair.getValue());
        }
    }

    /**
     * Opening another "dialog" type.
     * <p>
     * This method is used for changing a type of "dialog":
     * when there is a successful authorization or registration we should change what the user sees and ables to do,
     * so another type of dialog with another available actions should be opened
     * </p>
     *
     * @param authorizedUser current user
     * @param menu           a type of "dialog" menu, which would be printed and used to choose the action
     */
    protected abstract void openNewWindow(AuthorizedUser authorizedUser, Map<Integer, String> menu) throws IOException;

    /**
     * Checking the correctness of choice of action to do.
     * <p>
     * As there is a menu with certain actions, user should be unable to input any variant which is distinct from proposed
     * </p>
     *
     * @param choice user's input of choice
     * @return true, if the choice is correct
     */
    public abstract boolean isCorrectChoice(String choice);

    public static boolean fillAreas() {
        ResultSet rs = BaseAdministration.collectAreas();
        try {
            while (rs.next()) {
                areas.add(new Area(rs.getInt("area_id"), rs.getString("area_value")));
            }
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public static void areasInfo() {
        for (Area area : areas) {
            System.out.println(area.id + ". " + area.location);
        }
    }

    public static Area getArea(int id) {
        for (Area area : areas) {
            if (area.id == id) {
                return area;
            }
        }
        return null;
    }

    public static boolean isNumeric(String s) {
        if (!s.isEmpty()) {
            ParsePosition pos = new ParsePosition(0);
            NumberFormat.getInstance().parse(s, pos);
            return s.length() == pos.getIndex();
        }
        return false;
    }

    public static boolean isRegistrationDataCorrect(String login, String password, String FIO, String passport, String telephone,
                                                    String birthYear, String birthMonth, String birthDay) throws NumberFormatException {

        boolean correctLogin = isCorrectLoginPassword(login);
        if (!correctLogin) {
            System.out.println("Login can't contain spaces!\t\tlogin:\t" + login);
            return false;
        }

        boolean correctPassword = isCorrectLoginPassword(password);
        if (!correctPassword) {
            System.out.println("Password can't contain spaces!\t\tpassword:\t" + password);
            return false;
        }

        boolean correctFio = isCorrectFio(FIO);
        if (!correctFio) {
            System.out.println("Wrong name format!\t\tname:\t" + FIO);
            return false;
        }

        if (passport.length() != 10 || !isNumeric(passport)) {
            System.out.println("Wrong passport format!\t\tpassport:\t" + passport);
            return false;
        }

        if (!isNumeric(telephone) || telephone.length() != 11 || !telephone.startsWith("8")) {
            System.out.println("Wrong telephone number format!\t\tphone number:\t" + telephone);
            return false;
        }

        if (!isNumeric(birthYear)) {
            System.out.println("Wrong birth year format!\t\tbirth year:\t" + birthYear);
            return false;
        }
        if (!isNumeric(birthMonth)) {
            System.out.println("Wrong birth month format!\t\tbirth month:\t" + birthMonth);
            return false;
        }
        if (!isNumeric(birthDay)) {
            System.out.println("Wrong birth day format!\t\tbirth day:\t" + birthDay);
            return false;
        }

        int birthYearInt = Integer.parseInt(birthYear);
        int birthMonthInt = Integer.parseInt(birthMonth);
        int birthDayInt = Integer.parseInt(birthDay);
        if (!isDateCorrect(birthYearInt, birthMonthInt, birthDayInt)) {
            return false;
        }

        boolean isAdult = isUserAdult(birthYearInt, birthMonthInt, birthDayInt);
        if (!isAdult) {
            System.out.println("Yoy are underage, you can't register!\t\t" + birthYear + "." + birthMonth + "." + birthDay + "\t");
            return false;
        }
        return true;

    }

    public static boolean isCarDataCorrect(String gosNum, String model, String seats) throws NumberFormatException {
        boolean numberCorrect = TechnicalInfo.isCarNumCorrect(gosNum);
        if (!numberCorrect) {
            System.out.println("Wrong car number format!\t\tcar number:\t" + gosNum);
            return false;
        }

        boolean modelCorrect = TechnicalInfo.isNotOnlySpecialSymbols(model);
        if (!modelCorrect) {
            System.out.println("Wrong model format!\t\tmodel:\t" + model);
            return false;
        }

        if (!isNumeric(seats)) {
            System.out.println("Wrong seats' number format!\t\tseats:\t" + seats);
            return false;
        }

        int seatsInt = Integer.parseInt(seats);
        if (!((seatsInt == 2) || (seatsInt == 4) || (seatsInt == 6))) {
            System.out.println("Wrong number of seats!\t\tseats:\t" + seats);
            return false;
        }
        return true;
    }

    /**
     * Checking the correctness of FIO.
     * <p>
     * FIO should contain at least one letter
     * </p>
     *
     * @param FIO user's inputted FIO
     * @return true if the fio is correct
     */
    private static boolean isCorrectFio(String FIO) {
        int lettersNum = 0;
        for (int i = 0; i < FIO.length(); i++) {
            char c = FIO.charAt(i);
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
                lettersNum++;
                continue;
            }
            if (c == ' ' || c == '-') { continue;
            } else {
                return false;
            }
        }
        return lettersNum != 0;
    }

    /**
     * Checking the correctness of inputted login and password.
     * <p>
     * Login and password cannot contain spaces
     * </p>
     *
     * @param data user's inputted login or password
     * @return true if the login or password are correct
     */
    private static boolean isCorrectLoginPassword(String data) {
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == ' ') {
                return false;
            }
        }
        return true;
    }

    public static boolean isDateCorrect(int birthYear, int birthMonth, int birthDay) {
        try {
            if (birthYear<1930) {
                System.out.println("Incorrect birth date!\t\tdate:\t" + birthYear + "." + birthMonth + "." + birthDay);
                return false;
            }
            LocalDate dat1 = LocalDate.of(birthYear, birthMonth, birthDay);
            return true;
        } catch (DateTimeException e) {
            System.out.println("Incorrect birth date!\t\tdate:\t" + birthYear + "." + birthMonth + "." + birthDay);
            return false;
        }
    }

    private static boolean isUserAdult(int birthYear, int birthMonth, int birthDay) {
        try {
            LocalDate dat1 = LocalDate.of(birthYear, birthMonth, birthDay);
            LocalDate dat2 = LocalDate.now();
            return Period.between(dat1, dat2).getYears() >= 18;
        } catch (DateTimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checking the correctness of car number.
     * <p>
     * Car number should be inputted by the format A123BC
     * </p>
     *
     * @param number user's inputted car number
     * @return true, if the number is correct
     */
    public static boolean isCarNumCorrect(String number) {
        if (number.length() != 6) {
            return false;
        }
        char first = number.charAt(0);
        char[] letters = {'E', 'T', 'Y', 'O', 'P', 'A', 'H', 'K', 'X', 'C', 'B', 'M'};
        boolean successFirst = false;
        for (char letter : letters) {
            if (letter == first) {
                successFirst = true;
                break;
            }
        }
        for (int i = 1; i < 4; i++) {
            char c = number.charAt(i);
            if ('0' >= c || c >= '9') {
                return false;
            }
        }
        char second = number.charAt(4);
        boolean successSecond = false;
        for (char letter : letters) {
            if (letter == second) {
                successSecond = true;
                break;
            }
        }
        char third = number.charAt(5);
        boolean successThird = false;
        for (char letter : letters) {
            if (letter == third) {
                successThird = true;
                break;
            }
        }
        if (!successFirst || !successSecond || !successThird) {
            return false;
        }
        return true;
    }

    public static boolean isNotOnlySpecialSymbols(String s) {
        int lettersNum = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ('0' <= c && c <= '9') {
                lettersNum++;
                break;
            }
            if ('a' <= c && c <= 'z') {
                lettersNum++;
                break;
            }
            if ('A' <= c && c <= 'Z') {
                lettersNum++;
                break;
            }
        }
        return lettersNum != 0;
    }

    public static boolean isCorrectDouble(String number) {
        String decimalPattern = "([0-9]*)\\.([0-9]*)";
        boolean match = Pattern.matches(decimalPattern, number);
        return !number.isEmpty() && (match || (isNumeric(number) && !number.startsWith("-")));
    }

    public static boolean isCorrectAreaId(String areaChoice) {
        if (isNumeric(areaChoice)) {
            int areaId = Integer.parseInt(areaChoice);
            Area areaToTake = getArea(areaId);
            if (areaToTake != null) {
                return true;
            } else
                System.out.println("Enter a correct zone identifier!\t\tyour choice:\t" + areaChoice);
        } else System.out.println("\nEnter a number!\t\tyour choice:\t" + areaChoice);
        return false;
    }

    public static Date dateFormat(LocalDateTime time) {
        return new Date(time.getYear() - 1900, time.getMonth().getValue() - 1, time.getDayOfMonth(), time.getHour(), time.getMinute());
    }

    public static boolean isRentDataCorrect(Map<Integer, Car> carsForRent, String carChoice, String yearToStartRent, String monthToStartRent, String dayToStartRent,
                                            String hourToStartRent, String minuteToStartRent, String yearToStopRent, String monthToStopRent, String dayToStopRent,
                                            String hourToStopRent, String minuteToStopRent, String areaToTake, String areaToReturn, String pricePerMinute) {

        if (!isNumeric(yearToStartRent) || !isNumeric(monthToStartRent) || !isNumeric(dayToStartRent) || !isNumeric(hourToStartRent) ||
                !isNumeric(minuteToStartRent) || !isNumeric(yearToStopRent) || !isNumeric(monthToStopRent) || !isNumeric(dayToStopRent) || !isNumeric(hourToStopRent) ||
                !isNumeric(minuteToStopRent)) {
            System.out.println("\nData and Time fields should be filled bu numbers!!\n");
            return false;
        }

        if (!isNumeric(areaToTake) || !isNumeric(areaToReturn)) {
            System.out.println("\nZone should be chosen by it's identifier!\t\tyour choice:\t" + areaToTake + " или " + areaToReturn);
            return false;
        }

        if (!isNumeric(carChoice)) {
            System.out.println("\nCar should be chosen by it's identifier!\t\tyour choice:\t" + carChoice);
            return false;
        }
        if (!isCorrectDouble(pricePerMinute)) {
            System.out.println("\nPrice per minute should be a number!\t\tyour price:\t" + pricePerMinute);
            return false;
        }

        int carCh = Integer.parseInt(carChoice);
        if (!carsForRent.containsKey(carCh)) {
            System.out.println("\nChoose car from the proposed list!\t\tyour choice:\t" + carCh);
            return false;
        }
        int areaTake = Integer.parseInt(areaToTake);
        int areaReturn = Integer.parseInt(areaToReturn);

        if (getArea(areaTake) == null || getArea(areaReturn) == null) {
            System.out.println("\nChoose zone from the proposed list!\t\tyour choice:\t" + areaToTake + "; " + areaReturn);
            return false;
        }
        double price = Double.parseDouble(pricePerMinute);
        if (price < 0) {
            System.out.println("\nPrice per minute of renting cannot be negative!\t\tyour price:\t" + price);
            return false;
        }

        try {
            LocalDateTime.of(
                    Integer.parseInt(yearToStartRent), Integer.parseInt(monthToStartRent),
                    Integer.parseInt(dayToStartRent), Integer.parseInt(hourToStartRent),
                    Integer.parseInt(minuteToStartRent)
            );

            LocalDateTime.of(
                    Integer.parseInt(yearToStopRent), Integer.parseInt(monthToStopRent),
                    Integer.parseInt(dayToStopRent), Integer.parseInt(hourToStopRent),
                    Integer.parseInt(minuteToStopRent));
            return true;
        } catch (DateTimeException e) {
            System.out.println("\nINCORRECT DATE OR TIME FORMAT\n");
            return false;
        }
    }
}
