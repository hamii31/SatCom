# SatCom - Terrestrial Subnetwork Configuration Design
[![DOI](https://zenodo.org/badge/1415321483.svg)](https://doi.org/10.5281/zenodo.23387935)

A computational implementation of the configuration-design model for satellite communication networks described in:

> Verkhovsky, B.S. (2011) 'Satellite communication networks: Configuration design of terrestrial subnetworks', *Journal of Telecommunications Management*, 3(4), pp. 360–373.

## Overview of Version 1.0

The program simulates a population of users and a set of terrestrial earth stations (ES), then determines which users fall within each earth station's serviceable range and computes the cost of servicing them based on their data traffic.

The simulation runs in three stages:

1. **User generation** - A fixed number of users is created, each assigned a random geographic location (latitude/longitude) and a random data-traffic volume in gigabytes.
2. **Earth station generation** - A fixed number of earth stations is created, each assigned a random geographic location.
3. **Assignment & costing** - For every user/earth-station pair, the great-circle distance is computed. Users that fall within an earth station's serviceable radius are assigned to it, along with the monetary cost of servicing their traffic.

> **Note:** The user count and earth-station count are hardcoded. All locations, and each user's data usage, are randomly generated on every run.

## Requirements

- Java Development Kit (JDK) 9 or later (the code uses `List.of(...)`, introduced in Java 9).

## How to Run

```bash
javac SatCom.java
java SatCom
```

## Configuration

The key parameters are set inside `main`:

| Parameter | Variable | Default | Description |
|-----------|----------|---------|-------------|
| Number of users | `n` | `100,000` | Total simulated users |
| Number of earth stations | `m` | `1,000` | Total simulated earth stations |

Additional tunable constants:

| Constant | Location | Default | Description |
|----------|----------|---------|-------------|
| `costPerGB` | `computeCost` | `0.85` | Average SatCom price per GB (USD), midpoint of the 2026 $0.2–$1.5 range |
| `ANTENNA_HEIGHT` | `computeGCDforEachUserAndES` | `50.0` m | Assumed earth-station antenna height |
| `SERVICEABLE_RADIUS` | `computeGCDforEachUserAndES` | derived | Radio horizon distance, computed as `4.12 × √(ANTENNA_HEIGHT)` with atmospheric refraction factored in |
| `earthRadius` | `computeGCD` | `6,371.01` km | Mean radius of the Earth |

## Data Model

- **Users (`P`)** - `Map<Integer, List<Double>>`
  Key: user ID. Value: `[latitude, longitude, dataTraffic (GB)]`.
  - Latitude: random in **[-90, 90]** degrees
  - Longitude: random in **[-180, 180]** degrees
  - Data traffic: random in **[1, 999]** GB

- **Earth stations (`C`)** - `Map<Integer, List<Double>>`
  Key: ES ID. Value: `[latitude, longitude]`.

- **Coverage/cost map (`CPC`)** - `Map<Integer, Map<Integer, Double>>`
  Key: ES ID. Value: a map of `{ userID → servicing cost (USD) }` for every user within that station's serviceable radius.

## Methods

| Method | Purpose |
|--------|---------|
| `createUsers(int n)` | Generates `n` users with random coordinates and data traffic. |
| `createEarthStations(int m)` | Generates `m` earth stations with random coordinates. |
| `computeGCD(x1, y1, x2, y2)` | Returns the great-circle distance (km) between two points using the spherical law of cosines. |
| `computeCost(double traffic)` | Returns the servicing cost for a given traffic volume (`traffic × costPerGB`). |
| `computeGCDforEachUserAndES(P, C)` | Assigns each in-range user to its earth station(s) and records the servicing cost. |
| `main(String[] args)` | Orchestrates the simulation and prints results. |

## Distance Calculation

Distances use the **great-circle distance** (spherical law of cosines):

$$d = R \cdot \arccos\big(\sin\phi_1 \sin\phi_2 + \cos\phi_1 \cos\phi_2 \cos(\lambda_1 - \lambda_2)\big)$$

where $R$ is the Earth's radius, $\phi$ are latitudes and $\lambda$ are longitudes (converted from degrees to radians).

## Sample Output

```
User_ID 1 is at (34.512, -118.203) and accumulates 452.311 GB of traffic.
...

ES_ID 1 is online at (34.981, -117.774)
...

ES_ID 1 is servicing 3 users:
	User_ID 1 - $384.46
	User_ID 57 - $120.07
	User_ID 892 - $640.12
...
```

## Overview of Version 2.0
The program simulates a population of users and will compute the clustering of users. Based on that clustering, it will find the most cost-efficient location for ES for each cluster.

## Reference

Verkhovsky, B.S. (2011) 'Satellite communication networks: Configuration design of terrestrial subnetworks', *Journal of Telecommunications Management*, 3(4), pp. 360–373. Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
