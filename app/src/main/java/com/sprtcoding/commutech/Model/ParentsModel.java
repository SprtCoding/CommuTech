package com.sprtcoding.commutech.Model;

public class ParentsModel {
    String USER_ID, ADDRESS, CONTACT_NUMBER, EMAIL_ID, NAME;

    public ParentsModel() {
    }

    public ParentsModel(String UID, String address, String contactNo, String email, String fullname) {
        this.USER_ID = UID;
        this.ADDRESS = address;
        this.CONTACT_NUMBER = contactNo;
        this.EMAIL_ID = email;
        this.NAME = fullname;
    }

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public void setADDRESS(String ADDRESS) {
        this.ADDRESS = ADDRESS;
    }

    public String getCONTACT_NUMBER() {
        return CONTACT_NUMBER;
    }

    public void setCONTACT_NUMBER(String CONTACT_NUMBER) {
        this.CONTACT_NUMBER = CONTACT_NUMBER;
    }

    public String getEMAIL_ID() {
        return EMAIL_ID;
    }

    public void setEMAIL_ID(String EMAIL_ID) {
        this.EMAIL_ID = EMAIL_ID;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }
}
