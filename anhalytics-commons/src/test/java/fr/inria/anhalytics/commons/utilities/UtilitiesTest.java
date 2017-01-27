package fr.inria.anhalytics.commons.utilities;


import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author azhar
 */
public class UtilitiesTest {

    @Test
    public void testDaysInMonths_standardMonths_30() throws Exception {
        assertThat(Utilities.daysInMonth(2400, 4), is(30));
        assertThat(Utilities.daysInMonth(2400, 6), is(30));
        assertThat(Utilities.daysInMonth(2400, 9), is(30));
        assertThat(Utilities.daysInMonth(2400, 11), is(30));

        assertThat(Utilities.daysInMonth(2003, 4), is(30));
        assertThat(Utilities.daysInMonth(2003, 6), is(30));
        assertThat(Utilities.daysInMonth(2003, 9), is(30));
        assertThat(Utilities.daysInMonth(2003, 11), is(30));
    }

    @Test
    public void testDaysInMonths_standardMonths_31() throws Exception {
        assertThat(Utilities.daysInMonth(2400, 1), is(31));
        assertThat(Utilities.daysInMonth(2400, 3), is(31));
        assertThat(Utilities.daysInMonth(2400, 5), is(31));
        assertThat(Utilities.daysInMonth(2400, 7), is(31));
        assertThat(Utilities.daysInMonth(2400, 8), is(31));
        assertThat(Utilities.daysInMonth(2400, 10), is(31));
        assertThat(Utilities.daysInMonth(2400, 12), is(31));

        assertThat(Utilities.daysInMonth(2003, 1), is(31));
        assertThat(Utilities.daysInMonth(2003, 3), is(31));
        assertThat(Utilities.daysInMonth(2003, 5), is(31));
        assertThat(Utilities.daysInMonth(2003, 7), is(31));
        assertThat(Utilities.daysInMonth(2003, 8), is(31));
        assertThat(Utilities.daysInMonth(2003, 10), is(31));
        assertThat(Utilities.daysInMonth(2003, 12), is(31));
    }

    @Test
    public void testDaysInMonth_february_nbOfDaysShouldAdaptAccordingToYear() throws Exception {
        assertThat(Utilities.daysInMonth(2400, 2), is(29));
        assertThat(Utilities.daysInMonth(1996, 2), is(29));
        assertThat(Utilities.daysInMonth(2003, 2), is(28));
    }
    
    
    
    
}
