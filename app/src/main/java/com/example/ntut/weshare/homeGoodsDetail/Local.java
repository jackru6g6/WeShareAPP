package com.example.ntut.weshare.homeGoodsDetail;


import java.io.Serializable;
import java.sql.Blob;
import java.sql.Timestamp;

public class Local implements Serializable {
    private int localNo;
    private String localName;

    public Local(int localNo, String localName) {
        this.localNo = localNo;
        this.localName = localName;
    }

    public int getLocalNo() {
        return localNo;
    }

    public void setLocalNo(int localNo) {
        this.localNo = localNo;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }
}