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

import java.util.Formatter;

/**
 * This class represents a subject.
 */
public class Subject {

    public String name;
    public String code;
    public long lastUpdated;
    public double attendance;
    public int theoryClassesPresent;
    public int theoryClasses;
    public int labClassesPresent;
    public int labClasses;

    public String generateBunkStats(int minimumAttendance, boolean extendedStats) {
        StringBuilder bunkStats = new StringBuilder();
        int attendance = (int) this.attendance;
        int classes = this.theoryClasses + this.labClasses;
        int classesPresent = this.theoryClassesPresent + this.labClassesPresent;
        int classesAbsent = classes - classesPresent;

        if (attendance <= 75) {
            bunkStats.append("DO NOT BUNK ANY MORE CLASSES\n");
        } else {
            for (int a = 75; a < attendance; a += 5) {
                int daysBunk = (int) ((100 * classesPresent / (double) a) - (double) classes);
                if (daysBunk > 0) {
                    bunkStats.append(new Formatter().format("Bunk %d%s %s for %d%% attendance\n",
                            daysBunk,  classesAbsent == 0 ? "" : " more",
                            daysBunk == 1 ? "class" : "classes", a));
                }
            }
        }

        int nextAttendance = (attendance + 4) / 5 * 5;
        if (nextAttendance == attendance) nextAttendance = attendance + 5;
        if (nextAttendance < 75) nextAttendance = 75;
        for (int a = nextAttendance; a <= 95; a += 5) {
            int daysNeed = (int) ((a * classes - 100 * classesPresent) / (double) (100 - a));
            if (daysNeed > 0 && (daysNeed + classes <= 50)) {
                bunkStats.append(new Formatter().format("Need %d more %s for %d%% attendance\n",
                        daysNeed, daysNeed == 1 ? "class" : "classes", a));
            }
        }

        if (bunkStats.length() != 0) bunkStats.setLength(bunkStats.length() - 1);

        return bunkStats.toString();
    }
}
