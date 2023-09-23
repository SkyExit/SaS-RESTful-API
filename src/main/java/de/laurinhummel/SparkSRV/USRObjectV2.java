package de.laurinhummel.SparkSRV;

//ICH HABE PLÄNE h
public class USRObjectV2 {
    private int id;
    private String validation;
    private String name;
    private int money;
    private int priority;

    public USRObjectV2(int id, String validation, String name, int money, int priority) {
        this.id = id;
        this.validation = validation;
        this.name = name;
        this.money = money;
        this.priority = priority;
    }

    public int getId() { return id; }
    public String getValidation() { return validation; }
    public String getName() { return name; }
    public int getMoney() { return money; }
    public int getPriority() { return priority; }

    public void setMoney(int newMoney) { money = newMoney; }
}
