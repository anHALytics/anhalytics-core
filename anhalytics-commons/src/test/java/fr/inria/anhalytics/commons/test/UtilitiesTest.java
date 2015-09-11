package fr.inria.anhalytics.commons.test;

import static com.sun.org.apache.regexp.internal.RETest.test;
import fr.inria.anhalytics.commons.utilities.Utilities;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author azhar
 */
public class UtilitiesTest {

    @Test
    public void testDaysInMonth() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        int[] years = {2400, 1996, 2003};

        Class myTarget = Utilities.class;
        Class params[] = new Class[2];
        params[0] = int.class;
        params[1] = int.class;
        Method method = myTarget.getDeclaredMethod("daysInMonth", params);
        method.setAccessible(true);

        for (int y : years) {
            for (int i = 1; i < 13; i++) {
                if (i == 1 || i == 3 || i == 5 || i == 7 || i == 8 || i == 10 || i ==12) {
                    assertEquals(31, method.invoke(method, y, i));
                } else if (i == 2) {
                    if (((y % 4 == 0) && (y % 100 != 0)) || (y % 400 == 0)) {
                        assertEquals(29, method.invoke(method, y, i));
                    } else {
                        assertEquals(28, method.invoke(method, y, i));
                    }
                } else
                    assertEquals(30, method.invoke(method, y, i));
            }
        }
    }

    public void testUpdateDates() {

    }
}
