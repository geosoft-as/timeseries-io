package no.geosoft.timeseriesio.gpx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Models a GPX track element.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class GpxTrack
{
  /** Name of this track. */
  private final String name_;

  /** Track type, such as "hiking". */
  private String type_;

  /** The points that makes up the track. */
  private final List<GpxTrackPoint> trackPoints_ = new ArrayList<>();

  /**
   * Create a new GPX track.
   *
   * @param name  Name of track. May be null.
   */
  public GpxTrack(String name)
  {
    name_ = name;
  }

  /**
   * Return name of this track.
   *
   * @return  Name of this track. Null if none provided.
   */
  public String getName()
  {
    return name_;
  }

  /**
   * Return type of this track.
   *
   * @return  Type of this track. May be null if none provided.
   */
  public String getType()
  {
    return type_;
  }

  /**
   * Set type of this track.
   *
   * @param type  Track type. May be null.
   */
  public void setType(String type)
  {
    type_ = type;
  }

  /**
   * Add the given track point to this track.
   *
   * @param trackPoint  Track point to add. Non-null.
   * @throws IllegalArgumentException  If trackPoint is null.
   */
  public void addTrackPoint(GpxTrackPoint trackPoint)
  {
    if (trackPoint == null)
      throw new IllegalArgumentException("trackPoint cannot be null");

    trackPoints_.add(trackPoint);
  }

  /**
   * Return the track points of this track.
   *
   * @return The track points of this track. Never null.
   */
  public List<GpxTrackPoint> getTrackPoints()
  {
    return Collections.unmodifiableList(trackPoints_);
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();

    String name = name_ != null ? name_ : "<NONAME>";
    s.append("Track: " + name + "\n");

    for (GpxTrackPoint point : trackPoints_)
      s.append(point.toString() + "\n");

    return s.toString();
  }
}
