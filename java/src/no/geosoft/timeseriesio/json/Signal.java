package no.geosoft.timeseriesio.json;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import no.geosoft.timeseriesio.util.DataArray;
import no.geosoft.timeseriesio.util.Util;
import no.geosoft.timeseriesio.util.Statistics;

/**
 * Model a time series signal as it is defined by the TimeSeries.JSON format.
 * <p>
 * A signal consist of measurement data of a specific type.
 * The signal may have one or more dimensions.
 *
 * @author <a href="mailto:info@petroware.no">Petroware AS</a>
 */
public final class Signal
{
  /** Name of this signal. Never null. */
  private final String name_;

  /** Description of signal. May be null if not provided. */
  private final String description_;

  /** Quantity of the signal data. Null if unknown or N/A. */
  private final String quantity_;

  /** Unit of this signal. May be null indicating unitless. */
  private final String unit_;

  /** Value type of the data of this signal. */
  private final Class<?> valueType_;

  /** Number of "dimensions" or directions that have been measured. [1,&gt;. */
  private final int nDimensions_;

  /** The signal values. Array of nDimensions. */
  private final DataArray[] values_;

  /** Signal statistics. */
  private final Statistics statistics_ = new Statistics();

  /**
   * Signal range. Array of two: smallest and largest value across dimensions.
   * Null means it needs to be recomputed.
   */
  private Object[] range_ = null;

  /** The binary size of each element in the signal. Used with binary storage only. */
  private volatile int size_;

  /**
   * Create a TimeSeries.JSON signal instance.
   *
   * @param name         Name (mnemonic) of signal. Non-null.
   * @param description  Signal long name or description. May be null if not provided.
   * @param quantity     Quantity of the signal data. Null if unknown or N/A.
   * @param unit         Unit of measure for the signal data. Null if unitless.
   * @param valueType    Value type of signal data.
   * @param nDimensions  Dimension of signal. &lt;0,&gt;.
   * @throws IllegalArgumentException  If name or valueType is null, or nDimensions is out of bounds.
   */
  public Signal(String name, String description, String quantity, String unit,
                Class<?> valueType, int nDimensions)
  {
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    if (valueType == null)
      throw new IllegalArgumentException("valueType cannot be null");

    if (nDimensions <= 0)
      throw new IllegalArgumentException("Invalid nDimensions: " + nDimensions);

    name_ = name;
    description_ = description;
    quantity_ = quantity;
    unit_ = unit;
    valueType_ = valueType;
    nDimensions_ = nDimensions;

    values_ = new DataArray[nDimensions_];
    for (int i = 0; i < nDimensions_; i++)
      values_[i] = new DataArray(valueType_);

    if (valueType_ == Double.class || valueType_ == Long.class)
      size_ = 8;
    else if (valueType_ == Date.class)
      size_ = 30;
    else if (valueType_ == Boolean.class)
      size_ = 1;
    else
      size_ = 0;
  }

  /**
   * Create a new TimeSeriers.JSON signal as a copy of the specified one.
   *
   * @param signal         Signal to copy. Non-null.
   * @param includeValues  True to include signal values, false to not.
   * @throws IllegalArgumentException  If signal is null.
   */
  public Signal(Signal signal, boolean includeValues)
  {
    this(signal.getName(),
         signal.getDescription(),
         signal.getQuantity(),
         signal.getUnit(),
         signal.getValueType(),
         signal.getNDimensions());

    // Size defaults to 0 for string curves so we set it explicitly here
    size_ = signal.size_;

    if (includeValues) {
      for (int index = 0; index < signal.getNValues(); index++) {
        for (int dimension = 0; dimension < signal.getNDimensions(); dimension++)
          addValue(dimension, signal.getValue(dimension, index));
      }
    }
  }

  /**
   * Return name of this signal.
   *
   * @return  Name of this signal. Never null.
   */
  public String getName()
  {
    return name_;
  }

  /**
   * Return quantity of the data of this signal.
   *
   * @return  Quantity of the signal data. Null if unitless.
   */
  public String getQuantity()
  {
    return quantity_;
  }


  /**
   * Return unit of measure of the data of this signal.
   *
   * @return  Unit of measure of the signal data. Null if unitless.
   */
  public String getUnit()
  {
    return unit_;
  }

  /**
   * Return description of this signal.
   *
   * @return  Description of this signal. Null if not provided.
   */
  public String getDescription()
  {
    return description_;
  }

