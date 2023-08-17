package de.laurinhummel.SparkSRV;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class USRObject {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String name;

    @DatabaseField
    private int money;

    public USRObject() {

    }

    public long getId() { return id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public int getMoney() { return this.money; }
    public void setMoney(int money) { this.money = money; }
}
