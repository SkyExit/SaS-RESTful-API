package de.laurinhummel.SparkSRV.handler;

import de.laurinhummel.SparkSRV.USRObjectV2;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class TransactionHandler {
    static DB db;

    //Sets value of new users to zero
    public static void validateUserBalance(int id) {
        try { db = DBMaker.fileDB("data.db").checksumHeaderBypass().make(); }
        catch (DBException.FileLocked e) { }
        HTreeMap myMap = db.hashMap("economy").createOrOpen();
        try {
            myMap.get(id);
            System.out.println("user exists");
        } catch (NullPointerException e) {
            myMap.put(id, 1234);
            System.out.println("created new user");
        }
    }
}
