package no.geosoft.timeseriesio.gpx;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Model the content of a GPX file.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Gpx
{
  /** The tracks of this GPX instance. */
  private final Set<GpxTrack> tracks_ = new HashSet<>();

  /**
   * Create a new empty GPX instance.
   */
  public Gpx()
  {
    // Nothing
  }

  /**
   * Add a track to this GPX instance.
   *
   * @param track  Track to add. Non-null.
   */
  public void addTrack(GpxTrack track)
  {
    if (track == null)
      throw new IllegalArgumentException("track cannot be null");

    tracks_.add(track);
  }

  /**
   * Return the tracks of this GPX instance.
   *
   * @return  The tracks of this GPX instance. Never null.
   */
  public Set<GpxTrack> getTracks()
  {
    return Collections.unmodifiableSet(tracks_);
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return "GPX";
  }
}
