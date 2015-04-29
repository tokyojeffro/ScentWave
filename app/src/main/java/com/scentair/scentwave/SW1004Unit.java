package com.scentair.scentwave;

import com.google.gson.annotations.Expose;

import java.util.*;

public class SW1004Unit {
    @Expose public String mitecSerial;
    @Expose public String scentairSerial;
    @Expose public Date created;
    // Do not include in the POST/INSERT for this record in the database
    // The unit test results will reference this number
    public Integer id;

    public SW1004Unit(String mitecSerial, String scentairSerial){
        this.mitecSerial = mitecSerial;
        this.scentairSerial = scentairSerial;
        this.created = new Date();
        this.id = 0;
    }
}