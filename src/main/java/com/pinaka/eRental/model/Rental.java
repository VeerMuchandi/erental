/**
 *
 * @filename Rental.java
 * @author Veer Muchandi
 * @created Oct 26, 2012
 * © Copyright 2012 Pinaka LLC
 *
 */
package com.pinaka.eRental.model;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
/**
 * @filename Rental.java
 * @author Veer Muchandi
 * @created Oct 26, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
public @interface Rental {

}
