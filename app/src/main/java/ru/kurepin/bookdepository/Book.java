package ru.kurepin.bookdepository;

import java.util.Date;
import java.util.UUID;

public class Book {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mReaded;

    public Book() {
        mId = UUID.randomUUID();
        mDate = new Date(); // текущая дата
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isReaded() {
        return mReaded;
    }

    public void setReaded(boolean readed) {
        mReaded = readed;
    }
}