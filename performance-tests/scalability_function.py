def calculate_cost(
    cpu_percentage, storage_ratio, threads, db_conns, read_visible_rate, weights: dict
):
    """
    Calculates C(k) using normalized weights and saturation penalties.

    Formula: C(k) = (w_cpu * CPU)^2 + (w_st * Storage) + (w_t * Thread_Saturation)^2 + (w_db * DB_Saturation) + (w_rvr * (1 - read_visible_rate) ^ 4)
    """
    THREAD_LIMIT = 200
    DB_LIMIT = 10

    thread_sat = (threads / THREAD_LIMIT) ** 2
    db_sat = (db_conns / DB_LIMIT) ** 2
    read_visible = (1 - read_visible_rate) ** 4
    cost = (
        (weights["cpu"] * cpu_percentage)
        + (weights["storage"] * storage_ratio)
        + (weights["threads"] * thread_sat)
        + (weights["db"] * db_sat)
        + (weights["read_visible_weight"] * read_visible)
    )
    return cost


def calculate_productivity(rps, drop_rate, latency_p95, cost):
    """
    Calculates F(k) = (Throughput * Value) / Cost
    Throughput = rps - dropped
    Value = 1 / latency_p95 (Higher latency reduces value)
    """
    throughput = max(0, rps - drop_rate)
    value = 1 / latency_p95 if latency_p95 > 0 else 0

    # F(k) = (Lambda * f(k)) / C(k)
    return (throughput * value) / cost if cost > 0 else 0


# CONFIGURATION
weights = {
    "cpu": 1.0,
    "storage": 0.5,
    "threads": 1.0,
    "db": 1.0,
    "read_visible_weight": 3.0,
}

k1_data = {
    "rps": 300,
    "drop_rate": 0,
    "latency_p95": 3.69 + 2.55,
    "cpu": 0.2602,
    "storage_ratio": 1.6,
    "threads": 39,
    "db": 3,
    "read_visible_rate": 0.76,
}

k2_data = {
    "rps": 400,
    "drop_rate": 0,
    "latency_p95": 3.7 + 2.89,
    "cpu": 0.3566,
    "storage_ratio": 1.6,
    "threads": 100,
    "db": 4,
    "read_visible_rate": 0.02,
}

# Execution
c1 = calculate_cost(
    k1_data["cpu"],
    k1_data["storage_ratio"],
    k1_data["threads"],
    k1_data["db"],
    k1_data.get("read_visible_rate", 1),
    weights,
)
f1 = calculate_productivity(
    k1_data["rps"], k1_data["drop_rate"], k1_data["latency_p95"], c1
)

c2 = calculate_cost(
    k2_data["cpu"],
    k2_data["storage_ratio"],
    k2_data["threads"],
    k2_data["db"],
    k2_data.get("read_visible_rate", 1),
    weights,
)
f2 = calculate_productivity(
    k2_data["rps"], k2_data["drop_rate"], k2_data["latency_p95"], c2
)

# Scalability Function: psi = F(k2) / F(k1)
scalability = f2 / f1 if f1 > 0 else 0

print(f"--- Results ---")
print(f"Productivity at k1 (F1): {f1:.4f}")
print(f"Productivity at k2 (F2): {f2:.4f}")
print(f"Scalability (ψ): {scalability:.4f}")

if scalability < 1.0:
    print("System is sub-linear. Cost/Latency is outgrowing throughput.")
else:
    print("System is scaling efficiently.")
