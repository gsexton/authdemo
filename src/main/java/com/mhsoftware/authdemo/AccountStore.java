package com.mhsoftware.authdemo;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.Constructor;


/**
 * A class for storing user accounts and retrieving them. 
 *  
 * For simplicity, and ease of inspection, it uses a yaml file. This 
 * would have problems in the real world with concurrency while 
 * re-writing the whole file, performance of lookups, etc. I'm aware of 
 * these things, I'm ignoring them purposely. 
 *  
 * In numerous places, I'm iterating over the list for simplicity rather 
 * than using a map to speed up access. I know this is O(N)
 * but time constraints... 
 *  
 * In a production system, I would use a database of some sort to hold 
 * account information and lookup performance would be more or less constant. 
 *  
 * The structure of the file is: 
 *  
 * nextUserID: int 
 * Users: 
 * - UserAccount 
 * - UserAccount 
 * - ... 
 *  
 * @author gsexton (12/10/21)
 */
public class AccountStore {

    public static final String STORE_FILE = "account-info.yaml";
    /**
     * A class to act as a memory representation of our credential file.
     */
    public static class Store {
        /** The identity value for the user id. */
        public int nextUserID;
        /** The list of users in the credential file. */
        public List<UserAccount> users;

        public Store() {
            super();
        }
    }

    private static AccountStore instance;

    private Store m_store;

    static {
        // Initialize the singleton.
        instance = new AccountStore();
    }

    private AccountStore() {
    }

    public static AccountStore getInstance() {
        return instance;
    }

    public UserAccount getAccount(final String userName) {
        synchronized (this) {
            Store store = readStore();
            for (UserAccount u: store.users) {
                if (u.userName.equals(userName)) {
                    return u;
                }
            }
        }
        return null;
    }

    public UserAccount getAccount(final int userID) {
        synchronized (this) {
            Store store = readStore();
            for (UserAccount u: store.users) {
                if (u.userID == userID) {
                    return u;
                }
            }
        }
        return null;
    }

    public boolean deleteAccount(final int userID) {
        synchronized (this) {
            Store store = readStore();
            for (int i = 0; i < store.users.size(); i++) {
                if (store.users.get(i).userID == userID) {
                    store.users.remove(i);
                    serializeStore();
                    return true;
                }
            }
        }
        return false;
    }

    public void addAccount(UserAccount user) {
        synchronized (this) {
            Store store = readStore();
            if (user.userID == 0) {
                user.userID = store.nextUserID;
                store.nextUserID = store.nextUserID + 1;
                store.users.add(user);
                serializeStore();
            } else {
                updateAccount(user);
            }
        }
    }

    public void updateAccount(UserAccount user) {
        synchronized (this) {
            Store store = readStore();
            for (int i = 0; i < store.users.size(); i++) {
                if (store.users.get(i).userID == user.userID) {
                    store.users.set(i, user);
                    serializeStore();
                    return;
                }
            }
            throw new RuntimeException("The specified account: " + user.userID + " was not found in the datastore for update!");
        }
    }

    public Collection<UserAccount> getAccounts() {
        List<UserAccount> lst;
        synchronized (this) {
            Store store = readStore();
            if (store.users == null) {
                store.users = new ArrayList<>();
            }
            lst = Collections.unmodifiableList(store.users);
        }
        return lst;
    }

    public void resetStore() {
        synchronized (this) {
            m_store = null;
        }
    }

    /**
     * This method reads the contents of the account-info.yaml file into a store 
     * object.
     * 
     * @return Store 
     */
    private Store readStore() {
        synchronized (this) {
            if (this.m_store != null) {
                return this.m_store;
            }

            File f = new File(STORE_FILE);

            if (f.exists()) {
                Constructor constructor = new Constructor(Store.class);
                TypeDescription customTypeDescription = new TypeDescription(Store.class);
                customTypeDescription.addPropertyParameters("users", UserAccount.class);
                constructor.addTypeDescription(customTypeDescription);
                Yaml yaml = new Yaml(constructor);
                try (InputStream  is = new FileInputStream(f)) {
                    m_store = yaml.load(is);
                } catch (IOException ioe) {
                    System.out.println(ioe.toString());
                }
            } else {
                m_store = new Store();
                m_store.users = new ArrayList<>();
            }
            return m_store;
        }
    }

    /**
     * This method serializes the store object to the yaml file.
     * 
     * @author gsexton (12/14/21)
     */
    private void serializeStore() {
        synchronized (this) {
            Yaml yaml = new Yaml();
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(STORE_FILE))) {
                yaml.dump(readStore(), osw);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }
}

