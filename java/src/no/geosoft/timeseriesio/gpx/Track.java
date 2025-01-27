package no.geosoft.timeseriesio.gpx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Models a GPX track element.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Track
{
  private final String name_;

  private final List<TrackPoint> trackPoints_ = new ArrayList<>();

  public Track(String name)
  {
    name_ = name;
  }

  public void addTrackPoint(TrackPoint trackPoint)
  {
    trackPoints_.add(trackPoint);
  }

  public List<TrackPoint> getTrackPoints()
  {
    return Collections.unmodifiableList(trackPoints_);
  }
}
