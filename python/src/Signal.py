class Signal:
    def __init__(self, name, description, quantity, unit, value_type, n_dimensions):
        self._name = name
        self._description = description
        self._quantity = quantity
        self._unit = unit
        self._value_type = value_type
        self._n_dimensions = n_dimensions

