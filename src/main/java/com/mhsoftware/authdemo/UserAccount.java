package com.mhsoftware.authdemo;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple representation of a user account. 
 *  
 * For simplicity, I'm dispensing with setters and getters. 
 *  
 * I'm also not implementing methods that I typically would. Some 
 * examples I'm not implementing: 
 *  
 * Comparable() interface 
 * 
 * @author gsexton (12/10/21)
 */
public class UserAccount {

    public int userID;
    public String userName;
    public String fullName;
    /** 
     * This is the TRANSFORMED password. For example, if Argon2 is used for password encryption, then 
     * this will be the hash value of the password. 
     *  
     * If the authentication provider uses an external store (e.g. ActiveDirectory/LDAP, SAML, etc) 
     * this value may be null. 
     */
    public String password;
    public String emailAddress;
    public boolean enabled;
    public Date lastSignin;
    public Date pwdChangeDate;
    public int badLoginCount;
    public Date lastBadSignin;

    public String toString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("America/Denver"));

        StringBuilder sb = new StringBuilder(256);
        sb.append("{userID: ").append(userID)
            .append(", userName: \"").append(userName).append("\"")
            .append(", fullName: \"").append(fullName).append("\"")
            .append(", emailAddress: \"").append(emailAddress).append("\"")
            .append(", password: \"").append(password).append("\"")
            .append(", enabled: ").append(enabled)
            .append(", badLoginCount: ").append(badLoginCount)
            .append(", lastSignin: ").append(lastSignin == null ? "null" : "\"" + df.format(lastSignin) + "\"")
            .append(", pwdChangeDate: ").append(pwdChangeDate == null ? "null" : "\"" + df.format(pwdChangeDate) + "\"")
            .append(", lastBadSignin: ").append(lastBadSignin == null ? "null" : "\"" + df.format(lastBadSignin) + "\"");

        return sb.toString();
    }

    public UserAccount() {
    }

    public boolean equals(Object o) {
        if (o instanceof UserAccount) {
            UserAccount other = (UserAccount)o;
            return this.userID == other.userID;
        }
        return false;
    }

    public int hashCode() {
        return userID;
    }
}
