package com.blueeaglecreditunion.scrpits;

public class myPassword {

    private static String pass = "F33u-2na8";
    private static String username = "wsmith";
    private static String databaseName = "jdbc:db2://208.69.139.109:50000";

    /**
     * Default constructor
     */
    public myPassword() {

    }

    /**
     *  Default constructor
     *
     * @param password - This is your password
     * @param user - this is your user name
     * @param database - database name
     */
    public myPassword(String password, String user, String database){
        this.pass = password;
        this.username = user;
        this.databaseName = database;
    }

    /**
     * Gets the password of the user.
     *
     * @return users password
     */
    public static String getPassword() {
        return pass;
    }

    /**
     * Gets the username of the...user
     * @return
     */
    public static String getUsername() {
        return username;
    }

    /**
     *  Gets the database connection name
     * @return - database name
     */
    public static String getDatabaseName() {
        return databaseName;
    }
}