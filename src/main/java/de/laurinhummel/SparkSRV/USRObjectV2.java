package de.laurinhummel.SparkSRV;

//ICH HABE PLÄNE h
public class USRObjectV2 {
    private int id;
    private String validation;
    private int money;
    private int priority;

    public USRObjectV2(int id, String validation, int money, int priority) {
        this.id = id;
        this.validation = validation;
        this.money = money;
        this.priority = priority;
    }

    public int getId() { return id; }
    public String getValidation() { return validation; }
    public int getMoney() { return money; }
    public int getPriority() { return priority; }

    public void setMoney(int newMoney) { money = newMoney; }
}
