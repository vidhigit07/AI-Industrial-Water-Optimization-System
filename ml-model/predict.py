import joblib
import pandas as pd

# Load trained ML model
model = joblib.load("water_demand_model.pkl")

print("Industrial Water Demand Prediction System")
print("-----------------------------------------")

# Take input values from user
production_load = float(input("Enter Production Load (%): "))
temperature = float(input("Enter Temperature (°C): "))
cleaning_cycle = int(input("Cleaning Cycle Active? (1 = Yes, 0 = No): "))
shift_hours = float(input("Enter Shift Hours: "))

# Create input data
input_data = pd.DataFrame([{
    "Production_Load": production_load,
    "Temperature": temperature,
    "Cleaning_Cycle": cleaning_cycle,
    "Shift_Hours": shift_hours
}])

# Predict demand
prediction = model.predict(input_data)[0]

print("\nPredicted Water Demand:", round(prediction, 2), "litres/hour")

# Compare with current max flow system
current_capacity = 29

print("Current Optimized Network Capacity:", current_capacity, "flow units")

if prediction > 1300:
    print("\nStatus: High Demand Expected")
    print("Suggestion: Check bottleneck pipelines and run What-If Simulation.")
else:
    print("\nStatus: Demand is Normal")
    print("Suggestion: Current infrastructure is likely sufficient.")