  /**
   * Return the value type for the data of this signal,
   * typically Double.class, Integer.class, String.class, etc.
   *
   * @return Value type for the data of this signal. Never null.
   */
  public Class<?> getValueType()
  {
    return valueType_;
  }

  /**
   * Return the number of dimensions of this signal.
   *
   * @return  Number of dimensions of this signal. [1,&gt;.
   */
  public int getNDimensions()
  {
    return nDimensions_;
  }

  /**
   * Return the size (number of bytes) of the values of this signal when stored
   * in binary format.
   * <p>
   * <b>Note:</b> For string values the <em>number of bytes</em> needed to
   * represent the longest text UTF-8 string of the signal may differ from the
   * number of <em>characters</em> in the string, as each UTF-8 character may
   * take up 1, 2 or 3 bytes.
   *
   * @return Size of a binary representation of the values of this signal. [0,&gt;.
   */
  public int getSize()
  {
    return size_;
  }

  /**
   * Specify the (maximum) size (number of bytes) of string values of this signal.
   * <p>
   * <b>Note:</b> For string values the <em>number of bytes</em> needed to
   * represent the longest text UTF-8 string of the signal may differ from the
   * number of <em>characters</em> in the string, as each UTF-8 character may
   * take up 1, 2 or 3 bytes.
   *
   * @param size  Size to set. [0,&gt;.
   * @throws IllegalArgumentException  If size &lt; 0;
   * @throws IllegalStateException  If the method is being called for other than
   *                                string signals.
   */
  public void setSize(int size)
  {
    if (size < 0)
      throw new IllegalArgumentException("Invalid size: " + size);

    if (valueType_ != String.class)
      throw new IllegalStateException("Method can only be called for string signals");

    size_ = size;
  }

  /**
   * Add a value to this signal.
   *
   * @param dimension  Dimension index. [0,nDimensions&gt;.
   * @param value      Value to add. Null to indicate absent.
   * @throws IllegalArgumentException  If dimension is out of bounds.
   */
  public void addValue(int dimension, Object value)
  {
    if (dimension < 0 || dimension >= nDimensions_)
      throw new IllegalArgumentException("Invalid dimension: " + dimension);

    values_[dimension].add(Util.getAsType(value, valueType_));

    // Update size if this is a string signal
    if (valueType_ == String.class && value != null) {
      String s = value.toString();
      int size = s.getBytes(StandardCharsets.UTF_8).length;
      if (size > size_)
        size_ = size;
    }

    range_ = null;
  }

  /**
   * Add a value to this signal. If this is a multi-dimensional signal,
   * the value is added to the first dimension. This is a convenience method
   * for single dimensional signals.
   *
   * @param value  Value to add. Null indicates absent.
   */
  public void addValue(Object value)
  {
    addValue(0, value);
  }

  /**
   * Set a specific value in this signal.
   *
   * @param index      Index of signal to set. [0&gt;. If index is beyond current maximum,
   *                   the signal is padded with nulls.
   * @param dimension  Dimension entry to set. [0,nDimensions&gt;.
   * @param value      Value to set. Null for absent.
   */
  public void setValue(int index, int dimension, Object value)
  {
    if (index < 0)
      throw new IllegalArgumentException("Invalid index: " + index);

    if (dimension < 0 || dimension >= nDimensions_)
      throw new IllegalArgumentException("Invalid dimension: " + dimension);

    // Pad with nulls if necessary
    for (int i = getNValues() - 1; i < index; i++) {
      for (int dim = 0; dim < getNDimensions(); dim++) {
        addValue(dim, null);
      }
    }

    values_[dimension].set(index, Util.getAsType(value, valueType_));

    // Update size if this is a string signal
    if (valueType_ == String.class && value != null) {
      String s = value.toString();
      int size = s.getBytes(StandardCharsets.UTF_8).length;
      if (size > size_)
        size_ = size;
    }

    range_ = null;
  }

  /**
   * Set a specific value in this signal.
   * This is a convenience shorthand for setValue(index, 0, value) where the client
   * knows the signal is single dimensional.
   *
   * @param index  Index of value to set. [0,&gt;. If index is beyond current maximum,
   *               the signal is padded with nulls.
   * @param value  Value to set. Null for absent.
   */
  public void setValue(int index, Object value)
  {
    setValue(index, 0, value);
  }

