import java.util.*;

/**
 * This class holds the cluster operations logic, such as the building of clusters and the computation of the cost for servicing each cluster.
 * It also holds the logic for choosing the best location for an Earth Station within each cluster
 */
class Clusters {

    /* Find the middle point on a line between two points */
    private static List<Double> computeMidpoint(double x1, double y1, double x2, double y2) {
        double x_m = SatCom.computeAverage(x1, x2);
        double y_m = SatCom.computeAverage(y1, y2);
        
        return List.of(x_m, y_m);
    }

    /** Finds the furthest two users within the cluster */
    private static List<Double> getClusterDiameter(List<Integer> cluster, Map<Integer, List<Double>> P) {
        List<Double> midPointCoordinates = new ArrayList<>();
        for (int i = 0; i < cluster.size(); i++) {
            // reset for each user
            double biggestDistance = Double.MIN_VALUE;

            double x1 = P.get(cluster.get(i)).get(0);
            double y1 = P.get(cluster.get(i)).get(1);

            for (int j = i + 1; j < cluster.size(); j++) {
                double x2 = P.get(cluster.get(j)).get(0);
                double y2 = P.get(cluster.get(j)).get(1);

                double distance = SatCom.computeGCD(x1, y1, x2, y2);

                if (distance > biggestDistance) {
                    midPointCoordinates = new ArrayList<>(computeMidpoint(x1, y1, x2, y2));
                    biggestDistance = distance;
                    midPointCoordinates.add(biggestDistance);
                }            
            }
        }
        
        return midPointCoordinates;
    }

    /** Retrieve the data usage of a user based on their index in a cluster */
    public static double getTrafficOfUserInCluster(List<Integer> cluster, Map<Integer, List<Double>> P, int index) {
        return P.get(cluster.get(index)).get(2);
    }
    /** 
     * 
     * Iterates over a cluster and retrieves the traffic value associated to the current user
     * Returns a list of maps of users and their data traffic
     */
    private static Map<Integer, Double> MapOfUsersAndUsage(List<Integer> cluster, Map<Integer, List<Double>> P) {
        Map<Integer, Double> usersAndTraffics = new HashMap<>();

        for (int i = 0; i < cluster.size(); i++) {
            usersAndTraffics.put(cluster.get(i), getTrafficOfUserInCluster(cluster, P, i));
        }
        return usersAndTraffics;
    }

