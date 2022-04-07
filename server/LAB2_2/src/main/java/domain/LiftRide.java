package domain;

public class LiftRide {
    Integer resortId;
    Integer seasonId;
    Integer daysId;
    Integer skierId;
    Integer liftID;
    Integer time;
    Integer waitTime;

    public LiftRide(Integer resortId, Integer seasonId, Integer daysId, Integer skierId, Integer liftID, Integer time, Integer waitTime) {
        this.resortId = resortId;
        this.seasonId = seasonId;
        this.daysId = daysId;
        this.skierId = skierId;
        this.liftID = liftID;
        this.time = time;
        this.waitTime = waitTime;
    }

    public Integer getResortId() {
        return resortId;
    }

    public void setResortId(Integer resortId) {
        this.resortId = resortId;
    }

    public Integer getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Integer seasonId) {
        this.seasonId = seasonId;
    }

    public Integer getDaysId() {
        return daysId;
    }

    public void setDaysId(Integer daysId) {
        this.daysId = daysId;
    }

    public Integer getSkierId() {
        return skierId;
    }

    public void setSkierId(Integer skierId) {
        this.skierId = skierId;
    }

    public Integer getLiftID() {
        return liftID;
    }

    public void setLiftID(Integer liftID) {
        this.liftID = liftID;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }
}
