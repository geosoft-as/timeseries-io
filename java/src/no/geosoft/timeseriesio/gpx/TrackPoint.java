package no.geosoft.timeseriesio.gpx;

import java.util.Date;

public final class TrackPoint
{
  private final Date time_;

  private final Double latitude_;

  private final Double longitude_;

  private final Double elevation_;

  private Double heartRate_;

  private Double cadence_;

  public TrackPoint(Date time, Double latitude, Double longitude, Double elevation)
  {
    if (time == null)
      throw new IllegalArgumentException("time cannot be null");

    this.time_ = new Date(time.getTime());
    this.latitude_ = latitude;
    this.longitude_ = longitude;
    this.elevation_ = elevation;
  }

  public Date getTime()
  {
    return new Date(time_.getTime());
  }

  public Double getLatitude()
  {
    return latitude_;
  }

  public Double getLongitude()
  {
    return longitude_;
  }

  public Double getElevation()
  {
    return elevation_;
  }

  public void setHeartRate(double heartRate)
  {
    heartRate_ = heartRate;
  }

  public Double getHeartRate()
  {
    return heartRate_;
  }

  public void setCadence(double cadence)
  {
    cadence_ = cadence;
  }

  public Double getCadence()
  {
    return cadence_;
  }

  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("TrackPoint\n");
    s.append("  Time: " + time_ + "\n");
    s.append("  Position: " + latitude_ + "," + longitude_ + "\n");
    s.append("  Elevation: " + elevation_);
    return s.toString();
  }
}