    /*
    * Gets all User_IDs from a cluster map (the User_ID as a key also)
    * Returns a list of user_IDs
    */
    private static Set<Integer> retrieveCluster(Map.Entry<Integer,Set<Integer>> clusterMap) {
        Set<Integer> cluster = clusterMap.getValue();
        cluster.add(clusterMap.getKey());
        return cluster;
    }
    private static boolean userAlreadyInClusterMap(int user, Map<Integer, Set<Integer>> clustersPerUser) {
        for (Set<Integer> usersInCluster : clustersPerUser.values()) {
            if (usersInCluster.contains(user)) {
                return true;
            }
        }
        return false;
    }
    /* Creates a cluster for each user if the user has nearby users within a serviceable radius */
    private static Map<Integer, Set<Integer>> createCluters(Map<Integer, List<Double>> P) {
        // Holds the key of each user that has a nearby user / cluster of users
        Map<Integer, Set<Integer>> clustersPerUser = new LinkedHashMap<>();
        
        // for each user
        for (Map.Entry<Integer, List<Double>> currentUser : P.entrySet()) {
            // initialize a collection for the keys of the users near the current user
            Set<Integer> nearbyUsers = new HashSet<>();
            // iterate over P
            for (Map.Entry<Integer, List<Double>> users : P.entrySet()) {
                if (!currentUser.getKey().equals(users.getKey()) 
                && (!clustersPerUser.containsKey(currentUser.getKey()) && !userAlreadyInClusterMap(currentUser.getKey(), clustersPerUser))) {
                    // Compute distance between each user
                    double distance = SatCom.computeGCD(currentUser.getValue().get(0), currentUser.getValue().get(1), 
                                                users.getValue().get(0), users.getValue().get(1));

                    // distance should be within the hypotethical range of a network
                    if (distance <= SatCom.SPHERICAL_CAP_RADIUS / 2) {
                        nearbyUsers.add(users.getKey());
                    }
                }
            }
            // create an instance with the current user's ID as a key, and the nearby users as values
            if (!nearbyUsers.isEmpty()) {
                clustersPerUser.put(currentUser.getKey(), nearbyUsers);
            }
        }

        // Display the clusters
        // String message = "User_ID %d has a cluster with :User_ID %d ";
        // DisplayInfo(clustersPerUser, message);
        System.out.printf("%d clusters formed from the %,d users\n", clustersPerUser.size(), P.size());

        return clustersPerUser;   
    }
    private static double getBiggestDiameter(double d1, double d2) {
        return d1 > d2 ? d1 : d2;
    }
    private static boolean isWithinAnotherNetwork(Map<Map<Integer, List<Double>>, Map<Integer, Double>> networks, List<Double> currentESCoordinates,
    double d1, Map<Integer, List<Double>> P) {
        for (Map.Entry<Map<Integer, List<Double>>, Map<Integer, Double>> entry : networks.entrySet()) {
            Map<Integer, List<Double>> ES = entry.getKey();
            Map<Integer, Double> userInfo = entry.getValue();
            List<Integer> userCluster = new ArrayList<>(userInfo.keySet());

            for (Map.Entry<Integer, List<Double>> ESEntry : ES.entrySet()) {
                // compute the diameter of the cluster of the network
                List<Double> params = getClusterDiameter(userCluster, P);
                double d2 = params.get(2);

                // get the bigger diameter
                double greatestDiameter = getBiggestDiameter(d1, d2);

                // compute the distance between the two stations, the distance should be smaller than the greatest diameter of the two
                double distance = SatCom.computeGCD(ESEntry.getValue().get(0), ESEntry.getValue().get(1), currentESCoordinates.get(0), currentESCoordinates.get(1));
                
                if (distance < greatestDiameter) {
                    return true;
                }          
            }
        }
        
        return false;
    }
    private static void createANetwork (double clusterDiameter, List<Double> coordinates, List<Integer> cluster, 
    Map<Map<Integer, List<Double>>, Map<Integer, Double>> networks, Map<Integer, List<Double>> P) {
        
        Map<Integer, List<Double>> C = new HashMap<>();
        C.put(1, coordinates);
        double microwaveCost = SatCom.computeMicrowaveCost(clusterDiameter);
        double opticFCost = SatCom.computeOpticCost(clusterDiameter);

        if (microwaveCost > opticFCost) {
            if (SatCom.isCostEfficient(opticFCost)) {
                double netRevenuePerMonth = SatCom.computeTotalEarningsPerStation(cluster, P, false, opticFCost);
                // add the network cost to the coordinates of the ES
                C.get(1).add(opticFCost);
                // 1.0 to denote that it is an optic fiber network
                C.get(1).add(1.0); 
                C.get(1).add(netRevenuePerMonth);
                networks.put(C, MapOfUsersAndUsage(cluster, P));
            }
        } else if (opticFCost > microwaveCost) {
            if (SatCom.isCostEfficient(microwaveCost)) {
                double netRevenuePerMonth = SatCom.computeTotalEarningsPerStation(cluster, P, true, opticFCost);
                C.get(1).add(microwaveCost);
                // 0.0 to denote that it is a microwave network
                C.get(1).add(0.0);
                C.get(1).add(netRevenuePerMonth);
                networks.put(C, MapOfUsersAndUsage(cluster, P));
            }
        }          
    }
    /** 
     * Iterates over each cluster, finds the midpoint coordinates of the biggest diameter for each cluster, 
     * creates an ES at the midpoint of each cluster, computes the cost for both a Microwave and Optic Network within each cluster,
     * and checks which network is more cost-effective
     * Returns a map of:
     *  - Keys -> Map -> Earth Station Key, List of double values representing coordinates, cost and network type
     *  - Values -> Map -> User_ID, double value representing the user data usage in Mbps
     */
    private static Map<Map<Integer, List<Double>>, Map<Integer, Double>> mostCostEffectiveNetwork(Map<Integer, Set<Integer>> clustersPerUser, 
        Map<Integer, List<Double>> P) {
    
        Map<Map<Integer, List<Double>>, Map<Integer, Double>> networks = new LinkedHashMap<>();
        
        // iterate over the clusters
        for (Map.Entry<Integer, Set<Integer>> clusterInfo : clustersPerUser.entrySet()) {
            // Get all users from the cluster
            List<Integer> cluster = new ArrayList<>(retrieveCluster(clusterInfo));

            List<Double> arr = getClusterDiameter(cluster, P);
            double clusterDiameter = arr.get(2);
            List<Double> coordinates = new ArrayList<>(List.of(arr.get(0), arr.get(1)));

            if (networks.isEmpty()) {
                // if no other networks exist
                createANetwork(clusterDiameter, coordinates, cluster, networks, P);
            }
            else if (!isWithinAnotherNetwork(networks, coordinates, clusterDiameter, P)) {
                // if the current cluster is not within another network
                createANetwork(clusterDiameter, coordinates, cluster, networks, P);
            }
        }

        return networks;
    }

    /**
     * Creates user clusters
     * Returns a map of Networks
     */
    public static Map<Map<Integer, List<Double>>, Map<Integer, Double>> buildNetwork(Map<Integer, List<Double>> P) {
        // Create clusters for each user, if applicable 
        Map<Integer, Set<Integer>> clustersPerUser = createCluters(P);

        // build networks
        Map<Map<Integer, List<Double>>, Map<Integer, Double>> networks = mostCostEffectiveNetwork(clustersPerUser, P);
        if (networks.isEmpty()) {
            System.out.println("No cost-efficient networks could be built.");
        } else {
            SatCom.displayNetworks(networks);
        }
        return networks;
    }
} 
