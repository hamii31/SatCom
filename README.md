# SatCom - Terrestrial Subnetwork Configuration Design
[![DOI](https://zenodo.org/badge/1415321483.svg)](https://doi.org/10.5281/zenodo.23387935)

A computational implementation of the configuration-design model for satellite communication networks described in:

> Verkhovsky, B.S. (2011) 'Satellite communication networks: Configuration design of terrestrial subnetworks', *Journal of Telecommunications Management*, 3(4), pp. 360–373.

## Overview of SatCom

The program simulates a population of users and computes the clusters that form between them. Within each cluster, the program computes the best location for a terrestrial earth station. It then computes the cost for the construction of a network with which to offer service within the cluster. The network can be a single-hop microwave network or an optic fiber network, the type is chosen based on cost-efficiency. 

The simulation runs in five stages:

1. **User generation** - A fixed number of users is created, each assigned a random geographic location (latitude/longitude) and a random data-traffic volume in gigabytes.
2. **Cluster computation** - A cluster is created when a user has close neighboring user/s (within serviceable range).
3. **Middle ground discovery** - For each cluster, a middle-ground is established via biggest diameter midpoint computation. (More improvements needed)
4. **Earth station generation** - Each middle ground serves as the location for a ES
5. **Cost computation** - The most cost-efficient network is chosen based on factors like per-user-demand, microwave capacity and radius, cost-per-hop, oversubscription rate, and cost of optic cable per km

> **Note:** The user count is hardcoded. User locations, and user's data usage, are randomly generated on every run.

## Sample Output

```
User_ID 1 is at (-60.005, -88.877) and accumulates 84.13 Mbps of traffic
...

User_ID 20000 is at (71.690, -107.702) and accumulates 60.76 Mbps of traffic
6834 clusters formed from the 20,000 users
...

ES_ID at (68.130, 71.073) is a/an Microwave Network and costs $150,000.00 and is servicing 2 users 
...

ES_ID at (-81.618, -74.557) is a/an Microwave Network and costs $375,000.00 and is servicing 5 users 
ES_ID at (-85.797, 115.274) is a/an Microwave Network and costs $375,000.00 and is servicing 4 users 

There are 4,093 ES in use out of 4,093, and they are serving 10,349 users out of 20,000
There are 4014 Microwave Networks and 79 Optic Networks.
```

## Project Requirements

- Java Development Kit (JDK) 9 or later.

## How to Run The Project

```bash
javac SatCom.java
java SatCom
```

## Thoughts
With the complete overhaul, the computational process became less clogged, and a noticeable boost in speed can be observed. However, more improvements are necessary, specifically within the clustering. 

## Reference

Verkhovsky, B.S. (2011) 'Satellite communication networks: Configuration design of terrestrial subnetworks', *Journal of Telecommunications Management*, 3(4), pp. 360–373. Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
