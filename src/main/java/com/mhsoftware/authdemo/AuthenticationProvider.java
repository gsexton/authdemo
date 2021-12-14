package com.mhsoftware.authdemo;

import java.util.Date;

/**
 * An abstract class for authentication providers to build from. 
 *  
 * You'll notice that I'm throwing AuthenticationExceptions rather than 
 * returning a result code. I'm explicitly doing this for safety reasons 
 * so that failure to check a result code won't result in the caller 
 * proceeding when they shouldn't. 
 *  
 * @author gsexton@mhsoftware.com
 */
public abstract class AuthenticationProvider {

    protected PasswordPolicyValidator policyValidator;



    /**
     * Verify preconditions are met. For example, is the account enabled.
     */
    public void verifyLoginPreconditions(UserAccount user) throws AuthenticationException {
        if (!user.enabled) {
            throw new AuthenticationException("The account is disabled.");
        }
        /* 
            TODO:
         
            This would be a good place to put in a rate limiter so that if the number of
            failed logins over a certain period occurs, the system temporarily disables
            login for that account. We don't want people to be able to brute-force the
            account password by shoving a dictionary through here.
         
            The design of the rate limiter would come from internal development/security
            standards.
        */
    }


    /**
     * Verify the supplied password is correct for the user account. 
     *  
     * The implementation could be something that computes a hash and checks it, or 
     * it could be a callout to another authentication provider like LDAP/AD, etc. 
     */
    public abstract boolean verifyPassword(UserAccount user, String password) throws AuthenticationException;

    /**
     * Change the password for the account. 
     *  
     * Note that implementations may throw an exception if they're not capable of 
     * changing the password. For example, an OAuth authenticator would likely not 
     * be able to change the password for the account. 
     */
    public abstract void changePassword(UserAccount user, String password) throws PasswordValidationException;

    public PasswordPolicyValidator getPasswordPolicyValidator() {
        return policyValidator;
    }

    public void setPasswordPolicyValidator(PasswordPolicyValidator policyValidator) {
        this.policyValidator = policyValidator;
    }

    /**
     * Take care of routine post-authentication chores. For example, resetting the bad login count, 
     * or setting the last signin date. 
     *  
     * @return boolean True if the account was modified and should be serialized. 
     */
    public boolean postLogin(UserAccount user,
                             final boolean success) throws AuthenticationException {

        if (!user.enabled) {
            //
            // I'm going to give a developer message here. For a real system, this should give the
            // message of something like "Internal error detected. Please contact technical support",
            // and the actual issue logged to the system's logs.
            //
            throw new AuthenticationException(this.getClass().getName() + ".postLogin() called for disabled user account!");
        }

        if (success) {
            user.badLoginCount = 0;
            user.lastSignin = new Date(System.currentTimeMillis());
        } else {
            if (user.badLoginCount < 0) {
                user.badLoginCount = 0;
            }
            user.badLoginCount++;
            PasswordPolicyValidator policyValidator = getPasswordPolicyValidator();
            if (policyValidator != null) {
                /* 
                TODO:   The system policy should be consulted, and if the bad login
                        count exceeds a specific value, the account should be disabled.
                        This gets complex, if you want to do things like specify policies
                        like: No more than 3 bad login attempts in a 5 minute window...
                 f
                Map<String, Object> policy = policyValidator.getPolicy(); 
                 
                if (policy.containsKey("badLoginCount") && user.badLoginCount>=((Integer)policy.get("badLoginCount")).intValue()) { 
                    user.enabled=false; 
                } 
                */
            }
            user.lastBadSignin = new Date(System.currentTimeMillis());
        }
        return true;
    }


}
