package com.mhsoftware.authdemo;
import java.util.List;

/**
 * Exception thrown when a password validiation error arises.
 * 
 * @author gsexton (12/10/21)
 */
public class PasswordValidationException extends Exception {

    private List<String> messages;

    public PasswordValidationException(List<String> messages) {
        super();
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Password Validation Error\n\nWhile validating the password, the following errors were found:\n");
        messages.forEach(msg -> sb.append(msg).append("\n"));
        return sb.toString();
    }
}

