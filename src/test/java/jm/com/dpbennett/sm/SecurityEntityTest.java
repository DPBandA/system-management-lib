/*
Report Manager (RM) - Reporting module. 
Copyright (C) 2021  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
package jm.com.dpbennett.sm;

import java.util.Hashtable;
import javax.naming.Context;
import org.junit.Test;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;

/**
 *
 * @author Desmond Bennett
 */
public class SecurityEntityTest {

//    @Test
//    public void testLDAP() {
//        try {
//           
//            String username = "dbennett";
//            // tk to be made system options
//            String searchControlsReturningAttributes = "uid";
//            String securityPrincipalPrefix = "uid=";
//            String usernameSuffix = ",";
//            String distinguishedNameString = "dc=dpbennett,dc=com,dc=jm"; // DN - use domainName as in database instead?
//            // tk end system options
//            
//            String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
//            String securityAuthentication = "simple";
//            String securityPrincipal = securityPrincipalPrefix + 
//                    username + 
//                    usernameSuffix 
//                    + distinguishedNameString; // tk should be distinguishedName in database not domainName?
//            String securityCredentials = "";
//            String providerUrl = "ldap://dpbennett.com.jm:389";
//            Hashtable env = new Hashtable();
//
//            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
//            env.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);
//            env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
//            env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
//            env.put(Context.PROVIDER_URL, providerUrl);
//
//            InitialLdapContext ctx = new InitialLdapContext(env, null);
//
//            SearchControls constraints = new SearchControls();
//            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
//            String[] attrIDs = {searchControlsReturningAttributes};
//
//            constraints.setReturningAttributes(attrIDs);
//
//            //String ctxName = "dc=dpbennett,dc=com,dc=jm"; // tk should be distinguishedName in database?
//            NamingEnumeration answer = ctx.search(distinguishedNameString, "SAMAccountName=" + username, constraints);
//
//            if (!answer.hasMore()) { // Assuming only one match
//                System.out.println("User found!");
//            }
//        } catch (NamingException ex) {
//            System.out.println("Naming exception!: " + ex);
//
//        }
//
//    }

}
