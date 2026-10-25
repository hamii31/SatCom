# SatCom - Terrestrial Subnetwork Configuration Design
[![DOI](https://zenodo.org/badge/1415321483.svg)](https://doi.org/10.5281/zenodo.23387935)

A computational implementation of the configuration-design model for satellite communication networks described in:

> Verkhovsky, B.S. (2011) 'Satellite communication networks: Configuration design of terrestrial subnetworks', *Journal of Telecommunications Management*, 3(4), pp. 360–373.

## Overview of SatCom

The program simulates a population of users within a pre-defined area, and computes the clusters that form between them. Within each cluster, the program computes the best location for a terrestrial earth station. It then computes the cost for the construction of a network with which to offer service within the cluster. The network can be a microwave network or an optic fiber network, the type is chosen based on cost-efficiency. 

The simulation runs in five stages:

1. **Spherical cap computation** - A location on Earth's surface is randomly generated, then a spherical cap is computed based on hardcoded height and Earth radius constants.
2. **User generation** - A fixed number of users is created, each assigned a random geographic location (latitude/longitude) within a specified zone, and a random data-traffic volume in mbps.
3. **Cluster computation** - A cluster is created when a user has close neighboring user/s within a pre-defined range (to be improved). 
4. **Middle ground discovery** - For each cluster, a middle-ground is established via biggest diameter midpoint computation.
5. **Earth station generation** - Each middle ground serves as the location for a ES only if the radius of the network does not overlap with an existing network. 
6. **Cost computation** - The type of network is chosen based on the cost of the infrastructure and on a contention ratio model for each network.

> **Note:** The user count is hardcoded. User locations, and user's data usage, are randomly generated on every run.

## Sample Output

```
User_ID 1 is at (-60.005, -88.877) and accumulates 84.13 Mbps of traffic
...


User_ID 19999 is at (65.111, 52.254) and accumulates 21.11 Mbps of traffic
User_ID 20000 is at (67.926, 46.539) and accumulates 26.80 Mbps of traffic

13 clusters formed from the 20,000 users
...

ES_ID 1 at (71.227, 28.645) will be a Microwave Network, cost $450,000.00 to be built, make $113,774,277.34/month, and be servicing 4931 users 
ES_ID 2 at (69.710, 63.331) will be a Microwave Network, cost $450,000.00 to be built, make $107,222,645.19/month, and be servicing 4680 users 
ES_ID 3 at (75.296, 39.133) will be a Microwave Network, cost $150,000.00 to be built, make $69,813,678.38/month, and be servicing 3042 users 

```

## Project Requirements

- Java Development Kit (JDK) 9 or later.

## How to Run The Project

```bash
javac SatCom.java
java SatCom
```

## Thoughts
Generating the users within a specified area helps with better clustering strategies, and cost-effective modeling. The computation time has improved, however the logic is getting harder to follow - simplifications and improvements needed. 

## Reference

Verkhovsky, B.S. (2011) 'Satellite communication networks: Configuration design of terrestrial subnetworks', *Journal of Telecommunications Management*, 3(4), pp. 360–373. Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
