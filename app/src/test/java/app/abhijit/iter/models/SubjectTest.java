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
    public void generateBunkStats_NumberOfTotalClassesIsSmall_DoesNotContainRepeatedStats() {
        Subject subject = new Subject();
        subject.attendance = 100;
        subject.theoryClassesPresent = 9;
        subject.theoryClasses = 9;

        String bunkStats = "";
        bunkStats += "Bunk 3 classes for 75% attendance\n";
        bunkStats += "Bunk 2 classes for 80% attendance\n";
        bunkStats += "Bunk 1 class for 85% attendance";

        assertEquals(bunkStats, subject.generateBunkStats(75, true));
    }

    @Test
    public void generateBunkStats_NumberOfTotalClassesIsLarge_DoesNotContainRepeatedStats() {
        Subject subject = new Subject();
        subject.attendance = 85;
        subject.theoryClassesPresent = 17;
        subject.theoryClasses = 20;

        String bunkStats = "";
        bunkStats += "Bunk 2 more classes for 75% attendance\n";
        bunkStats += "Bunk 1 more class for 80% attendance\n";
        bunkStats += "Need 10 more classes for 90% attendance";

        assertEquals(bunkStats, subject.generateBunkStats(75, true));
    }

    @Test
    public void generateBunkStats_ZeroClasses_GeneratesEmptyStats() {
        Subject subject = new Subject();

        assertEquals("", subject.generateBunkStats(75, true));
    }
}
