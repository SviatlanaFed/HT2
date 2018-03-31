package app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Number {
    private String id="";
    private String owner="";
    private String phoneNumber="";


    public Number(String id, String owner, String phoneNumber) {
        this.id = id;
        this.owner = owner;
        this.phoneNumber = phoneNumber;
    }

    // Конструктор для создания пустой записи о номере.
    public Number()
    {
        this.id = "0";
        this.owner = "";
        this.phoneNumber = "";
    }
    public Number(String owner, String phoneNumber) {
        this.id = "0";
        this.owner = owner;
        this.phoneNumber = phoneNumber;
    }

    public Number(String phoneNumber) {
        this.id = "0";
        this.owner = "";
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

       public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Валидация номера телефона.Для незаполненного поля номера телефона можно передать второй параетр == true,
    // тогда допускается пустое значение.
    public boolean validatePhoneNumber(String phoneNumber,boolean empty_allowed)   {
        if (empty_allowed) {
            Matcher matcher = Pattern.compile("[\\d-*+*#*]{0,50}").matcher(phoneNumber);
            return matcher.matches();
        }
        else {
            Matcher matcher = Pattern.compile("[\\d-*+*#*]{2,50}").matcher(phoneNumber);
            return matcher.matches();
        }

    }
}
