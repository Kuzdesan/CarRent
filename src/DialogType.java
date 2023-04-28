import java.io.IOException;
import java.sql.SQLException;

/**
 * Interface for general dialog's method.
 * <p>
 *     Choose option is a general method for classes Form, Account and OwnerAccount.
 *     This method accepts that type of user, for which the dialog is outputting.
 *     Form class is operating with unauthorized user so in Form class this method accepts an UnAuthorizedUser object.
 *     Account class is operating with authorizedUser so in Account class this method accepts an AuthorizedUser object.
 *     For each type of the "dialog" classes there is a different set of actions available for user
 * </p>
 * @param <T> - an object of class that extends an abstract class User: UnauthorizedUser or AuthorizedUser.
 *           param is envious of the user type this or another "dialog" operates with
 */
public interface DialogType<T extends User> {
    public void choose(int ch, T obj) throws IOException, SQLException;
}