  /**
   * Return the number of values in this signal.
   *
   * @return  Number of values in this signal. [0,&gt;.
   */
  public int getNValues()
  {
    return values_[0].size();
  }

  /**
   * Return the number of values in the specified dimension.
   *
   * @param dimension  Dimension to check. [0,nDimension&gt;.
   * @return           Number of values in the specified dimension. [0,&gt;.
   */
  int getNValues(int dimension)
  {
    assert dimension >= 0 && dimension < nDimensions_ : "Invalid dimenion: " + dimension;
    return values_[dimension].size();
  }

  /**
   * Return a specific value from the given dimension of this signal.
   *
   * @param index      Position index. [0,nValues&gt;.
   * @param dimension  Dimension index. [0,nDimensions&gt;.
   * @return           The requested value. Null if absent.
   */
  public Object getValue(int index, int dimension)
  {
    // Skip argument checking for performance reasons.

    return values_[dimension].get(index);
  }

  /**
   * Return s specific value from this signal. If the signal has multiple dimensions, the value
   * from the first dimension is returned. This is a convenience shorthand for getValue(index, 0)
   * when the client knows that the signal is one dimensional.
   *
   * @param index  Index to return value from. [0,nValues&gt;.
   * @return       The requested value. Null if absent.
   */
  public Object getValue(int index)
  {
    // Skip argument checking for performance reasons.

    return getValue(index, 0);
  }

  /**
   * Return the range (i.e.&nbsp;the min and max value) of this signal.
   * The returned array is never null. The two entries may
   * be null if min/max does not exist.
   * <p>
   * If the signal is multi-dimensional, the range is reported
   * across all dimensions.
   *
   * @return  The range of this signal as an array of two (min/max).
   *          Never null. The entries may be null if no range exists.
   */
  public Object[] getRange()
  {
    if (range_ == null) {
      double minValue = Double.NaN;
      double maxValue = Double.NaN;

      int nValues = getNValues();
      for (int dimension = 0; dimension < nDimensions_; dimension++) {
        for (int index = 0; index < nValues; index++) {
          double v = Util.getAsDouble(getValue(index, dimension));
          if (Double.isNaN(minValue) || v < minValue)
            minValue = v;
          if (Double.isNaN(maxValue) || v > maxValue)
            maxValue = v;
        }
      }

      range_ = new Object[2];
      range_[0] = Util.getAsType(minValue, valueType_);
      range_[1] = Util.getAsType(maxValue, valueType_);
    }

    return new Object[] {range_[0], range_[1]};
  }

  /**
   * Return signal statistics. Statistics is available even if log
   * data has not been stored.
   *
   * @return  Signal statistics. Never null.
   */
  public Statistics getStatistics()
  {
    return statistics_;
  }

  /**
   * Remove all values from this signal.
   */
  public void clear()
  {
    for (int dimension = 0; dimension < values_.length; dimension++)
      values_[dimension].clear();
    statistics_.reset();

    if (valueType_ == String.class)
      size_ = 0;

    range_ = null;
  }

  /**
   * Trim all signal lists to their actual dimension to save memory.
   * The assumption is that the lists are complete and will not grow
   * any further.
   */
  void trim()
  {
    for (int i = 0; i < nDimensions_; i++)
      values_[i].trim();
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("Name..........: " + name_ + "\n");
    s.append("Unit..........: " + unit_ + "\n");
    s.append("Description...: " + description_ + "\n");
    s.append("Value type....: " + valueType_ + "\n");
    s.append("N dimensions..: " + nDimensions_ + "\n");
    s.append("N values......: " + getNValues() + "\n");
    s.append("Range.........: " + getRange()[0] + " - " + getRange()[1] + "\n");
    s.append("Values........: " + "\n");
    for (int index = 0; index < getNValues(); index++) {
      for (int dimension = 0; dimension < getNDimensions(); dimension++)
        s.append(getValue(index, dimension) + ", ");
      s.append("\n");
    }
    return s.toString();
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    Signal s1 = new Signal("test", null, null, null, Double.class, 4);
    s1.setValue(10, 2, null);
    s1.setValue(2, 1, null);

    Signal s2 = new Signal("test2", null, null, null, Double.class, 1);
    s2.addValue(100.0);

    TimeSeriesJson t = new TimeSeriesJson();
    t.addSignal(s1);
    t.addSignal(s2);

    System.out.println(t);
  }
}
