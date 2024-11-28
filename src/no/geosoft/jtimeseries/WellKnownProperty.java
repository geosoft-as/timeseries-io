package no.geosoft.jtimeseries;

/**
 * List the well known properties of the TimeSeries.JSON format.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public enum WellKnownProperty
{
  /** Time series name. */
  NAME("name"),

  /** Time series description. */
  DESCRIPTION("description"),

  /** Time series source. */
  SOURCE("source"),

  /** Time series organization. */
  ORGANIZATION("wellbore"),

  /** License information. */
  LICENSE("license"),

  /** Time series location. */
  LOCATION("location"),

  /** Start time. */
  TIME_START("timeStart"),

  /** End time. */
  TIME_END("timeEnd"),

  /** Step if regular sampling. */
  TIME_STEP("timeStep"),

  /** Pointer to data source in case this is kept separate. */
  DATA_URI("dataUri");

  /** Key used when the property is written to file. Non-null. */
  private final String key_;

  /**
   * Create a well known property entry.
   *
   * @param key  Key as when written to file. Non-null.
   */
  private WellKnownProperty(String key)
  {
    assert key != null : "key cannot be null";
    key_ = key;
  }

  /**
   * Return key of this property.
   *
   * @return Key of this property. Never null.
   */
  public String getKey()
  {
    return key_;
  }

  /**
   * Get property for the specified key.
   *
   * @param key  Key to get property of. Non-null.
   * @return     The associated property, or null if not found.
   * @throws IllegalArgumentException  If key is null.
   */
  public static WellKnownProperty getByKey(String key)
  {
    if (key == null)
      throw new IllegalArgumentException("key cannot be null");

    for (WellKnownProperty property : WellKnownProperty.values()) {
      if (property.getKey().equals(key))
        return property;
    }

    // Not found
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return key_;
  }
}
