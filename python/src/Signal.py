class Signal:
    def __init__(self, name, description, quantity, unit, value_type, n_dimensions):
        self._name = name
        self._description = description
        self._quantity = quantity
        self._unit = unit
        self._value_type = value_type
        self._n_dimensions = n_dimensions

        if value_type in (float, int):
            self._size = 8
        elif value_type == datetime.date:
            self._size = 30
        elif value_type == bool:
            self._size = 1
        else:
            self._size = 0

    def get_name(self):
        return self._name

    def get_quantity(self):
        return self._quantity

    def get_unit(self):
        return self._unit

    def get_value_type(self):
        return self._value_type

    def get_n_dimensions(self):
        return self._n_dimensions

    def get_size(self):
        return self._size

    def set_size(self, size):
        self._size = size

    def add_value(self, dimension, value):


s = Signal("lat", "test", "test", "m", float, 1)
print(s.get_name())
