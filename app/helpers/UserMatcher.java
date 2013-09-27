package helpers;
import models.User;

import org.mockito.ArgumentMatcher;

public class UserMatcher extends ArgumentMatcher<User> {
	@Override
	public boolean matches(Object user) {
       return true;
   }


}