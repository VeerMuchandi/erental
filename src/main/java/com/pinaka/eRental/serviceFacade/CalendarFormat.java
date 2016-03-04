package com.pinaka.eRental.serviceFacade;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.jboss.resteasy.annotations.StringParameterUnmarshallerBinder;

/**
 * 
 * @filename CalendarFormat.java
 * @author Muchandi
 * @created Dec 5, 2012
 *
 * © Copyright 2012 Pinaka LLC
 *
 */
@Retention(RUNTIME)
@StringParameterUnmarshallerBinder(CalendarFormatter.class)
public @interface CalendarFormat {
	String value();

}
