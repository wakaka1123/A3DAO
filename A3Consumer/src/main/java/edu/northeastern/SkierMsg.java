package edu.northeastern;

public class SkierMsg {
  private int time;
  private int liftID;
  private int resortID;
  private int seasonID;
  private int dayID;
  private int skierID;

  public SkierMsg() {}
  public SkierMsg(int time, int liftID, int resortID, int seasonID, int dayID, int skierID) {
    this.time = time;
    this.liftID = liftID;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
  }

  public int getTime() {
    return time;
  }

  public int getLiftID() {
    return liftID;
  }

  public int getResortID() {
    return resortID;
  }

  public int getSeasonID() {
    return seasonID;
  }

  public int getDayID() {
    return dayID;
  }

  public int getSkierID() {
    return skierID;
  }
}
