public class ConversionTemperatura { 
public double convertTemperature(double temperature, String fromUnit, String toUnit) { 
// Convert the units to lowercase for case-insensitive comparison 
fromUnit = fromUnit.toLowerCase(); 
toUnit = toUnit.toLowerCase(); 
// Check for valid units and perform the corresponding conversion 
if (fromUnit.equals("celsius")) { 
if (toUnit.equals("fahrenheit")) { 
return (temperature * 9 / 5) + 32; 
} else if (toUnit.equals("kelvin")) { 
return temperature + 273.15; 
} 
} else if (fromUnit.equals("fahrenheit")) { 
if (toUnit.equals("celsius")) { 
return (temperature - 32) * 5 / 9; 
} else if (toUnit.equals("kelvin")) { 
return ((temperature - 32) * 5 / 9) + 273.15; 
} 
} else if (fromUnit.equals("kelvin")) { 
if (toUnit.equals("celsius")) { 
return temperature - 273.15; 
} else if (toUnit.equals("fahrenheit")) { 
return ((temperature - 273.15) * 9 / 5) + 32; 
} 
} 
// If the units are invalid or not recognized, return NaN 
return Double.NaN; 
} 