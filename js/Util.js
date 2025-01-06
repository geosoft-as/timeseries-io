"use strict"

export class Util
{
  static getAsNumber(value)
  {
    // No-value
    if (value == null)
      return NaN;

    // Number
    if (typeof value == "number")
      return value;

    // Date
    if (value instanceof Date)
      return value.getTime();

    // Boolean
    if (typeof value == "boolean")
      return value ? 1.0 : 0.0;

    // String
    if (typeof value == "string") {
      if (value.length == 0)
        return NaN;

      return Number(value);
    }

    // Others
    return null;
  }

  static getNumberAsType(value, valueType)
  {
    if (valueType == null)
      throw new TypeError("valueType cannot be null");

    // NaN reports as null for all types
    if (isNaN(value))
      return null;

    // float
    if (valueType == "float")
      return value;

    // integer
    if (valueType == "integer")
      return Math.round(value);

    // datetime
    if (valueType == "datetime")
      return new Date(Math.round(value));

    // string
    if (valueType == "string")
      return "" + value;

    // boolean
    if (valueType == "boolean")
      return value != 0.0;

    return null;
  }

  static getAsType(value, valueType)
  {
    if (valueType == null)
      throw new TypeError("valueType cannot be null");

    if (value == null)
      return null;

    if (valueType == "string")
      return value.toString();

    if (valueType == "datetime" && typeof value == "string")
      return new Date(value);

    return this.getNumberAsType(this.getAsNumber(value), valueType);
  }
}


