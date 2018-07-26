package com.networknt.oauth.spnego;

import com.networknt.config.Config;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;

/**
 * Utility class to start up a test KDC backed by a directory server.
 *
 * It is better to start the server once instead of once per test but once running
 * the overhead is minimal. However a better solution may be to use the suite runner.
 *
 * TODO - May be able to add some lifecycle methods to DefaultServer to allow
 * for an extension.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class KerberosKDCUtil {
    static final String CONFIG_SPNEGO = "spnego";
    static final SpnegoConfig config = (SpnegoConfig)Config.getInstance().getJsonObjectConfig(CONFIG_SPNEGO, SpnegoConfig.class);

    public static Subject login(final String userName, final char[] password) throws LoginException {
        Subject theSubject = new Subject();
        CallbackHandler cbh = new UsernamePasswordCBH(userName, password);
        LoginContext lc = new LoginContext("KDC", theSubject, cbh, createJaasConfiguration());
        lc.login();

        return theSubject;
    }

    private static Configuration createJaasConfiguration() {
        return new Configuration() {

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                if (!"KDC".equals(name)) {
                    throw new IllegalArgumentException("Unexpected name '" + name + "'");
                }

                AppConfigurationEntry[] entries = new AppConfigurationEntry[1];
                Map<String, Object> options = new HashMap<>();
                options.put("debug", config.getDebug());
                options.put("refreshKrb5Config", "true");
                options.put("storeKey", "true");
                if("true".equalsIgnoreCase(config.getUseKeyTab())) {
                    options.put("useKeyTab", config.getUseKeyTab());
                    options.put("keyTab", config.getKeyTab());
                    options.put("principal", config.getPrincipal());
                }
                options.put("isInitiator", "true");
                entries[0] = new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule", REQUIRED, options);
                return entries;
            }

        };
    }

    private static class UsernamePasswordCBH implements CallbackHandler {

        /*
         * Note: We use CallbackHandler implementations like this in test cases as test cases need to run unattended, a true
         * CallbackHandler implementation should interact directly with the current user to prompt for the username and
         * password.
         *
         * i.e. In a client app NEVER prompt for these values in advance and provide them to a CallbackHandler like this.
         */

        private final String username;
        private final char[] password;

        private UsernamePasswordCBH(final String username, final char[] password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback current : callbacks) {
                if (current instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) current;
                    ncb.setName(username);
                } else if (current instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) current;
                    pcb.setPassword(password);
                } else {
                    throw new UnsupportedCallbackException(current);
                }
            }

        }

    }
}
