package helpers;

import java.util.List;

import org.mockito.ArgumentMatcher;

class IsListOfTwoElements extends ArgumentMatcher<List> {
    public boolean matches(Object list) {
        return ((List) list).size() == 2;
    }
}