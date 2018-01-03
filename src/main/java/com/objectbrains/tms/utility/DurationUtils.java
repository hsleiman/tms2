/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.utility;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import java.io.IOException;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

/**
 *
 * @author connorpetty
 */
public class DurationUtils {

    public static Duration getDuration(LocalDateTime start, LocalDateTime end) {
        return new Duration(start.toDateTime(DateTimeZone.UTC), end.toDateTime(DateTimeZone.UTC));
    }

    public static void writeDuration(ObjectDataOutput out, Duration duration) throws IOException {
        if (duration == null) {
            out.writeLong(0);
        } else {
            out.writeLong(duration.getMillis());
        }
    }

    public static Duration readDuration(ObjectDataInput in) throws IOException {
        return new Duration(in.readLong());
    }
}
