import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void Testing(UnauthorizedUser user, Form form) throws IOException, SQLException, InterruptedException {

        boolean successFormChoice = checkingChoice(form, fillWrongChoices());
        boolean successAccountChoice = checkingChoice(new Account(), fillWrongChoices());
        boolean successOwnerAccountChoice = checkingChoice(new OwnerAccount(), fillWrongChoices());
        String testingRenterLog = BaseAdministration.getTestingLogin("dasha", "123456789");
        String testingRenterPass = BaseAdministration.getTestingPassword("dasha", "123456789");
        String testingCarNumber = BaseAdministration.getTestingCarNumber("dasha", "123456789");
        AuthorizedUser testRenter = form.createRenter(BaseAdministration.collectRenterDate(testingRenterLog, testingRenterPass));
        boolean successAuthTest = checkAuthorization(testingRenterLog, testingRenterPass);
        boolean successRegistrationTest = checkRegistration(testRenter.login, fillWrongChoices());
        boolean successCarAddingTest = checkingCarAdding(testRenter, testingCarNumber);
        boolean successAvailableByPrice = checkingAvailableByPrice(fillWrongChoices());
        boolean successAvailableByArea = checkingAvailableByArea(fillWrongChoices());

        if (successFormChoice && successAccountChoice && successOwnerAccountChoice &&
                successAuthTest && successRegistrationTest && successCarAddingTest &&
                successAvailableByPrice && successAvailableByArea){
            for (int i =0; i<3;i++){
                System.out.println("\n\n\t\t\tALL TESTS ARE SUCCESSFUL\t\t\t");
            }
        }
//        boolean sucessAvailableUpdateStatus = checkingTimeRunStatusOfAvailable(testRenter);
//        boolean successRentTesting = checkingRentingCar(testRenter);
    }

    public static LinkedList<Object> fillWrongChoices() {
        LinkedList<Object> toTest = new LinkedList<Object>();
        toTest.add("dd");
        toTest.add("1 2");
        toTest.add(1.0);
        toTest.add("");
        toTest.add(-1);
        toTest.add("\n");
        toTest.add("\t");
        toTest.add(" ");
        return toTest;
    }


    public static boolean checkingChoice(Form form, LinkedList<Object> toTest) {
        boolean successTest = true;
        System.out.println("\n\n\t\t\tTESTING CHOICE FORM\t\t\t");
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        TechnicalInfo.unAuthorizedMenu.forEach((a, b) -> System.out.println("Possible variants: " + a + ". " + b));
        toTest.add(TechnicalInfo.unAuthorizedMenu.size());
        for (Object obj : toTest) {
            if (form.isCorrectChoice(obj.toString())) {
                successTest = false;
            }
        }
        if (!successTest) System.out.println("\nTEST FOR FORM CHOICE IS FAULT");
        else System.out.println("\nSUCCESSFULLY TEST FOR FORM CHOICE");
        return successTest;
    }

    public static boolean checkingChoice(Account account, LinkedList<Object> toTest) {
        boolean successTest = true;
        System.out.println("\n\n\t\t\tTESTING CHOICE ACCOUNT\t\t\t");
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        TechnicalInfo.authorizedMenu.forEach((a, b) -> System.out.println("Possible variants: " + a + ". " + b));
        toTest.add(-1);
        toTest.add(TechnicalInfo.authorizedMenu.size());
        for (Object obj : toTest) {
            if (account.isCorrectChoice(obj.toString())) {
                successTest = false;
            }
        }
        if (!successTest) System.out.println("\nTEST FOR ACCOUNT CHOICE IS FAULT");
        else System.out.println("\nSUCCESSFULLY TEST FOR ACCOUNT CHOICE");
        return successTest;
    }

    public static boolean checkingChoice(OwnerAccount ownerAccount, LinkedList<Object> toTest) {
        boolean successTest = true;
        System.out.println("\n\n\t\t\tTESTING CHOICE OWNER ACCOUNT\t\t\t");
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        TechnicalInfo.ownerMenu.forEach((a, b) -> System.out.println("Possible variants: " + a + ". " + b));
        toTest.add(TechnicalInfo.ownerMenu.size());
        for (Object obj : toTest) {
            if (ownerAccount.isCorrectChoice(obj.toString())) {
                successTest = false;
            }
        }
        if (!successTest) System.out.println("\nTEST FOR ACCOUNT CHOICE IS FAULT");
        else System.out.println("\nSUCCESSFULLY TEST FOR ACCOUNT CHOICE");
        return successTest;
    }

    public static boolean checkAuthorization(String testLog, String testPass) {
        UnauthorizedUser user = new UnauthorizedUser();
        System.out.println("\n\n\t\t\tTESTING AUTHORIZATION\t\t\t");
        System.out.println("~~~~~\t\tBase data\t\t~~~~~");
        BaseAdministration.seeAllAcc();
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        Map<Integer, Boolean> testAuth = new HashMap<>();
        int i = 0;
        testAuth.put(i++, user.authorization(testLog, "1325"));
        testAuth.put(i++, user.authorization("test", testPass));
        testAuth.put(i++, user.authorization("", "1325"));
        testAuth.put(i++, user.authorization("test", ""));
        testAuth.put(i++, user.authorization("", ""));
        int numOfSuccessRegTests = 0;
        for (Map.Entry<Integer, Boolean> pair : testAuth.entrySet()) {
            if (!pair.getValue()) numOfSuccessRegTests++;
        }
        if (testAuth.size() == numOfSuccessRegTests) {
            System.out.println("\nSUCCESSFULLY AUTHORIZATION TEST");
            return true;
        } else {
            System.out.println("\nTEST FOR AUTHORIZATION IS FAULT");
            return false;
        }

    }

    public static boolean checkRegistration(String testLog, LinkedList<Object> testingDates) {
        System.out.println("\n\n\t\t\tTESTING REGISTRATION\t\t\t");
        System.out.println("~~~~~\t\tBase data\t\t~~~~~");
        BaseAdministration.seeAllAcc();
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        Map<Integer, Boolean> regTests = new HashMap<>();
        UnauthorizedUser unauthorizedUser = new UnauthorizedUser();
        int i = 0;
        regTests.put(i++, unauthorizedUser.registration(testLog, "123", "dasha", "1234567890", "88008008080", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha maha", "123", "dasha", "1234567890", "880090090", "2022", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "12 3", "dasha", "1234567890", "880090090", "2000", "12", "12"));

        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Mas4ha", "1234567890", "880090090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha@", "1234567890", "880090090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", " - - ", "1234567890", "880090090", "2000", "12", "12"));

        regTests.put(i++, unauthorizedUser.registration("masha", "123", "dasha", "12345*7890", "880090090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "dasha", "12345X7890", "880090090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "dasha", "123456 789", "880090090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "dasha", "12345678909", "880090090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "dasha", "123456789", "880090090", "2000", "12", "12"));

        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "18009009090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "8a800900909", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "8 80090090", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "8800900909", "2000", "12", "12"));
        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "880090090909", "2000", "12", "12"));

        regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "88009009090", "2004", "12", "15"));
        for (Object obj : testingDates) {
            regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "88009009090", obj.toString(), "12", "15"));
        }
        for (Object obj : testingDates) {
            regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "88009009090", "2000", obj.toString(), "15"));
        }
        for (Object obj : testingDates) {
            regTests.put(i++, unauthorizedUser.registration("masha", "123", "Masha", "1234567890", "88009009090", "2000", "12", obj.toString()));
        }

        int numOfSuccessRegTests = 0;
        for (Map.Entry<Integer, Boolean> pair : regTests.entrySet()) {
            if (!pair.getValue()) numOfSuccessRegTests++;
        }

        if (regTests.size() == numOfSuccessRegTests) {
            System.out.println("\nSUCCESSFULLY REGISTRATION TEST");
            return true;
        } else {
            System.out.println("\nTEST FOR REGISTRATION IS FAULT");
            return false;
        }
    }

    public static boolean checkingCarAdding(AuthorizedUser renter, String testGosNum) throws IOException {
        System.out.println("\n\n\t\t\tTESTING CAR CREATING\t\t\t");
        System.out.println("~~~~~\t\tBase data\t\t~~~~~");
        BaseAdministration.seeAllCars();
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        Map<Integer, Car> testCar = new HashMap<>();
        int i = 0;
        testCar.put(i++, renter.createCar(testGosNum, "BMW", "4"));
        testCar.put(i++, renter.createCar("Q123AA", "bmw", "2"));

        testCar.put(i++, renter.createCar("A123DD", "bmw", "2"));
        testCar.put(i++, renter.createCar("A1A3AA", "bmw", "2"));
        testCar.put(i++, renter.createCar("A123A", "bmw", "2"));

        testCar.put(i++, renter.createCar(testGosNum, " ", "4"));
        testCar.put(i++, renter.createCar(testGosNum, "", "6"));
        testCar.put(i++, renter.createCar(testGosNum, ".-/", "2"));

        testCar.put(i++, renter.createCar(testGosNum, "bmw", "1"));
        testCar.put(i++, renter.createCar(testGosNum, "bmw", "\n"));
        testCar.put(i++, renter.createCar(testGosNum, "bmw", "xxx"));
        testCar.put(i++, renter.createCar(testGosNum, "bmw", "1x"));

        int numOfSuccessRegTests = 0;
        for (Map.Entry<Integer, Car> pair : testCar.entrySet()) {
            if (pair.getValue() == null) numOfSuccessRegTests++;
        }
        if (numOfSuccessRegTests == testCar.size()) {
            System.out.println("\nSUCCESSFULLY CAR ADDING TEST");
            return true;
        } else {
            System.out.println("\nTEST FOR CAR ADDING IS FAULT");
            return false;
        }

    }

    public static boolean checkingAvailableByPrice(LinkedList<Object> testingDates) {
        System.out.println("\n\n\t\t\tTESTING OUTPUT AVAILABLE BY PRICE\t\t\t");
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        Map<Integer, Map<Integer, RentalApplication>> availPriceTests = new HashMap<>();
        testingDates.remove(1.0);
        Account acc = new Account();
        int i = 0;
        for (Object obj : testingDates) {
            availPriceTests.put(i++, acc.collectAllAvailables(obj.toString(), "1.0"));
        }
        for (Object obj : testingDates) {
            availPriceTests.put(i++, acc.collectAllAvailables("1.0", obj.toString()));
        }
        int numOfSuccessRegTests = 0;
        for (Map.Entry<Integer, Map<Integer, RentalApplication>> pair : availPriceTests.entrySet()) {
            if (pair.getValue() == null) numOfSuccessRegTests++;
        }
        if (availPriceTests.size() == numOfSuccessRegTests) {
            System.out.println("\nSUCCESSFULLY SEE AVAILABLE BY PRICE TEST");
            return true;
        } else {
            System.out.println("\nTEST FOR OUTPUT AVAILABLE BY PRICE IS FAULT");
            return false;
        }
    }

    public static boolean checkingAvailableByArea(LinkedList<Object> testingDates) {
        System.out.println("\n\n\t\t\tTESTING OUTPUT AVAILABLE BY AREA\t\t\t");
        System.out.println("~~~~~\t\tAll Areas\t\t~~~~~");
        TechnicalInfo.areasInfo();
        System.out.println("\n~~~~~\t\tStart testing\t\t~~~~~");
        Map<Integer, Map<Integer, RentalApplication>> availPriceTests = new HashMap<>();
        testingDates.add(5);
        Account acc = new Account();
        int i = 0;
        for (Object obj : testingDates) {
            availPriceTests.put(i++, acc.collectAllAvailables(obj.toString()));
        }
        int numOfSuccessRegTests = 0;
        for (Map.Entry<Integer, Map<Integer, RentalApplication>> pair : availPriceTests.entrySet()) {
            if (pair.getValue() == null) numOfSuccessRegTests++;
        }
        if (availPriceTests.size() == numOfSuccessRegTests) {
            System.out.println("\nSUCCESSFULLY SEE AVAILABLE BY AREA TEST");
            return true;
        } else {
            System.out.println("\nTEST FOR OUTPUT AVAILABLE BY AREA IS FAULT");
            return false;
        }
    }

    /**
     * Checking changing the available status after certain time
     * <p>
     * When time to finish rent is before current time, this application becomes time-run and
     * unable for outputting
     * </p>
     */
    public static boolean checkingTimeRunStatusOfAvailable(AuthorizedUser owner) throws InterruptedException {
        Account acc = new Account();
        OwnerAccount ownAcc = new OwnerAccount();
        Map<Integer, RentalApplication> foundCars = Available.seeCarsToGiveInRent(BaseAdministration.collectCarsCouldBeGivenInRent(owner));
        Map<Integer, Car> cars = ownAcc.seeCanBeGivenInRent(owner, foundCars);
        String currDay = String.valueOf(LocalDateTime.now().getDayOfMonth());
        String currHours = String.valueOf(LocalDateTime.now().getHour());
        String currMinutes = String.valueOf(LocalDateTime.now().getMinute() + 1);
        String minuteToStop = String.valueOf(LocalDateTime.now().getMinute() + 2);
        owner.giveCarInRent(foundCars, cars, "0", "2022", "12", currDay, currHours, currMinutes,
                "2022", "12", currDay, currHours, minuteToStop, "1", "1", "10");
        acc.seeAllAvailables();
        TimeUnit.MINUTES.sleep(2);
        Map<Integer, RentalApplication> availables = acc.seeAllAvailables();
        return availables == null;
    }

    public static boolean checkingRentingCar(AuthorizedUser owner) {
        Account acc = new Account();
        OwnerAccount ownAcc = new OwnerAccount();
        Map<Integer, RentalApplication> foundCars = Available.seeCarsToGiveInRent(BaseAdministration.collectCarsCouldBeGivenInRent(owner));
        Map<Integer, Car> cars = ownAcc.seeCanBeGivenInRent(owner, foundCars);
        String currDay = String.valueOf(LocalDateTime.now().getDayOfMonth());
        String currHours = String.valueOf(LocalDateTime.now().getHour());
        String currMinutes = String.valueOf(LocalDateTime.now().getMinute() + 1);
        String minuteToStop = String.valueOf(LocalDateTime.now().getMinute() + 5);
        if (owner.giveCarInRent(foundCars, cars, "0", "2022", "12", currDay, currHours, currMinutes,
                "2022", "12", currDay, currHours, minuteToStop, "1", "1", "10")) {
            System.out.println("Successfully given in rent");
        } else System.out.println("Smth went wrong");
        Map<Integer, RentalApplication> avails = acc.seeAllAvailables();
        owner.rentCar("1", avails);
        if (owner.rentCar("0", avails)) {
            System.out.println("Successful");
            return true;
        }
        return false;
    }
}
