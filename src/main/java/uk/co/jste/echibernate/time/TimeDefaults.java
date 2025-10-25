package uk.co.jste.echibernate.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class TimeDefaults {
  public static final ZoneOffset DEFAULT_SYSTEM_TIME_ZONE = ZoneOffset.UTC;
  public static final Instant START_OF_TIME = LocalDate.of(1900, 1, 1).atStartOfDay().toInstant(DEFAULT_SYSTEM_TIME_ZONE);
  public static final Instant END_OF_TIME = LocalDate.of(9999, 1, 1).atStartOfDay().toInstant(DEFAULT_SYSTEM_TIME_ZONE);
}
