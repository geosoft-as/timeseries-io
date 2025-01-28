package no.geosoft.timeseriesio.gpx;

import java.util.Date;

/**
 * A track point (with known extensions) as defined by GPX.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class GpxTrackPoint
{
  /** Time of this track point. Null if not provided. */
  private final Date time_;

  /** Latitude of this track point. Null if not provided. */
  private final Double latitude_;

  /** Longitude of this track point. Null if not provided. */
  private final Double longitude_;

  /** Elevation of this track point. Null if not provided. */
  private final Double elevation_;

  /** GARMIN extension: Heart rate of this track point. Null if not provided. */
  private Double heartRate_;

  /** GARMIN extension: Cadence of this track point. Null if not provided. */
  private Double cadence_;

  /**
   * Create a new GPX track point.
   *
   * @param time       Time of this track point. Null if not provided.
   * @param latitude   Latitude of this track point. Null if not provided.
   * @param longitude  Longitude of this track point. Null if not provided.
   * @param elevation  Elevation of this track point. Null if not provided.
   */
  public GpxTrackPoint(Date time, Double latitude, Double longitude, Double elevation)
  {
    this.time_ = new Date(time.getTime());
    this.latitude_ = latitude;
    this.longitude_ = longitude;
    this.elevation_ = elevation;
  }

  /**
   * Return time of this track point.
   *
   * @return  Time of this track point. Null if not provided.
   */
  public Date getTime()
  {
    return new Date(time_.getTime());
  }

  /**
   * Return latitude of this track point.
   *
   * @return  Latitude of this track point. Null if not provided.
   */
  public Double getLatitude()
  {
    return latitude_;
  }

  /**
   * Return longitude of this track point.
   *
   * @return  Longitude of this track point. Null if not provided.
   */
  public Double getLongitude()
  {
    return longitude_;
  }

  /**
   * Return elevation of this track point.
   *
   * @return  Elevation of this track point. Null if not provided.
   */
  public Double getElevation()
  {
    return elevation_;
  }

  /**
   * Set heart rate of this track point.
   *
   * @param heartRate  Heart rate to set. [0.0,&gt;. May be null for unknown or N/A.
   */
  public void setHeartRate(Double heartRate)
  {
    if (heartRate != null && heartRate < 0.0)
      throw new IllegalArgumentException("Invalid heartRate: " + heartRate);

    heartRate_ = heartRate;
  }

  /**
   * Return the GARMIN extension heart rate of this track point.
   *
   * @return  Heart rate of this track point. Null if not provided.
   */
  public Double getHeartRate()
  {
    return heartRate_;
  }

  /**
   * Set cadence of this point.
   *
   * @param cadence  Cadence to set. [0.0,&gt;. May be null for unknown or N/A.
   */
  public void setCadence(Double cadence)
  {
    if (cadence != null && cadence < 0.0)
      throw new IllegalArgumentException("Invalid cadence: " + cadence);

    cadence_ = cadence;
  }

  /**
   * Return the GARMIN extension cadence of this track point.
   *
   * @return  Cadence of this track point. Null if not provided.
   */
  public Double getCadence()
  {
    return cadence_;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("GPX TrackPoint\n");
    s.append("  Time........: " + time_ + "\n");
    s.append("  Position....: " + latitude_ + "," + longitude_ + "\n");
    s.append("  Elevation...: " + elevation_ + "\n");
    s.append("  Heart rate..: " + heartRate_ + "\n");
    s.append("  Cadence.....: " + cadence_ + "\n");
    return s.toString();
  }
}
