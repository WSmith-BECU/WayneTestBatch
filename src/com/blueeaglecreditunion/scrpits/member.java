package com.blueeaglecreditunion.scrpits;

import org.omg.CORBA.portable.ServantObject;

import java.sql.Date;
import com.corelationinc.script.Serial;

public class member {
    private String memberName;
    private String acctNum;
    private java.sql.Date DOB;
    private int age;
    private String address;
    private String email;
    private String phone;
    private java.sql.Date memberDate;
    private Serial personSerial;

    public member() {

    }

    public member(String memberName, String acctNum, Date DOB, int age, String address, String email, String phone, Date memberDate, Serial personSerial) {
        this.memberName = memberName;
        this.acctNum = acctNum;
        this.DOB = DOB;
        this.age = age;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.memberDate = memberDate;
        this.personSerial = personSerial;

    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getAcctNum() {
        return acctNum;
    }

    public void setAcctNum(String acctNum) {
        this.acctNum = acctNum;
    }

    public Date getDOB() {
        return DOB;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getMemberDate() {
        return memberDate;
    }

    public void setMemberDate(Date memberDate) {
        this.memberDate = memberDate;
    }

    public Serial getPersonSerial() {
        return personSerial;
    }

    public void setPersonSerial(Serial personSerial) {
        this.personSerial = personSerial;
    }
}
