package com.mhsoftware.authdemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * A class to centralize password policy validation prior to a 
 * set/change password operation. 
 *  
 * The design permits multiple policies if that's something that's 
 * really needed. 
 *  
 * The only implemented policy is the minLength for the password. 
 */
public class PasswordPolicyValidator {

    private Map<String, Object> policy;

    /*
        Class-wide statics
    */

    /**
     * Map of policy names and instantiated PasswordPolicyValidators: 
     *  
     * k = PolicyName 
     * v = PasswordPolicyValidator 
     */
    private static Map<String, PasswordPolicyValidator> instances = new HashMap<>();
    private static Map<String, Map<String, Object>> passwordPolicies = new HashMap<>();

    public static PasswordPolicyValidator getDefaultPolicyValidator() {
        return getInstance(null);
    }

    public static void setDefaultPolicy(Map<String, Object> policy) {
        synchronized (passwordPolicies) {
            passwordPolicies.put(null, policy);
        }
    }

    public static PasswordPolicyValidator getInstance(String policyName) {
        PasswordPolicyValidator result = null;
        synchronized (passwordPolicies) {
            if (!passwordPolicies.containsKey(policyName)) {
                throw new RuntimeException("Attempt to create PasswordPolicyValidator when policy name " + policyName + " does not have a configured policy!");
            }
            result = instances.get(policyName);
            if (result == null) {
                result = new PasswordPolicyValidator(passwordPolicies.get(policyName));
                instances.put(policyName, result);
            }
        }
        return result;
    }

    /**
     * Add a new, named password policy. If it's an update, any created 
     * PasswordPolicyValidator instances are removed. 
     *  
     * Caching instances locally may result in use of a validator with an 
     * outdated policy. 
     * 
     * @param policyName 
     * @param configuration Policy key/value pairs.
     */
    public static void addPolicy(String policyName, Map<String, Object> configuration) {
        synchronized (passwordPolicies) {
            if (instances.containsKey(policyName)) {
                instances.remove(policyName);
            }
            passwordPolicies.put(policyName, configuration);
        }
    }


    private PasswordPolicyValidator(Map<String, Object> policy) {
        this.policy = policy;
    }

    /**
     * Get the policy map. It's returned as an unmodifiable map 
     * to prevent malicious use. 
     * 
     * @return Map&lt;String,Object&gt; 
     */
    public Map<String, Object> getPolicy() {
        return Collections.unmodifiableMap(policy);
    }

    /**
     * Ensure that a password meets required policy elements. 
     *  
     * @param password 
     * 
     * @return List&lt;String&gt; 
     *  
     * A list of failed validations for that proposed password. 
     * E.G. length, complexity, previously used, etc. 
     *  
     * For each validation key in the policy, the actual validation 
     * must be implemented in this routine. 
     *  
     * If a validation is found that isn't implemented, a runtime exception is thrown. 
     */
    public void validatePassword(String password) throws PasswordValidationException {
        List<String> messages = new ArrayList<>();
        for (Map.Entry<String, Object> me: policy.entrySet()) {
            switch (me.getKey()) {
            case "minLength":
                validateMinLength(password, (Integer)me.getValue(), messages);
                break;
            default:
                throw new RuntimeException(this.getClass().getName() + " validation for " + me.getKey() + " is not implemented!");
            }
        }
        if (messages.isEmpty()) {
            return;
        }
        throw new PasswordValidationException(messages);
    }

    private void validateMinLength(String password, Integer minLength, List<String> messages) {
        int pwLength = password == null ? -1 : password.trim().length();
        if (pwLength <= 0) {
            /*
                So, I'm hard coding these messages. In a production system, I would probably be using
                resource bundles for localization of messages into different locales and using a formatter
                to ensure elements are correctly localized.
            */
            messages.add("Empty password supplied.");
        } else if (pwLength < minLength.intValue()) {
            messages.add("The password length of " + pwLength + " is less than the required length of " + minLength.toString() + ".");
        }
    }
}

