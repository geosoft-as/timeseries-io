# TimeSeries.JSON

The TimeSeries.JSON format is a modern time series format designed for the future requirements
of simplicity, compatibility, speed, massive storage, massive transmission, cloud computing
and big data analytics.

* Based on the _JavaScript Object Notation_ [JSON](https://www.json.org) open standard ([RFC 8259](https://tools.ietf.org/html/rfc8259) and [RFC 7493](https://tools.ietf.org/html/rfc7493))
* Non-proprietary
* Text-based, lightweight and human readable
* Full [UTF-8](https://en.wikipedia.org/wiki/UTF-8) support according to the JSON standard
* Built-in _no-value_ support
* Simple syntax consisting of collections of name/value pairs (_objects_) and ordered lists of values (_arrays_)</li>
* Compact type system
* Quantity and unit support based on established standards
* Date and time support through the [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) standard
* Supports single value and multi-dimensional signals
* Fast: The simple syntax and streaming nature makes parsing extremely efficient
* Omnipresent parsers and generators for just about [any](http://json.org) system environment available
* Existing ecosystem of [NoSQL](https://en.wikipedia.org/wiki/NoSQL) cluster database support with high volume storage, search and indexing, distribution, scalability and high performance analytics</li>



## Example

```json
[
  {
    "header": {
      "TimeSeries.JSON", "1.0",
      "name": "New York City Marathon",
      "description": "R. Chepngetich",
      "source": "Garmin Fenix 8",
      "organization": "Runners World",
      "license": "Creative Commons BY-NC",
      "location": [40.785091, -73.968285],
      "timeStart": "2024-12-02T00:00:00.000Z",
      "timeEnd": "2024-12-02T02:09:56.000Z",
      "timeStep": null
    },
    "signals": [
      {
        "name": "time",
        "description": null,
        "quantity": "time",
        "unit": "s",
        "valueType": "datetime",
        "dimensions": 1
      },
      {
        "name": "latitude",
        "description": null,
        "quantity": "angle",
        "unit": "degA",
        "valueType": "float",
        "dimensions": 1
      },
      {
        "name": "longitude",
        "description": null,
        "quantity": "angle",
        "unit": "degA",
        "valueType": "float",
        "dimensions": 1
      },
      {
        "name": "elevation",
        "description": null,
        "quantity": "length",
        "unit": "m",
        "valueType": "float",
        "dimensions": 1
      }
    ],
    "data": [
      ["2024-12-02T00:00:00.000Z", 40.60305, -74.0556192, 27],
      ["2024-12-02T00:03:07.000Z", 40.60490, -74.0499759, -2],
      ["2024-12-02T00:07:04.000Z", 40.60721, -74.0428090, -2],
      ["2024-12-02T00:11:18.000Z", 40.60965, -74.0351057,  4],
      ["2024-12-02T00:12:44.000Z", 40.61053, -74.0325308, 16],
      ["2024-12-02T00:13:51.000Z", 40.61162, -74.0308571, 11],
      ["2024-12-02T00:14:53.000Z", 40.61294, -74.0298057, 18],
      ["2024-12-02T00:16:15.000Z", 40.61452, -74.0280890, 16],
      ["2024-12-02T00:17:43.000Z", 40.61627, -74.0263295, 12],
      ["2024-12-02T00:19:22.000Z", 40.61771, -74.0289688, 24]
    ]
 }
]
```



## Data types

The TimeSeries.JSON format defines the following data types for header and signal data:

| Type     | Description                    | Examples                                    |
|----------|--------------------------------|---------------------------------------------|
| float    | Floating point decimal numbers | 10.2, 0.014, 3.1e-108, 2.13e12, 0.0, null   |
| integer  | Integer decimal numbers        | 10, 42, 1000038233, -501, null              |
| string   | Text strings                   | "error", "stopped", "message 402", "", null |
| boolean  | Logic states                   | true, false, null                           |
| datetime | Date/time specifications according to [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) | "2020-12-19", "2023-02-18T16:23:48,3-06:00", null |

Numbers must contain values corresponding to a double-precision 64-bit
[IEEE 754](https://en.wikipedia.org/wiki/IEEE_754) binary format value.
Integer values has the same internal representation in JavaScript as floats and should be
limited to 52 bits (+/-9007199254740991) to ensure accuracy.

Also, numeric values that cannot be represented as sequences of digits (such as _Infinity_
and _NaN_) must be avoided. Use `null` instead.



## Header

The time series header contains metadata that describes the overall measurement operation and
consists of any JSON objects and arrays that the producing entity find necessary
and sufficient.

However, in order to efficiently communicate metadata across disparate systems and
companies the common properties listed below are defined as _well known_.
Metadata outside this set has low informational value and is in
general not fit for further processing.

| Key             | Type          | Description                                                                     |
|-----------------|---------------|---------------------------------------------------------------------------------|
| TimeSeries.JSON | string        | Version identifier, currently "1.0".                                            |
| name            | string        | Measurement name.                                                               |
| description     | string        | Measurement description.                                                        |
| source          | string        | Source of the measurement: tool, sensor, system etc.                            |
| organization    | string        | Organization behind the measurements.                                           |
| license         | string        | License information for the time series data.                                   |
| location        | [float,float] | Geographic location of the measurements: latitude,longitude in decimal degrees. |
| timeStart       | string        | Time of first measurement.                                                      |
| timeEnd         | string        | Time of last measurement.                                                       |
| timeStep        | float         | Time (in seconds) between time entries, or null if irregular or not known.      |
| dataUri         | string        | Point to data source in case this is kept separate. Can be absolute or relative according to the [URI](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier) specification. |

All header data are optional including the header object itself.



## Signal definition

The following keys are used for signal definitions:

| Key             | Type    | Description                                                                 |
|-----------------|---------|-----------------------------------------------------------------------------|
| name            | string  | Signal name or mnemonic. Mandatory. Non-null.                               |
| description     | string  | Signal description. Optional.                                               |
| quantity        | string  | Signal quantity such as _concentration_, _pressure_, _force_ etc. Optional. |
| unit            | string  | Unit of measurement such as _ppm_, _ft_, _bar_, etc. Optional.              |
| valueType       | string  | Signal value type: _float_, _integer_, _string_, _datetime_ or _boolean_. Non-null. Optional. _float_ assumed if not present. |
| dimensions      | integer | Number of dimensions. [1,>. Non-null. Optional. 1 assumed if not present.   |
| maxSize         | integer | Maximum storage size (number of bytes) for UTF-8 string data. Used with binary storage in order to align the signals. [0,&gt;. Optional. 20 assumed if not present. Ignored for signals where _valueType_ is other than string. |

In addition to the listed, clients may add any number of _custom_
signal definition entries in any form supported by the JSON syntax, but as
for header data in general this is not recommended.



## Data

Measurements are specified in arrays for each index entry, with one entry per signal.
If a signal is multi-dimensional, the entry is itself an array of subentries,
one per dimension.

Measurements are according to the value type defined for the signal,
or `null` for no-values.
The time index is always the first signal listed, and must not contain no-values.
It is advised that the time index signals are continuously increasing or decreasing, but this is not a requirement.

No custom additions to the signal definition may alter the _structure_ of the data definition as specified above.



## The time index

The time index is the first signal listed and contains the values to which all other signals are associated.

The time index value type is typically _datetime_ indicating the exact time of the associated measurements,
but this is no requirement.

Sometimes it may be more convenient to keep the index as in integer or float type representing  a time _delta_
(like milliseconds or seconds) from the `timeStart` entry given in the header.

Or the index values can be without any connection to exact time, but simply indicating relative differences.
This is all up to the client software to decide.



## Writing TimeSeries.JSON data

Writing TimeSeries.JSON can be done in two different formats: _condensed_ or _pretty_.
The condensed format should be without whitespace and newlines and should be used for
transmission between computers only.

For measurements that may possibly be viewed by humans the pretty format should always be used.
This format should contain whitespace and indentation that emphasizes the logical structure of the content.
For the data section in particular, arrays of signal data for each index must be
written _horizontally_ and with commas between entries _aligned_:

```json
    ["2025-09-23T14:20:30Z", 282.589,  8.6657, 2.202, 2.222, [1.759, 2.31469,  1.33991E-3, 3.75839], 0.52435, ... ],
    ["2025-09-23T14:20:35Z", 286.239,    null, 2.277, 2.297, [2.219, 2.31189,        null,    null], 0.52387, ... ],
    ["2025-09-23T14:20:40Z", 276.537, 10.6638, 2.309,  null, [2.267, 2.29509, -3.67117E-3,    null], 0.53936, ... ],
    ["2025-09-23T14:20:45Z", 264.325, 10.6545, 2.324,  null, [2.110, 2.27902, -7.77555E-3, 3.67927], 0.55439, ... ],
    ["2025-09-23T14:20:50Z", 245.938,  9.6937, 2.333, 2.356, [1.525, 2.26512, -1.17965E-2, 3.68386], 0.56211, ... ],
    :
    :
```



## Binary storage

The _data_ object of a TimeSeries.JSON file may optionally be stored in a separate
binary file. The location of the file must be specified in the `dataUri` property of
the header.

The binary format is without structure, it just lists the measurement data row by row.
This allows for extremely fast access along any axis of the data.

The binary storage format for each value type is described below:

| Type | Storage | No-value |
|-------|--------|------------|
| float | 64 bit [IEEE 754](https://en.wikipedia.org/wiki/IEEE_754) floating point representation, [big-endian](https://en.wikipedia.org/wiki/Endianness) | IEEE 754 NaN |
| integer | 64 bit, [big-endian](https://en.wikipedia.org/wiki/Endianness) | 2<sup>63</sup> - 1, being the largest possible 64 bit number |
| string  | [UTF-8](https://en.wikipedia.org/wiki/UTF-8) encoded text, left aligned, space padded, `maxSize` bytes (or 20 if not specified) | Empty string |
| boolean | 8 bit, 0 = false, 1 = true |  Any value different from 0 and 1 |
| datetime | [ASCII](https://en.wikipedia.org/wiki/ASCII) encoded text containing [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) date/time specification, 30 characters | Empty string |



## Schema

Schema for the TimeSeriers.JSON is available [here](schemas/TimeSeries.json).



## Repository content

In addition to the detailed format description given here, the present repository contains tools for varioos computing environments

* [java](java/README.md) - Access library in Java
* [csharp][csharp/README.md] - Access library for .Net
* [python][python/README.md] - Access library for Python
* [excel][excel/README.md] - Tools for opening TimeSeries.JSON files in MS/Excel
* [matlab][matlab/README.md] - Tools for working with TimeSeries.JSON in Matlab
