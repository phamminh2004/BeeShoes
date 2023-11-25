package fpoly.mds.beeshoes.model;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

public class Work {
    private String id;
    private String name;
    private int shift;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private int status;

    public Work() {
    }

    public Work(String id, String name, int shift, LocalTime timeStart, LocalTime timeEnd, int status) {
        this.id = id;
        this.name = name;
        this.shift = shift;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    public LocalTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalTime timeStart) {
        this.timeStart = timeStart;
    }

    public LocalTime getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(LocalTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public HashMap<String, Object> convertHashMap() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        HashMap<String, Object> work = new HashMap<>();
        work.put("id", id);
        work.put("name",name);
        work.put("shift", shift);
        work.put("timeStart", dtf.format(timeStart));
        work.put("timeEnd", dtf.format(timeEnd));
        work.put("status", status);
        return work;
    }
}
