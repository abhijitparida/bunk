/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Abhijit Parida <abhijitparida.me@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.abhijit.iter.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubjectTest {

    @Test
    public void attendance_SimpleValue_CalculatesCorrectly() {
        Subject subject = new Subject();
        subject.theoryPresent = 18;
        subject.theoryTotal = 22;

        assertEquals(81.81, subject.attendance(), 0.1);
    }

    @Test
    public void attendance_ZeroValues_HandlesGracefullyWithoutThrowingExceptions() {
        Subject subject = new Subject();

        assertEquals(0.0, subject.attendance(), 0.1);
    }

    @Test
    public void bunkStats_NumberOfTotalClassesIsSmall_DoesNotContainRepeatedStats() {
        Subject subject = new Subject();
        subject.theoryPresent = 9;
        subject.theoryTotal = 9;

        String bunkStats = "";
        bunkStats += "Bunk 3 classes for 75% attendance\n";
        bunkStats += "Bunk 2 classes for 80% attendance\n";
        bunkStats += "Bunk 1 class for 85% attendance";

        assertEquals(bunkStats, subject.bunkStats(75, true));
    }

    @Test
    public void bunkStats_NumberOfTotalClassesIsLarge_DoesNotContainRepeatedStats() {
        Subject subject = new Subject();
        subject.theoryPresent = 17;
        subject.theoryTotal = 20;

        String bunkStats = "";
        bunkStats += "Bunk 2 more classes for 75% attendance\n";
        bunkStats += "Bunk 1 more class for 80% attendance\n";
        bunkStats += "Need 10 more classes for 90% attendance";

        assertEquals(bunkStats, subject.bunkStats(75, true));
    }

    @Test
    public void bunkStats_ZeroTotalClasses_GeneratesEmptyStats() {
        Subject subject = new Subject();

        assertEquals("", subject.bunkStats(75, true));
    }

    @Test
    public void bunkStats_LessThanMinimumAttendance_GeneratesDontBunkWarning() {
        Subject subject = new Subject();
        subject.theoryPresent = 1;
        subject.theoryTotal = 10;

        String bunkStats = "";
        bunkStats += "DO NOT BUNK ANY MORE CLASSES\n";
        bunkStats += "Need 12 more classes for 60% attendance\n";
        bunkStats += "Need 15 more classes for 65% attendance\n";
        bunkStats += "Need 20 more classes for 70% attendance\n";
        bunkStats += "Need 26 more classes for 75% attendance\n";
        bunkStats += "Need 35 more classes for 80% attendance";

        assertEquals(bunkStats, subject.bunkStats(60, true));
    }

    @Test
    public void bunkStats_ZeroAttendance_GeneratesDontBunkWarning() {
        Subject subject = new Subject();
        subject.theoryPresent = 0;
        subject.theoryTotal = 35;

        String bunkStats = "";
        bunkStats += "DO NOT BUNK ANY MORE CLASSES";

        assertEquals(bunkStats, subject.bunkStats(75, true));
    }

    @Test
    public void bunkStats_ExtendedStatsFalse_GeneratesMaxTwoLines() {
        Subject subject = new Subject();
        subject.theoryPresent = 17;
        subject.theoryTotal = 20;

        String bunkStats = "";
        bunkStats += "Bunk 1 more class for 80% attendance\n";
        bunkStats += "Need 10 more classes for 90% attendance";

        assertEquals(bunkStats, subject.bunkStats(75, false));
    }
}
