import org.postgresql.util.PSQLException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Date;


public class BaseAdministration {
    private static Connection conn = null;
    private final static String adminLogin = BaseAdminData.getAdminLogin();
    private final static String adminPassword = BaseAdminData.getAdminPassword();
    private final static String testingLogin = "testingRenter";
    private final static String testingPassword = "12345";
    private final static String testingCarNumber = "A111AA";


    public static String getTestingLogin(String admLog, String admPass) {
        if (admLog.equals(adminLogin) && admPass.equals(adminPassword)) {
            return testingLogin;
        } else return null;
    }

    public static String getTestingPassword(String admLog, String admPass) {
        if (admLog.equals(adminLogin) && admPass.equals(adminPassword)) {
            return testingPassword;
        } else return null;
    }

    public static String getTestingCarNumber(String admLog, String admPass) {
        if (admLog.equals(adminLogin) && admPass.equals(adminPassword)) {
            return testingCarNumber;
        } else return null;
    }


    public static void preparingBase() {
        boolean successConn = connection();
        if (successConn) {
            System.out.println("Successful connection");
        } else {
            System.out.println("Connection is fault");
            return;
        }

        boolean successCreateTables = creteTables();
        if (successCreateTables) {
            System.out.println("\nTables are ready for work\n");
        } else {
            System.out.println("Tables' creating is fault");
            return;
        }

        boolean successFillAreasBase = fillAreas();
        if (successFillAreasBase) {
            System.out.println("Zones' data is filled");
        } else System.out.println("Zones' filling is fault");

        boolean successFillAreasList = TechnicalInfo.fillAreas();
        if (successFillAreasList) {
            System.out.println("List of zones' is filled");
        } else System.out.println("List of zones' filling is fault");

        TechnicalInfo.fillMenu();

        boolean successCreateTestingData = createTestingDate();
        if (successCreateTestingData) {
            System.out.println("Testing data successfully filled");
        } else System.out.println("Something went wrong with testing data");
    }

    private static boolean creteTables() {

        boolean successCreateAccounts = createTableAcc();
        if (successCreateAccounts) {
            System.out.println("Accounts table is ready for work");
        } else System.out.println("Accounts table creating is fault");

        boolean successCreateAuthorized = createTableAuth();
        if (successCreateAuthorized) {
            System.out.println("Authorized table is ready for work");
        } else System.out.println("Authorized table creating is fault");

        boolean successCreateCar = createTableCar();
        if (successCreateCar) {
            System.out.println("Car table is ready for work");
        } else System.out.println("Car table creating is fault");

        boolean successCreateAreas = createTableAreas();
        if (successCreateAreas) {
            System.out.println("Areas table is ready for work");
        } else System.out.println("Areas table creating is fault");

        boolean successCreateAvails = createTableAvails();
        if (successCreateAvails) {
            System.out.println("Availables table is ready for work");
        } else System.out.println("Availables table creating is fault");

        boolean successCreateOrders = createTableOrders();
        if (successCreateOrders) {
            System.out.println("Orders table is ready for work");
        } else System.out.println("Orders table creating is fault");

        return successCreateAccounts && successCreateAuthorized &&
                successCreateCar && successCreateAvails &&
                successCreateAreas && successCreateOrders;
    }

