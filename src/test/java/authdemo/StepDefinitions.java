package authdemo;
import java.io.File;

import com.mhsoftware.authdemo.*;

import java.util.HashMap;
import java.util.Map;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    private boolean result;

    private PasswordPolicyValidator getDefaultPolicy() {
        Map<String, Object> passwordPolicy = new HashMap<>();
        passwordPolicy.put("minLength", Integer.valueOf(8));
        PasswordPolicyValidator.setDefaultPolicy(passwordPolicy);
        return PasswordPolicyValidator.getDefaultPolicyValidator();
    }

    @When("I create a user named {string} with password {string}")
    public void createAUser(String userName, String password) throws PasswordValidationException {
        /* 
           Set the default password policy
       */

        /*
        Create the account
        */
        UserAccount account = AccountStore.getInstance().getAccount(userName);
        assertNull(account, "TODO: Remove teh account-info.yaml file first!");
        account = new UserAccount();
        account.userName = userName;
        account.enabled = true;
        AuthenticationProvider ap = new AuthenticationProviderImpl(getDefaultPolicy());
        ap.changePassword(account, password);
        AccountStore.getInstance().addAccount(account);

    }

    @And("I reset the credential cache")
    public void resetCache() {
        AccountStore.getInstance().resetStore();
    }

    @When("I verify the password {string} for user {string}")
    public void verifyPassword(String password, String userName) {
        UserAccount account = AccountStore.getInstance().getAccount(userName);
        assertNotNull(account);
        AuthenticationProvider ap = new AuthenticationProviderImpl(getDefaultPolicy());
        try {
            ap.verifyLoginPreconditions(account);
            result = ap.verifyPassword(account, password);
            ap.postLogin(account, result);
        } catch (AuthenticationException ae) {
            // ignore
        }
    }

    @Then("the result will be {string}")
    public void checkResult(String expected) {
        assertTrue(result == Boolean.valueOf(expected).booleanValue(), "Assertion failure. Expected Result: " + expected + " Actual Result: " + result);
    }

    @And("the bad login count for user {string} will be {string}.")
    public void checkBadLoginCount(String userName, String value) {
        UserAccount account = AccountStore.getInstance().getAccount(userName);
        assertNotNull(account);
        switch (value) {
        case "zero":
            assertEquals(account.badLoginCount, 0);
            break;
        case "non-zero":
            assertTrue(account.badLoginCount != 0);
            break;
        }
    }

    @Given("user {string} exists in the credential system.")
    public void confirmUserPresent(String userName) {
        UserAccount account = AccountStore.getInstance().getAccount(userName);
        assertNotNull(account);
    }

    @When("I change the password for user {string} to {string}")
    public void changePassword(String userName, String password) throws AuthenticationException {
        UserAccount account = AccountStore.getInstance().getAccount(userName);
        assertNotNull(account);
        AuthenticationProvider ap = new AuthenticationProviderImpl(getDefaultPolicy());
        ap.verifyLoginPreconditions(account);
        try {
            ap.changePassword(account, password);
            result = true;
        } catch (PasswordValidationException pve) {
            result = false;
        }
        AccountStore.getInstance().updateAccount(account);
    }

    @When("I delete the user {string}")
    public void deleteAccount(String userName) {
        AccountStore as = AccountStore.getInstance();
        UserAccount account = as.getAccount(userName);
        assertNotNull(account);
        assertTrue(as.deleteAccount(account.userID));
    }

    @Then("the user {string} should not be found in the credential system.")
    public void checkUserDeleted(String userName) {
        UserAccount account = AccountStore.getInstance().getAccount(userName);
        assertNull(account);
    }

    @Given("the store file was deleted")
    public void deleteStoreFile() {
        File f = new File(AccountStore.STORE_FILE);
        if (f.exists()) {
            result = f.delete();
        } else {
            result = true;
        }
    }
}

