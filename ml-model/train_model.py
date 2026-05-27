print("Starting model training...")
import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error
import joblib

# Step 1: Load dataset
data = pd.read_csv("water_demand_dataset.csv")
print("Dataset loaded")


# Step 2: Select input features
X = data[["Production_Load", "Temperature", "Cleaning_Cycle", "Shift_Hours"]]

# Step 3: Select output/target column
y = data["Water_Demand"]

# Step 4: Split data into training and testing parts
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# Step 5: Create Linear Regression model
model = LinearRegression()

# Step 6: Train the model
model.fit(X_train, y_train)
print("Model fitting complete")

# Step 7: Test the model
predictions = model.predict(X_test)

# Step 8: Check error
error = mean_absolute_error(y_test, predictions)

print("Model trained successfully!")
print("Mean Absolute Error:", round(error, 2))

# Step 9: Save the trained model
joblib.dump(model, "water_demand_model.pkl")

print("Model saved as water_demand_model.pkl")