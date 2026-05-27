
import sys
import joblib
import pandas as pd

if len(sys.argv) != 5:
    print("Usage: py predict_for_java.py <Production_Load> <Temperature> <Cleaning_Cycle> <Shift_Hours>")
    sys.exit(1)

production_load = float(sys.argv[1])
temperature = float(sys.argv[2])
cleaning_cycle = int(sys.argv[3])
shift_hours = float(sys.argv[4])

model = joblib.load("ml-model/water_demand_model.pkl")

input_data = pd.DataFrame([{
    "Production_Load": production_load,
    "Temperature": temperature,
    "Cleaning_Cycle": cleaning_cycle,
    "Shift_Hours": shift_hours
}])

prediction = model.predict(input_data)[0]

print("AI WATER DEMAND PREDICTION")
print("--------------------------")
print("Production Load:", production_load, "%")
print("Temperature:", temperature, "C")
print("Cleaning Cycle:", "Active" if cleaning_cycle == 1 else "Inactive")
print("Shift Hours:", shift_hours)
print()
print("Predicted Water Demand:", round(prediction, 2), "litres/hour")

if prediction > 1300:
    print()
    print("Status: HIGH DEMAND EXPECTED")
    print("Suggestion: Run What-If Simulation and check critical bottleneck pipelines.")
else:
    print()
    print("Status: NORMAL DEMAND")
    print("Suggestion: Current infrastructure is likely sufficient.")
