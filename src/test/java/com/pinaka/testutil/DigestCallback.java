

/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package com.pinaka.testutil;

import java.util.Map;
import java.security.MessageDigest;

/**
 * An interface that can be used to augment the behavior of a digest hash.
 * One example usecase is with the password based login modules to
 * modify the behavior of the hashing to introduce prefix/suffix salts.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public interface DigestCallback
{
   /** Pass through access to the login module options. When coming from a
    * login module this includes the following keys:
    * javax.security.auth.login.name - for the username
    * javax.security.auth.login.password - for the String password
    */
   public void init(Map options);
   /**
    * Pre-hash callout to allow for content before the password. Any content
    * should be added using the MessageDigest update methods.
    * @param digest - the security digest being used for the one-way hash
    */ 
   public void preDigest(MessageDigest digest);
   /** Post-hash callout afer the password has been added to allow for content
    * after the password has been added. Any content should be added using the
    * MessageDigest update methods.
    * @param digest - the security digest being used for the one-way hash
    */
   public void postDigest(MessageDigest digest);
}