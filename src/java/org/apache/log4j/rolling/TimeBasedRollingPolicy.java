/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.rolling;

import org.apache.log4j.rolling.helper.Compress;
import org.apache.log4j.rolling.helper.DateTokenConverter;
import org.apache.log4j.rolling.helper.RollingCalendar;
import org.apache.log4j.rolling.helper.Util;

import java.io.File;

import java.util.Date;


/**
 * 
 *
 * If configuring programatically, do not forget to call {@link #activateOptions}
 * method before using this policy. Moreover, {@link #activateOptions} of
 * <code> TimeBasedRollingPolicy</code> must be called <em>before</em> calling
 * the {@link #activateOptions} method of the owning
 * <code>RollingFileAppender</code>.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingPolicy extends RollingPolicySkeleton
  implements TriggeringPolicy {
  static final String FNP_NOT_SET =
    "The FileNamePattern option must be set before using TimeBasedRollingPolicy. ";
  static final String SEE_FNP_NOT_SET =
    "See also http://logging.apache.org/log4j/codes.html#tbr_fnp_not_set";
  RollingCalendar rc;
  long nextCheck;
  Date lastCheck = new Date();
  String elapsedPeriodsFileName;

  public void activateOptions() {
    // find out period from the filename pattern
    if (fileNamePatternStr != null) {
      determineCompressionMode();
    } else {
      getLogger().warn(FNP_NOT_SET);
      getLogger().warn(SEE_FNP_NOT_SET);
      throw new IllegalStateException(FNP_NOT_SET + SEE_FNP_NOT_SET);
    }

    DateTokenConverter dtc = fileNamePattern.getDateTokenConverter();

    if (dtc == null) {
      throw new IllegalStateException(
        "FileNamePattern [" + fileNamePattern.getPattern()
        + "] does not contain a valid DateToken");
    }

    rc = new RollingCalendar();
    rc.init(dtc.getDatePattern());
    getLogger().debug(
      "The date pattern is '{}' from file name pattern '{}'.",
      dtc.getDatePattern(), fileNamePattern.getPattern());
    rc.printPeriodicity();

    long n = System.currentTimeMillis();
    lastCheck.setTime(n);
    nextCheck = rc.getNextCheckMillis(lastCheck);

    //Date nc = new Date();
    //nc.setTime(nextCheck);
    //getLogger().debug("Next check set to: " + nc);  
  }

  public void rollover() throws RolloverFailure {
    getLogger().debug("rollover called");
    getLogger().debug("compressionMode: " + compressionMode);

    if (activeFileName == null) {
      switch (compressionMode) {
      case Compress.NONE:
        // nothing to do;
        break;
      case Compress.GZ:
        getLogger().debug("GZIP compressing [{}]", elapsedPeriodsFileName);
        Compress.GZCompress(elapsedPeriodsFileName);
        break;
      case Compress.ZIP:
        getLogger().debug("ZIP compressing [{}]", elapsedPeriodsFileName);
        Compress.ZIPCompress(elapsedPeriodsFileName);
        break;
      }
    } else {
      switch (compressionMode) {
      case Compress.NONE:
        Util.rename(activeFileName, elapsedPeriodsFileName);
        break;
      case Compress.GZ:
        getLogger().debug("GZIP compressing [[}]", elapsedPeriodsFileName);
        Compress.GZCompress(activeFileName, elapsedPeriodsFileName);
        break;
      case Compress.ZIP:
        getLogger().debug("ZIP compressing [[}]", elapsedPeriodsFileName);
        Compress.ZIPCompress(activeFileName, elapsedPeriodsFileName);
        break;
      }
    }
  }

  /**
  *
  * The active log file is determined by the value of the activeFileName
  * option if it is set. However, in case the activeFileName is left blank,
  * then, the active log file equals the file name for the current period
  * as computed by the <b>FileNamePattern</b> option.
  *
  */
  public String getActiveFileName() {
    getLogger().debug("getActiveLogFileName called");
    if (activeFileName == null) {
      return fileNamePattern.convert(lastCheck);
    } else {
      return activeFileName;
    }
  }

  public boolean isTriggeringEvent(File file) {
    //getLogger().debug("Is triggering event called");
    long n = System.currentTimeMillis();

    if (n >= nextCheck) {
      getLogger().debug("Time to trigger rollover");

      // We set the elapsedPeriodsFileName before we set the 'lastCheck' variable
      // The elapsedPeriodsFileName corresponds to the file name of the period
      // that just elapsed.
      elapsedPeriodsFileName = fileNamePattern.convert(lastCheck);
      getLogger().debug(
        "elapsedPeriodsFileName set to {}", elapsedPeriodsFileName);

      lastCheck.setTime(n);
      //getLogger().debug("ActiveLogFileName will return " + getActiveLogFileName());
      nextCheck = rc.getNextCheckMillis(lastCheck);

      Date x = new Date();
      x.setTime(nextCheck);
      getLogger().debug("Next check on {}", x);

      return true;
    } else {
      return false;
    }
  }
}