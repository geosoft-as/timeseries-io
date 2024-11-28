package no.geosoft.jtimeseries.util;

/**
 * Class for holding a space indentation commonly used at beginning
 * of pretty printed lines.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Indentation
{
  /** Pre-created strings of spaces of a given length. */
  private final static String[] SPACES = new String[100];

  /** Current indentation level. [0,&gt;. */
  private final int level_;

  /** Indentation unit. Number of characters for the indentation. [0,&gt;. */
  private final int unit_;

  /** The actual indentation unit string. Cached for speed. */
  private final String indent_;

  /**
   * Initialize static members of this class.
   */
  static {
    StringBuilder s = new StringBuilder("");
    for (int i = 0; i < SPACES.length; i++) {
      SPACES[i] = s.toString();
      s.append(' ');
    }
  }

  /**
   * Create an indentation instance of the specified unit,
   * and initial indentation.
   *
   * @param unit   Number of characters per indentation. [0,&gt;.
   * @param level  Current indentation level. [0,&gt;.
   * @throws IllegalArgumentException  If unit or level is invalid.
   */
  public Indentation(int unit, int level)
  {
    if (unit < 0)
      throw new IllegalArgumentException("Invalid unit: " + unit);

    if (level < 0)
      throw new IllegalArgumentException("Invalid level: " + level);

    unit_ = unit;
    level_ = level;
    indent_ = getSpaces(level_ * unit_);
  }

  /**
   * Return a string containing the specified number of
   * space characters.
   *
   * @param n  Length of string to create.
   * @return   Requested string. If n is less than or equal to
   *           0 an empty string is returned. Never null.
   */
  private static String getSpaces(int n)
  {
    // TODO: Not sure why we allow n < 0 here
    if (n <= 0)
      return "";

    if (n < SPACES.length)
      return SPACES[n];

    // This slightly faster than using String.format().
    // Combined with the caching it is a lot faster for typical strings.
    StringBuilder s = new StringBuilder("");
    for (int i = 0; i < n; i++)
      s.append(' ');

    return s.toString();
  }

  /**
   * Create an indentation instance of the specified unit,
   *
   * @param unit  Number of characters per indentation. [0,&gt;.
   */
  public Indentation(int unit)
  {
    this(unit, 0);
  }

  /**
   * Create a new indentation instance indented one level to the right.
   *
   * @return  The requested indentation instance. Never null.
   */
  public Indentation push()
  {
    return new Indentation(unit_, level_ + 1);
  }

  /**
   * Create a new indentation instance indented one level to the left.
   *
   * @return  The requested indentation instance. Never null.
   * @throws IllegalStateException  If already at lowest level.
   */
  public Indentation pop()
  {
    if (level_ == 0)
      throw new IllegalStateException("Already at lowest level");

    return new Indentation(unit_, level_ - 1);
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return indent_;
  }
}
