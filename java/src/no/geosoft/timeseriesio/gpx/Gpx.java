package no.geosoft.timeseriesio.gpx;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public final class Gpx
{
  private final Set<Track> tracks_ = new HashSet<>();

  public Gpx()
  {
  }

  public void addTrack(Track track)
  {
    tracks_.add(track);
  }

  public Set<Track> getTracks()
  {
    return Collections.unmodifiableSet(tracks_);
  }

  public String toStringh()
  {
    return "GPX";
  }
}
