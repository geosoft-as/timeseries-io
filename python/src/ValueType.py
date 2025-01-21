from enum import Enum

import datetime

class ValueTypeItem:
    def __init__(self, name, value_type):
        self._name = name
        self._value_type = value_type

    def get_name(self) -> str:
        return self._name

    def get_value_type(self):
        return self._value_type

    def __str__(self):
        return self._name

class ValueType(Enum):
    FLOAT = ValueTypeItem("float", float)
    INTEGER = ValueTypeItem("integer", int)
    STRING = ValueTypeItem("string", str)
    BOOLEAN = ValueTypeItem("boolean", bool)
    DATETIME = ValueTypeItem("datetime", datetime.date)

    def get(value):
        for item in ValueType:
            if isinstance(value, str) and item.value.get_name() == value:
                return item

            if value in (int, float, bool, str, datetime.date) and item.value.get_value_type() == value:
                return item

        return None
