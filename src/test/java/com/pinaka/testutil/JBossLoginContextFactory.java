/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.pinaka.testutil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

//import org.jboss.security.auth.callback.ObjectCallback;
//import org.jboss.security.auth.callback.SecurityAssociationCallback;




public class JBossLoginContextFactory {

    static class NamePasswordCallbackHandler implements CallbackHandler {
        private  final String username;
        private String password;


        private NamePasswordCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public void setPassword(String password) {
        	this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback current : callbacks) {
                if (current instanceof NameCallback) {
                    ((NameCallback) current).setName(username);
                } else if (current instanceof PasswordCallback) {
                    ((PasswordCallback) current).setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(current);
                }
            }
        }
    }
    
//    static class SecurityAssociationHandler implements CallbackHandler
//    {
//       private transient Principal principal;
//       private transient Object credential;
//
//       private SecurityAssociationHandler()
//       {
//       }
//
//       /** Initialize the UsernamePasswordHandler with the principal
//        and credentials to use.
//        */
//       private SecurityAssociationHandler(Principal principal, Object credential)
//       {
//          this.principal = principal;
//          this.credential = credential;
//       }
//
//       public void setSecurityInfo(Principal principal, Object credential)
//       {
//          this.principal = principal;
//          this.credential = credential;
//       }
//
//       /** Handles SecurityAssociationCallback, ObjectCallback, NameCallback and
//        PasswordCallback types. A NameCallback name property is set to
//        the Prinicpal.getName() value. A PasswordCallback password property is
//        set to the getPassword() value. The preferred SecurityAssociationCallback
//        has its principal and credential properties set to the instance principal
//        and credential. An ObjectCallback has its credential set to the credential
//        value.
//
//        @see #getPassword()
//        @exception UnsupportedCallbackException, thrown if any callback of
//        type other than SecurityAssociationCallback, ObjectCallback, NameCallback
//        or PasswordCallback are seen.
//        */
//       public void handle(Callback[] callbacks) throws
//          UnsupportedCallbackException
//       {
//          for (int i = 0; i < callbacks.length; i++)
//          {
//             Callback c = callbacks[i];
//             if (c instanceof SecurityAssociationCallback)
//             {
//                SecurityAssociationCallback sac = (SecurityAssociationCallback) c;
//                sac.setPrincipal(principal);
//                sac.setCredential(credential);
//             }
//             else if (c instanceof ObjectCallback)
//             {
//                ObjectCallback oc = (ObjectCallback) c;
//                oc.setCredential(credential);
//             }
//             else if (c instanceof NameCallback)
//             {
//                NameCallback nc = (NameCallback) c;
//                if (principal != null)
//                   nc.setName(principal.getName());
//             }
//             else if (c instanceof PasswordCallback)
//             {
//                PasswordCallback pc = (PasswordCallback) c;
//                char[] password = getPassword();
//                if (password != null)
//                   pc.setPassword(password);
//             }
//             else
//             {
//                throw new UnsupportedCallbackException(c, "Unrecognized Callback");
//             }
//          }
//       }
//
//       /** Try to convert the credential value into a char[] using the
//        first of the following attempts which succeeds:
//
//        1. Check for instanceof char[]
//        2. Check for instanceof String and then use toCharArray()
//        3. See if credential has a toCharArray() method and use it
//        4. Use toString() followed by toCharArray().
//        @return a char[] representation of the credential.
//        */
//       private char[] getPassword()
//       {
//          char[] password = null;
//          if (credential instanceof char[])
//          {
//             password = (char[]) credential;
//          }
//          else if (credential instanceof String)
//          {
//             String s = (String) credential;
//             password = s.toCharArray();
//          }
//          else
//          {
//             try
//             {
//                Class[] types = {};
//                Method m = credential.getClass().getMethod("toCharArray", types);
//                Object[] args = {};
//                password = (char[]) m.invoke(credential, args);
//             }
//             catch (Exception e)
//             {
//                if (credential != null)
//                {
//                   String s = credential.toString();
//                   password = s.toCharArray();
//                }
//             }
//          }
//          return password;
//       }
//    }

    static class JBossJaasConfiguration extends Configuration {
        private final String configurationName;

        JBossJaasConfiguration(String configurationName) {
            this.configurationName = configurationName;
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if (!configurationName.equals(name)) {
                throw new IllegalArgumentException("Unexpected configuration name '" + name + "'");
            }

            return new AppConfigurationEntry[] {
            		
//replaced the UserRolesLoginModule with DatabaseServerLoginModule
//            createUsersRolesLoginModuleConfigEntry(), 
            createDatabaseModuleConfigEntry(),

            createClientLoginModuleConfigEntry(),

            };
        }


        private AppConfigurationEntry createDatabaseModuleConfigEntry() {
            Map<String, String> options = new HashMap<String, String>();
            options.put("dsJndiName", "java:jboss/datasources/MysqlDS");
            options.put("principalsQuery", "select Password from Principals where PrincipalID=?");
            options.put("rolesQuery", "select Role, RoleGroup from Roles where PrincipalID=?");

/* Using without digest authentication for testing due to serialization issues with RFC2617Digest */            
//            options.put("hashAlgorithm", "MD5");
//            options.put("hashEncoding", "RFC2617");
//            options.put("hashUserPassword", "false");
//            options.put("hashStorePassword", "true");
//            options.put("passwordIsA1Hash", "true");
//            options.put("storeDigestCallback", "com.pinaka.testutil.RFC2617Digest");
//            options.put("storeDigestCallback", "org.jboss.security.auth.callback.RFC2617Digest");
            options.put("password-stacking", "useFirstPass");
            
            
            return new AppConfigurationEntry("org.jboss.security.auth.spi.DatabaseServerLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
        }
        
        /**
         * The {@link org.jboss.security.auth.spi.UsersRolesLoginModule} creates the association between users and
         * roles.
         * 
         * @return
         */
        private AppConfigurationEntry createUsersRolesLoginModuleConfigEntry() {
            Map<String, String> options = new HashMap<String, String>();
            return new AppConfigurationEntry("org.jboss.security.auth.spi.UsersRolesLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
        }

        /**
         * The {@link org.jboss.security.ClientLoginModule} associates the user credentials with the
         * {@link org.jboss.security.SecurityContext} where the JBoss security runtime can find it.
         * 
         * @return
         */
        private AppConfigurationEntry createClientLoginModuleConfigEntry() {
            Map<String, String> options = new HashMap<String, String>();
            options.put("multi-threaded", "true");
            options.put("restore-login-identity", "true");

            return new AppConfigurationEntry("org.jboss.security.ClientLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
        }
    }

    /**
     * Obtain a LoginContext configured for use with the ClientLoginModule.
     * 
     * @return the configured LoginContext.
     */
    public static LoginContext createLoginContext(final String username, final String password, final String realm) throws LoginException {
        final String configurationName = "Arquillian Testing";

        CallbackHandler cbh = new JBossLoginContextFactory.NamePasswordCallbackHandler(username, generateDigestPassword(username, password, realm)); //As digest callback is throwing exception, passing digest password directly
//      CallbackHandler cbh = new JBossLoginContextFactory.NamePasswordCallbackHandler(username, password);// Need to replace this if the mapcallback serializable exception goes away for digest authentication
        
       
//        Principal userPrincipal = new Principal() {
//            public String getName()  {
//                return username;
//               }
//             };
//             
//        Object pwd = password;
//        
        
//        CallbackHandler scbh = new JBossLoginContextFactory.SecurityAssociationHandler(userPrincipal, pwd);

        System.out.println("Logincontext Username"+username);
        System.out.println("Logincontext password"+password);
        System.out.println("Logincontext realm"+realm);
        Configuration config = new JBossJaasConfiguration(configurationName);

        return new LoginContext(configurationName, new Subject(), cbh, config);
    }
    

/**
 * Generates a password that can be verified against the digest authentication password
 * @param userName
 * @param password
 * @param realm
 * @return
 */
    public static String generateDigestPassword(String userName, String password, String realm) {
		
		return md5Hex(userName+":"+realm+":"+password);
	}
	
	private static String md5Hex(String data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
 
        return new String(encodeStringToHex(digest.digest(data.getBytes())));
    }

   private static String encodeStringToHex(byte[] rawData){

	StringBuffer hexText = new StringBuffer();
	String initialHex = null;
	int initHexLength = 0;
	for (int i = 0; i < rawData.length; i++) {
		int positiveValue = rawData[i] & 0x000000FF;
		initialHex = Integer.toHexString(positiveValue);
		initHexLength = initialHex.length();
		while (initHexLength++ < 2) {
			hexText.append("0");
			}

		hexText.append(initialHex);
		}
	return hexText.toString();
	}

}
