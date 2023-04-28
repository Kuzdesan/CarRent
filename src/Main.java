
import java.io.IOException;
import java.sql.*;
public class Main {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {

        BaseAdministration.preparingBase();
        BaseAdministration.seeAllAcc();
        UnauthorizedUser user = new UnauthorizedUser();
        Form newForm = new Form();
        int ch = 1;


      // Test.Testing(user, newForm);

        do{
            TechnicalInfo.out(TechnicalInfo.unAuthorizedMenu);
            String choice = TechnicalInfo.inputStr();
            if (newForm.isCorrectChoice(choice)){
                ch = Integer.parseInt(choice);
                newForm.choose(ch, user);
            }
        } while (ch!=TechnicalInfo.exit);

    }
}
