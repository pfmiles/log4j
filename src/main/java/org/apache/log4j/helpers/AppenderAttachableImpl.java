/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.helpers;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;

/**
   A straightforward implementation of the {@link AppenderAttachable}
   interface.

   @author Ceki G&uuml;lc&uuml;
   @since version 0.9.1 */
public class AppenderAttachableImpl implements AppenderAttachable {
  
  /** Array of appenders. */
  protected List<Appender> appenderList;

  /**
     Attach an appender. If the appender is already in the list in
     won't be added again.
  */
  public
  void addAppender(Appender newAppender) {
    // Null values for newAppender parameter are strictly forbidden.
    if(newAppender == null)
      return;
    
    if(appenderList == null) {
      appenderList = new CopyOnWriteArrayList<Appender>();
    }
    if(!appenderList.contains(newAppender))
      appenderList.add(newAppender);
  }

  /**
     Call the <code>doAppend</code> method on all attached appenders.  */
  public
  int appendLoopOnAppenders(LoggingEvent event) {
    int size = 0;

    if(appenderList != null) {
      size = appenderList.size();
      for(Appender a: appenderList) a.doAppend(event);
    }    
    return size;
  }


  /**
     Get all attached appenders as an Enumeration. If there are no
     attached appenders <code>null</code> is returned.
     
     @return Enumeration An enumeration of attached appenders.
   */
  public
  Enumeration<Appender> getAllAppenders() {
    if(appenderList == null) {
      return NullEnumeration.getInstance();
    } else { 
      final Iterator<Appender> iter = appenderList.iterator();
      return new Enumeration<Appender>(){

        public boolean hasMoreElements() {
            return iter.hasNext();
        }

        public Appender nextElement() {
            return iter.next();
        }
        
    };    
    }
  }

  /**
     Look for an attached appender named as <code>name</code>.

     <p>Return the appender with that name if in the list. Return null
     otherwise.  
     
   */
  public
  Appender getAppender(String name) {
     if(appenderList == null || name == null)
      return null;

     int size = appenderList.size();
     Appender appender;
     for(int i = 0; i < size; i++) {
       appender = (Appender) appenderList.get(i);
       if(name.equals(appender.getName()))
	  return appender;
     }
     return null;    
  }


  /**
     Returns <code>true</code> if the specified appender is in the
     list of attached appenders, <code>false</code> otherwise.

     @since 1.2 */
  public 
  boolean isAttached(Appender appender) {
    if(appenderList == null || appender == null)
      return false;

     int size = appenderList.size();
     Appender a;
     for(int i = 0; i < size; i++) {
       a  = (Appender) appenderList.get(i);
       if(a == appender)
	  return true;
     }
     return false;    
  }



  /**
   * Remove and close all previously attached appenders.
   * */
  public
  void removeAllAppenders() {
    if(appenderList != null) {
      int len = appenderList.size();      
      for(int i = 0; i < len; i++) {
	Appender a = (Appender) appenderList.get(i);
	a.close();
      }
      appenderList.clear();
      appenderList = null;      
    }
  }


  /**
     Remove the appender passed as parameter form the list of attached
     appenders.  */
  public
  void removeAppender(Appender appender) {
    if(appender == null || appenderList == null) 
      return;
    appenderList.remove(appender);    
  }


 /**
    Remove the appender with the name passed as parameter form the
    list of appenders.  
  */
  public
  void removeAppender(String name) {
    if(name == null || appenderList == null) return;
    int size = appenderList.size();
    for(int i = 0; i < size; i++) {
      if(name.equals(((Appender)appenderList.get(i)).getName())) {
	 appenderList.remove(i);
	 break;
      }
    }
  }

}