    private static boolean createTableAcc() {
        PreparedStatement stmtAccounts;
        String accCreate;
        try {
            accCreate = "CREATE TABLE IF NOT EXISTS accounts(login varchar PRIMARY KEY, password varchar NOT NULL)";
            conn.setAutoCommit(true);
            stmtAccounts = conn.prepareStatement(accCreate);
            stmtAccounts.executeUpdate();
            stmtAccounts.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static boolean createTableAuth() {
        PreparedStatement stmtAuthorized;
        String authCreate;
        try {
            authCreate = "CREATE TABLE IF NOT EXISTS authorized(" +
                    "login varchar primary key references accounts(login), " +
                    "fio varchar NOT NULL, " +
                    "passport varchar CHECK (length(passport)=10) NOT NULL," +
                    " telephone varchar  CHECK (length(telephone)=11) NOT NULL," +
                    " birth date)";
            conn.setAutoCommit(true);
            stmtAuthorized = conn.prepareStatement(authCreate);
            stmtAuthorized.executeUpdate();
            stmtAuthorized.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static boolean createTableCar() {
        PreparedStatement stmtCar;
        String carCreate;
        try {
            carCreate = "CREATE TABLE IF NOT EXISTS car(" +
                    "gos_number varchar CHECK (length(gos_number)=6) PRIMARY KEY," +
                    "model varchar NOT NULL, " +
                    "seats integer CHECK (seats in(2,4,6)) NOT NULL, " +
                    "status varchar CHECK (status in ('available_for_rent', 'in_rent', 'free', 'unavailable')) DEFAULT 'free' NOT NULL, " +
                    "owner_login varchar references accounts(login))";
            conn.setAutoCommit(true);
            stmtCar = conn.prepareStatement(carCreate);
            stmtCar.executeUpdate();
            stmtCar.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static boolean createTableAvails() {
        PreparedStatement stmtAvail;
        String createAvails;
        try {
            createAvails = "CREATE TABLE IF NOT EXISTS availables(\n" +
                    "available_id serial PRIMARY KEY, " +
                    "car_gos_number varchar CHECK (length(car_gos_number)=6) REFERENCES car(gos_number),\n" +
                    "area_to_take_id integer " +
                    "CHECK(area_to_take_id >= 0 AND area_to_take_id <5) REFERENCES areas(area_id) NOT NULL,\n" +
                    "area_to_return_id integer CHECK (area_to_return_id >=0 AND area_to_return_id<5) REFERENCES areas(area_id) NOT NULL,\n" +
                    "price_per_minute numeric(7, 2) CHECK (price_per_minute > 0) NOT NULL,\n" +
                    "time_to_take timestamp NOT NULL,\n" +
                    "time_to_return timestamp NOT NULL, " +
                    "availability varchar CHECK (availability in ('time-run', 'available', 'in_rent')) DEFAULT 'available', " +
                    "UNIQUE (car_gos_number, area_to_take_id, area_to_return_id, price_per_minute, time_to_take, time_to_return));";
            conn.setAutoCommit(true);
            stmtAvail = conn.prepareStatement(createAvails);
            stmtAvail.executeUpdate();
            stmtAvail.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static boolean createTableOrders() {
        PreparedStatement stmt;
        String create;
        try {
            create = "CREATE TABLE IF NOT EXISTS orders(\n" +
                    "order_id serial PRIMARY KEY,\n" +
                    "available_id integer REFERENCES availables(available_id) NOT NULL,\n" +
                    "renter_login varchar REFERENCES accounts(login) NOT NULL,\n" +
                    "area_was_returned_id integer REFERENCES areas(area_id),\n" +
                    "time_was_taken timestamp NOT NULL,\n" +
                    "time_was_returned timestamp,\n" +
                    "total_price numeric(7, 2),\n" +
                    "finished_status varchar CHECK (finished_status in ('finished', 'not_finished')) DEFAULT 'not_finished'\n" +
                    ");";
            conn.setAutoCommit(true);
            stmt = conn.prepareStatement(create);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static boolean createTableAreas() {
        PreparedStatement prep;
        String sql;
        try {
            sql = "CREATE TABLE IF NOT EXISTS areas(\n" +
                    "area_id integer CHECK(area_id >=0 AND area_id<5) PRIMARY KEY,\n" +
                    "area_value varchar CHECK (area_value in ('center', 'north', 'west', 'east', 'south')) UNIQUE NOT NULL);";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }


    private static boolean createTestingDate() {
        PreparedStatement stmtTest;
        String createTest;
        try {
            createTest =
                    "INSERT INTO accounts(login, password) " +
                            "SELECT 'testingRenter', '12345' " +
                            "WHERE NOT EXISTS  " +
                            "(SELECT login FROM accounts WHERE login = 'testingRenter');" +
                            "INSERT INTO authorized (login, fio, passport, telephone, birth) " +
                            "SELECT 'testingRenter', 'testingRenter','1234567890', '88008008080','2000-02-02' " +
                            "WHERE NOT EXISTS " +
                            "(SELECT login FROM authorized WHERE login = 'testingRenter'); " +
                            "INSERT INTO car (gos_number, model, seats, owner_login) " +
                            "SELECT 'A111AA', 'BMW', 4, 'testingRenter' " +
                            "WHERE NOT EXISTS " +
                            "(SELECT gos_number FROM car WHERE gos_number= 'A111AA');" +
                            "INSERT INTO car (gos_number, model, seats, owner_login)" +
                            "SELECT 'B123BB', 'Audi', 2, 'testingRenter' " +
                            "WHERE NOT EXISTS (SELECT gos_number FROM car WHERE gos_number= 'B123BB');" +
                            "" +
                            "INSERT INTO car (gos_number, model, seats, owner_login)" +
                            "SELECT 'K123KK', 'Mercedes', 6, 'testingRenter' " +
                            "WHERE NOT EXISTS (SELECT gos_number FROM car WHERE gos_number= 'K123KK');";
            conn.setAutoCommit(true);
            stmtTest = conn.prepareStatement(createTest);
            stmtTest.executeUpdate();
            stmtTest.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static boolean fillAreas() {
        PreparedStatement prep;
        String sql;
        try {
            sql = "INSERT INTO AREAS(area_id, area_value) " +
                    "SELECT '0', 'center' " +
                    "WHERE NOT EXISTS" +
                    "(SELECT area_id FROM AREAS WHERE area_id='0');" +
                    "INSERT INTO AREAS(area_id, area_value) " +
                    "SELECT '1', 'north' " +
                    "WHERE NOT EXISTS " +
                    "(SELECT area_id FROM AREAS WHERE area_id='1');" +
                    "INSERT INTO AREAS(area_id, area_value) " +
                    "SELECT '2', 'south' " +
                    "WHERE NOT EXISTS " +
                    "(SELECT area_id FROM AREAS WHERE area_id='2');" +
                    "INSERT INTO AREAS(area_id, area_value) " +
                    "SELECT '3', 'west' " +
                    "WHERE NOT EXISTS" +
                    "(SELECT area_id FROM AREAS WHERE area_id='3');" +
                    "INSERT INTO AREAS(area_id, area_value) " +
                    "SELECT '4', 'east' " +
                    "WHERE NOT EXISTS" +
                    "(SELECT area_id FROM AREAS WHERE area_id='4');";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.executeUpdate();
            prep.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }


    public static ResultSet collectAreas() {
        Statement stmt;
        try {
            String sql = "SELECT * FROM areas";
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private static boolean connection() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/project", adminLogin, adminPassword);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }


    private static boolean checkingAvailableTime() {
        PreparedStatement prep;
        String sql;
        try {
            sql = "UPDATE AVAILABLES SET availability='time-run' WHERE time_to_return < current_timestamp AND availability ='available';";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.executeUpdate();
            prep.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }


    public static boolean checkLogPass(String log, String pass) {
        boolean success = false;
        PreparedStatement prep;
        try {
            String sql = "SELECT* FROM ACCOUNTS WHERE login =? AND password = ?";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prep.setString(1, log);
            prep.setString(2, pass);
            ResultSet rs = prep.executeQuery();
            if (!rs.last()) {
                success = false;
            } else success = true;
            rs.close();
            prep.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return success;
    }


    public static void seeAllAcc() {
        Statement stmt;
        try {
            String sql = "SELECT * FROM accounts";
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println("логин: " + rs.getString("login") + "; пароль: " + rs.getString("password"));
            }
            rs.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public static void seeAllCars() {
        Statement stmt;
        try {
            String sql = "SELECT * FROM CAR";
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println("гос.номер: " + rs.getString("gos_number") + "; модель: " +
                        rs.getString("model") + "; количество мест: " + rs.getInt("seats") +
                        "; логин владельца: " + rs.getString("owner_login"));
            }
            rs.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public static boolean authorizedUserExists(String login) {
        boolean success = false;
        PreparedStatement prep;
        try {
            String sql = "SELECT* FROM AUTHORIZED WHERE login =?";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prep.setString(1, login);
            ResultSet rs = prep.executeQuery();
            if (!rs.last()) {
                success = false;
            } else success = true;
            rs.close();
            prep.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return success;
    }


    public static boolean registrationSuccess(String login, String name, String passport, String telephone, Date data) {
        PreparedStatement prep;
        boolean successInsert = false;
        boolean UserExists = authorizedUserExists(login);
        if (UserExists) {
            return false;
        }
        try {
            String sql = "INSERT INTO AUTHORIZED(login, FIO, passport, telephone, birth) VALUES (?,?, ?, ?, ?)";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prep.setString(1, login);
            prep.setString(2, name);
            prep.setString(3, passport);
            prep.setString(4, telephone);
            java.sql.Date date = new java.sql.Date(data.getTime());
            prep.setDate(5, date);
            try {
                prep.executeUpdate();
                successInsert = true;
            } catch (PSQLException e) {
                System.out.println(e);
                deleteAccount(login);
            }

            prep.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return successInsert;
    }


    private static void deleteAccount(String login) {
        PreparedStatement prep;
        try {
            String sql = "DELETE FROM ACCOUNTS WHERE login =?";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.setString(1, login);
            prep.executeUpdate();
            prep.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public static boolean insertUser(String log, String password) {
        boolean success = checkingLoginExist(log);

        /*если такого логина нет в бд, то success = false и мы можем создать пользователя с таким логином*/
        PreparedStatement prep;
        boolean successInsert = false;
        if (!success) {
            try {
                String sql = "INSERT INTO ACCOUNTS(login, password) VALUES (?,?)";
                conn.setAutoCommit(true);
                prep = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                prep.setString(1, log);
                prep.setString(2, password);
                prep.executeUpdate();
                successInsert = true;
                prep.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else successInsert = false;
        return successInsert;
    }


    public static boolean checkingLoginExist(String log) {
        boolean success = false;
        PreparedStatement prep;
        try {
            String sql = "SELECT* FROM ACCOUNTS WHERE login =?";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prep.setString(1, log);
            ResultSet rs = prep.executeQuery();
            if (!rs.last()) {
                success = false;
            } else success = true;
            rs.close();
            prep.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return success;
    }


    public static ResultSet collectRenterDate(String login, String pass) {
        PreparedStatement prep;
        ResultSet rs = null;
        try {
            String sql = "SELECT* FROM authorized WHERE login = (SELECT login FROM accounts WHERE login =? AND password = ?);";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.setString(1, login);
            prep.setString(2, pass);
            rs = prep.executeQuery();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return rs;
    }

    public static ResultSet collectCars(AuthorizedUser authorizedUser) {
        PreparedStatement prep;
        ResultSet rs = null;
        try {
            String sql = "SELECT* FROM CAR WHERE owner_login=?";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql);
            prep.setString(1, authorizedUser.login);
            rs = prep.executeQuery();

        } catch (Exception e) {
            System.out.println(e);
        }
        return rs;
    }


    public static boolean checkingCarInBase(String carGosNum) {
        boolean success = false;
        PreparedStatement prep;
        try {
            String sql = "SELECT* FROM CAR WHERE gos_number =?";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prep.setString(1, carGosNum);
            ResultSet rs = prep.executeQuery();
            if (!rs.last()) {
                success = false;
            } else success = true;
            rs.close();
            prep.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return success;

    }


    public static boolean addNewCar(String gosNum, String model, int seats, String ownerLogin) {
        PreparedStatement prep;
        boolean successInsert = false;
        try {
            String sql = "INSERT INTO CAR(gos_number, model, seats, owner_login) VALUES (?,?,?,?)";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prep.setString(1, gosNum);
            prep.setString(2, model);
            prep.setInt(3, seats);
            prep.setString(4, ownerLogin);
            prep.executeUpdate();
            successInsert = true;
            prep.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return successInsert;
    }

    public static ResultSet collectOwnerAvails(AuthorizedUser owner) {
        boolean successUpdatingAvailableTime = checkingAvailableTime();
        if (!successUpdatingAvailableTime) {
            return null;
        }
        PreparedStatement st;
        String sql;
        try {
            sql = "SELECT* FROM AVAILABLES as avail JOIN CAR as car on avail.car_gos_number = car.gos_number WHERE " +
                    "avail.availability='available' OR avail.availability='in_rent' AND  owner_login=? ORDER BY avail.time_to_take;";
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);
            st.setString(1, owner.login);
            ResultSet rs = st.executeQuery();
            return rs;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    public static ResultSet collectCarsCouldBeGivenInRent(AuthorizedUser owner) {
        boolean successUpdatingAvailableTime = checkingAvailableTime();
        if (!successUpdatingAvailableTime) {
            return null;
        }
        PreparedStatement st;
        String sql;
        try {
            sql = "WITH T0 AS (SELECT avail.available_id as available_id,\n" +
                    "ord.available_id as order_available_id, \n" +
                    "ord.finished_status as order_finished_status,\n" +
                    "all_cars.gos_number as gos_number, \n" +
                    "all_cars.model as car_model, \n" +
                    "all_cars.seats as seats,\n" +
                    "avail.time_to_take as time_car_is_unable_to_give_in_rent, \n" +
                    "ord.finished_status as status,\n" +
                    "COUNT(*) FILTER(where ord.finished_status = 'not_finished') as ck,\n" +
                    "GREATEST (MAX(ord.time_was_returned),avail.time_to_return ) as time_to_start_giving_in_rent\n" +
                    "FROM CAR as all_cars\n" +
                    "LEFT JOIN AVAILABLES as avail \n" +
                    "ON all_cars.gos_number = avail.car_gos_number \n" +
                    "LEFT JOIN ORDERS as ord\n" +
                    "ON avail.available_id = ord.available_id\n" +
                    "WHERE \n" +
                    "(\n" +
                    "  all_cars.owner_login = ? \n" +
                    "  AND (\n" +
                    "    avail.car_gos_number IS NULL\n" +
                    "    OR \n" +
                    "    (ord.available_id IS NULL AND avail.availability IS DISTINCT FROM 'time-run')\n" +
                    "    OR\n" +
                    "    (avail.availability IS DISTINCT FROM 'in_rent'\n" +
                    "     ))\n" +
                    ")\n" +
                    "GROUP BY \n" +
                    "avail.available_id, \n" +
                    "ord.available_id, \n" +
                    "ord.finished_status, \n" +
                    "all_cars.gos_number, \n" +
                    "all_cars.model, \n" +
                    "all_cars.seats, \n" +
                    "avail.time_to_take, \n" +
                    "avail.time_to_return \n" +
                    "\n" +
                    "HAVING \n" +
                    "(ord.available_id IS DISTINCT FROM NULL \n" +
                    "  OR \n" +
                    "  ord.available_id IS NULl)\n" +
                    "  ORDER BY avail.time_to_take),\n" +
                    "T1 AS (SELECT available_id as not_allowed FROM T0 WHERE status='not_finished')\n" +
                    "SELECT*FROM T0 LEFT JOIN T1 on (T0.available_id=T1.not_allowed) " +
                    "WHERE (T0.available_id IS DISTINCT FROM T1.not_allowed) OR T1.not_allowed IS NULL;";
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE);
            st.setString(1, owner.login);
            ResultSet rs = st.executeQuery();
            return rs;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    public static boolean giveCarInRent(Car car, Area areaToTake, Area areaToReturn,
                                        double pricePerMinute, LocalDateTime timeToStart, LocalDateTime timeToFinish) {
        Timestamp timeStart = Timestamp.valueOf(timeToStart);
        Timestamp timeFinish = Timestamp.valueOf(timeToFinish);

        PreparedStatement prep;
        String sql;
        try {
            sql = "INSERT INTO availables" +
                    "(car_gos_number," +
                    " area_to_take_id," +
                    "area_to_return_id," +
                    " price_per_minute," +
                    " time_to_take," +
                    " time_to_return)" +
                    "VALUES (?, ?, ?, ?, ?, ?);" +
                    "UPDATE CAR SET status = 'available_for_rent' WHERE gos_number =?;";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.setString(1, car.number);
            prep.setInt(2, areaToTake.id);
            prep.setInt(3, areaToReturn.id);
            prep.setDouble(4, pricePerMinute);
            prep.setTimestamp(5, timeStart);
            prep.setTimestamp(6, timeFinish);
            prep.setString(7, car.number);
            prep.executeUpdate();
            prep.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }


    public static ResultSet collectAvailables() {
        boolean successUpdatingAvailableTime = checkingAvailableTime();
        if (!successUpdatingAvailableTime) {
            return null;
        }
        PreparedStatement prep;
        String sql;
        try {
            sql = "SELECT* FROM AVAILABLES join CAR on car_gos_number=gos_number WHERE availability='available' AND status='available_for_rent' " +
                    "AND time_to_take <= current_timestamp;";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql);
            ResultSet rs = prep.executeQuery();
            return rs;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    public static ResultSet collectAvailables(double minRrice, double maxPrice) {
        boolean successUpdatingAvailableTime = checkingAvailableTime();
        if (!successUpdatingAvailableTime) {
            return null;
        }
        PreparedStatement prep;
        String sql;
        try {
            sql = "SELECT* FROM AVAILABLES  join CAR on car_gos_number=gos_number WHERE " +
                    "availability='available' AND status='available_for_rent' AND " +
                    "(price_per_minute BETWEEN ? AND ?)";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql);
            prep.setDouble(1, minRrice);
            prep.setDouble(2, maxPrice);
            ResultSet rs = prep.executeQuery();
            return rs;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    public static ResultSet collectAvailables(int areaToTakeId) {
        boolean successUpdatingAvailableTime = checkingAvailableTime();
        if (!successUpdatingAvailableTime) {
            return null;
        }
        PreparedStatement prep;
        String sql;
        try {
            sql = "SELECT* FROM AVAILABLES  join CAR on car_gos_number=gos_number WHERE " +
                    "availability='available' AND status='available_for_rent' AND " +
                    "area_to_take_id=?";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql);
            prep.setInt(1, areaToTakeId);
            ResultSet rs = prep.executeQuery();
            return rs;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    private static boolean changeStatuses(int availId) {
        PreparedStatement prep;
        String sql;
        try {
            sql = "UPDATE availables SET availability='in_rent' WHERE available_id=?;" +
                    "UPDATE Car SET status = 'in_rent' WHERE " +
                    "gos_number=(SELECT car_gos_number FROM availables WHERE available_id =?);";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.setInt(1, availId);
            prep.setInt(2, availId);
            prep.executeUpdate();
            prep.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }


    public static int rentCar(AuthorizedUser renter, int availId) {
        PreparedStatement prep;
        String sql;
        try {
            if (!changeStatuses(availId)) return 0;
            sql = "INSERT INTO orders (available_id, renter_login, time_was_taken) VALUES (?, ?, ?);";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            prep.setInt(1, availId);
            prep.setString(2, renter.login);
            Timestamp tm = Timestamp.valueOf(LocalDateTime.now());
            prep.setTimestamp(3, tm);
            prep.executeUpdate();
            ResultSet rs = prep.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (Exception e) {
            System.out.println(e);
            return 0;
        }
    }


    private static boolean changeTotalPrice() {
        PreparedStatement prep;
        String sql;
        try {
            sql = "UPDATE ORDERS SET total_price = (\n" +
                    "\tEXTRACT(YEAR FROM (localtimestamp-ord.time_was_taken))*365*30.5*24*60 +\n" +
                    "\tEXTRACT(MONTH FROM (localtimestamp-ord.time_was_taken))*30.5*24*60 +\n" +
                    "\tEXTRACT(DAY FROM (localtimestamp-ord.time_was_taken))*24*60 +\n" +
                    "\tEXTRACT(HOUR FROM (localtimestamp-ord.time_was_taken))*60 +\n" +
                    "\tEXTRACT(MINUTE FROM (localtimestamp-ord.time_was_taken)) + \n" +
                    "\tEXTRACT (SECOND FROM (localtimestamp-ord.time_was_taken))/60)*avail.price_per_minute FROM ORDERS\n" +
                    "\tas ord JOIN AVAILABLES as avail on ord.available_id= avail.available_id WHERE ord.finished_status='not_finished';";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.executeUpdate();
            prep.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }


    public static ResultSet collectOrders(AuthorizedUser renter) {
        if (!changeTotalPrice()) return null;
        PreparedStatement prep;
        String sql;
        try {
            sql = "SELECT* FROM AVAILABLES as avail JOIN CAR as car on avail.car_gos_number = car.gos_number\n" +
                    "JOIN ORDERS as ord on avail.available_id = ord.available_id WHERE ord.renter_login = ?;";
            conn.setAutoCommit(false);
            prep = conn.prepareStatement(sql);
            prep.setString(1, renter.login);
            return prep.executeQuery();
        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }
    }

    public static boolean finishOrder(int orderId, int areaToReturnId, int fine, String carStatus, String availStatus) {
        PreparedStatement prep;
        String sql;
        try {
            sql = "UPDATE ORDERS SET finished_status = 'finished', area_was_returned_id=?, " +
                    "time_was_returned=?, total_price=total_price+? WHERE order_id = ?;" +
                    "UPDATE AVAILABLES SET availability = ? WHERE available_id = (SELECT available_id FROM ORDERS where order_id = ?); " +
                    "UPDATE CAR SET status = ? WHERE gos_number = " +
                    "(SELECT car_gos_number FROM \n" +
                    "AVAILABLES WHERE available_id = (SELECT available_id FROM ORDERS where order_id = ?));";
            conn.setAutoCommit(true);
            prep = conn.prepareStatement(sql);
            prep.setInt(1, areaToReturnId);
            prep.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            prep.setInt(3, fine);
            prep.setInt(4, orderId);
            prep.setString(5, availStatus);
            prep.setInt(6, orderId);
            prep.setString(7, carStatus);
            prep.setInt(8, orderId);
            prep.executeUpdate();
            prep.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

}
