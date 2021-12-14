package com.mhsoftware.authdemo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This is the main implementation of the AuthDemo
 * 
 * Some things to note: 
 *  
 * I'm passing the password on the command line. That's a bad practice because 
 * any user that can list processes would be able to see the value. Two 
 * ways of doing it correctly would be accepting the password on stdin or 
 * accepting it in an environment variable. I'm not doing that because of 
 * time constraints. 
 *  
 * Additionally, for password change, I'm not requiring the old password. 
 * For a real CLI, that would probably be good to do. 
 */
public class App {

    /**
     * Parse arguments in the form --argName=argValue into a map of 
     *  
     * argName: argValue 
     * 
     * @param args 
     * 
     * @return Map&lt;String,String&gt; 
     */
    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            String[] halves = arg.split("=");
            String fld = halves[0].replace("--", ""),
                value = null;

            if (halves.length == 2) {
                value = halves[1];
            }
            options.put(fld, value);
        }
        return options;
    }

    private static void usage() {
        System.out.println("\n\nUsage: java -jar authdemo-jar-with-dependencies.jar options\n\n" +
                           "Where options are one of the following:\n\n" +
                           "\t--add userName=userName --password=passwordValue --fullName=\"full name\" --emailAddress=someone@domain.com\n" +
                           "\t--changePassword userName=userName password=newPassword\n" +
                           "\t--login --userName=userName --password=password\n" +
                           "\t--enable --userName=userName\n" +
                           "\t--disable --userName=userName\n" +
                           "\t--delete --userName=userName\n" +
                           "\t--query --userName=userName\n" +
                           "\t--listAccounts\n\n"
                          );
    }

    private static UserAccount getUserAccount(Map<String, String> commandOptions) {
        if (!commandOptions.containsKey("userName")) {
            System.out.println("A required userName was not specified. Aborting....");
            usage();
            System.exit(1);
        }
        return AccountStore.getInstance().getAccount(commandOptions.get("userName"));
    }

    private static void listAccounts() {
        Collection<UserAccount> accounts = AccountStore.getInstance().getAccounts();
        if (accounts.size() == 0) {
            System.out.println("No accounts found.");
        } else {
            accounts.forEach(account -> System.out.println(account));
        }
    }

    public static void deleteAccount(Map<String, String> commandOptions){

        UserAccount account = getUserAccount(commandOptions);
        if (account == null) {
            System.out.println("The specified account was not found.");
            return;
        }
        if (AccountStore.getInstance().deleteAccount(account.userID)) {
            System.out.println("The account was deleted.");
        } else {
            System.out.println("The account was not deleted!");
        }
    }

    public static void setEnabled(Map<String, String> commandOptions, final boolean enabled){
        UserAccount account = getUserAccount(commandOptions);
        if (account == null) {
            System.out.println("The specified account credentials are invalid.");
            return;
        }
        account.enabled = enabled;
        AccountStore.getInstance().updateAccount(account);
        System.out.println("Account Updated. New Value: " + account);
    }

    public static void query(Map<String, String>commandOptions){
        UserAccount account = getUserAccount(commandOptions);
        System.out.println("Query Result: " + account);
    }

    public static void login(Map<String, String> commandOptions){
        String userName = commandOptions.getOrDefault("userName", null),
            password = commandOptions.getOrDefault("password", null);
        if (userName == null || password == null) {
            System.out.println("You must specify userName and password for a login operation.\n");
            usage();
            return;
        }
        UserAccount account = getUserAccount(commandOptions);
        if (account == null) {
            System.out.println("The specified account credentials are invalid.");
            return;
        }
        AuthenticationProvider ap = new AuthenticationProviderImpl(PasswordPolicyValidator.getDefaultPolicyValidator());
        try {
            ap.verifyPassword(account, password);
            System.out.println("Login was successful.");
        } catch (AuthenticationException ae) {
            System.err.println(ae);
        }
        AccountStore.getInstance().updateAccount(account);
    }

    public static void addAccount(Map<String, String> fields){

        UserAccount account = getUserAccount(fields);
        if (account != null) {
            System.out.println("The account: " + account.userName + " already exists. Skipping add.");
            return;
        }
        account = new UserAccount();
        account.enabled = true;
        for (Map.Entry<String, String> field: fields.entrySet()) {
            switch (field.getKey()) {
                /*
                    I could use reflection here...
                */
            case "userName":
                account.userName = field.getValue();
                break;
            case "fullName":
                account.fullName = field.getValue();
            case "emailAddress":
                account.emailAddress = field.getValue();
                break;
            case "password":
                // That's done below.
                break;
            }
        }
        String password = fields.getOrDefault("password", null);
        if (password == null) {
            System.out.println("No password specified. Aborting.\n");
            return;
        }
        PasswordPolicyValidator policyValidator = PasswordPolicyValidator.getDefaultPolicyValidator();
        System.out.println("Plicy Valudator=" + policyValidator);
        try {
            AuthenticationProvider ap = new AuthenticationProviderImpl(policyValidator);
            ap.changePassword(account, password);
            System.out.println("Account Password Set: " + account);
        } catch (PasswordValidationException pve) {
            System.out.println(pve);
            return;
        }
        AccountStore.getInstance().addAccount(account);
        System.out.println("Account = " + account);
    }

    public static void changePassword(Map<String, String> commandOptions){
        UserAccount account = getUserAccount(commandOptions);
        if (account == null) {
            System.out.println("Account not found! Aborting!");
            return;
        }
        String password = commandOptions.getOrDefault("password", null);
        if (password == null) {
            System.out.println("No password specified for change. Aborting!");
            return;
        }
        PasswordPolicyValidator policyValidator = PasswordPolicyValidator.getDefaultPolicyValidator();
        try {
            AuthenticationProvider ap = new AuthenticationProviderImpl(policyValidator);
            ap.changePassword(account, password);
            System.out.println("The password was changed successfully.");
        } catch (PasswordValidationException pve) {
            System.out.println(pve);
            return;
        }
        AccountStore.getInstance().updateAccount(account);
    }


    public static void main( String[] args ){
        /*
            Create a really simple password policy for validation.
        */
        Map<String, Object> passwordPolicy = new HashMap<>();
        passwordPolicy.put("minLength", Integer.valueOf(8));
        PasswordPolicyValidator.setDefaultPolicy(passwordPolicy);

        /*
            Parse the arguments
        */
        String operation = args.length == 0 ? "--none" : args[0];
        Map<String, String> commandOptions = parseArguments(args);
        /*
            Execute the operation
        */
        switch (operation) {
        case "--add":
            addAccount(commandOptions);
            break;
        case "--changePassword":
            changePassword(commandOptions);
            break;
        case "--delete":
            deleteAccount(commandOptions);
            break;
        case "--disable":
            setEnabled(commandOptions, false);
            break;
        case "--enable":
            setEnabled(commandOptions, true);
            break;
        case "--listAccounts":
            listAccounts();
            break;
        case "--login":
            login(commandOptions);
            break;
        case "--query":
            query(commandOptions);
            break;
        default:
            usage();
            System.exit(2);
            break;
        }
    }
}
