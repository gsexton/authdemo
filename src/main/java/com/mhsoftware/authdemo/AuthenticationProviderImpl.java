package com.mhsoftware.authdemo;
import java.util.Date;

import com.password4j.*;

/**
 * An implementation of an authentication provider. It uses the Password4J library 
 * to transform the password using the Argon2 algorithm, and verify a supplied login 
 * password is correct. 
 *  
 * Initially, I was going to use BCrypt because I've used that before. I poked around 
 * and see the current concensus (not unanimous) opinion is to use Argon2, so that's 
 * what I'm using.
 *  
 * Given time constraints and since I'm not a cryptography expert, so I'm not vetting 
 * algorithms, optimizing parameters, etc. 
 *  
 * @author gsexton (12/10/21)
 */
public class AuthenticationProviderImpl extends AuthenticationProvider {

    public AuthenticationProviderImpl(PasswordPolicyValidator policyValidator) {
        this.setPasswordPolicyValidator(policyValidator);
    }

    /**
     * Verify the supplied password is correct for the user account. 
     *  
     * The implementation could be something that computes a hash and checks it, or 
     * it could be a callout to another authentication provider like LDAP/AD, etc. 
     */
    public boolean verifyPassword(UserAccount user, String password) throws AuthenticationException {

        verifyLoginPreconditions(user);

        boolean verified = Password.check(password, user.password).withArgon2();

        postLogin(user, verified);

        if (!verified) {
            throw new AuthenticationException("The supplied credentials are invalid.");
        }
        return true;
    }

    /**
     * Change the password for the account. 
     *  
     * Note that implementations may throw an exception if they're not capable of 
     * changing the password. For example, an OAuth authenticator would likely not 
     * be able to change the password for the account. 
     */
    public void changePassword(UserAccount user, String password) throws PasswordValidationException {
        PasswordPolicyValidator policyValidator = getPasswordPolicyValidator();
        if (policyValidator != null) {
            policyValidator.validatePassword(password);
        }
        // Add a random salt so that even if any two passwords are the same,
        // the output of the Argon2 function is unique.
        Hash hash = Password.hash(password).addRandomSalt(16).withArgon2();
        user.password = hash.getResult();
        user.pwdChangeDate = new Date(System.currentTimeMillis());
    }
}
