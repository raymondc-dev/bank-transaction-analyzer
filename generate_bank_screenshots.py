import os
impot pandas as pd
import matplotlib.pyplot as plt
os.makedirs("screenshots", exist_ok=True)

df = pd.read_csv("category_month_report.csv")

# Pie: total by category
summary = df.groupby("category")["amount"].sum()
plt.figure(figsize=(6,6))
summary.plot(kind="pie", autopct='%1.1f%%', startangle=90)
plt.title("Total Spending by Category")
plt.ylabel("")
plt.savefig("screenshots/summary.png", bbox_inches='tight')
plt.close()

# Bar: month x category
monthly = df.pivot(index="month", columns="category", values="amount").fillna(0)
monthly.plot(kind="bar", figsize=(8,5))
plt.title("Monthly Spending by Category")
plt.ylabel("Amount ($)")
plt.xlabel("Month")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig("screenshots/monthly.png", bbox_inches='tight')
plt.close()

print("Generated screenshots/summary.png and screenshots/monthly.png")
