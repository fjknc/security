package cn.anytec.security.model.parammodel;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimeModel {
    private long timestamp;
    private String catchTime;
    private Date date;
    private int dayOfWeek;
    private int hour;
    private int minute;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public TimeModel(LocalDateTime localDateTime){

        ZonedDateTime zonedDateTime= ZonedDateTime.of(localDateTime,ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault());
        long timestamp = zonedDateTime.toInstant().toEpochMilli();
        this.dayOfWeek = zonedDateTime.getDayOfWeek().getValue();
        this.hour = zonedDateTime.getHour();
        this.minute = zonedDateTime.getMinute();
        this.timestamp = timestamp;
        this.catchTime = format.format(timestamp);
        this.date = Date.from(zonedDateTime.toInstant());
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCatchTime() {
        return catchTime;
    }

    public void setCatchTime(String catchTime) {
        this.catchTime = catchTime;
    }


    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public SimpleDateFormat getFormat() {
        return format;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
