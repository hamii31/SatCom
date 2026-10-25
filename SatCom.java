import java.util.*;

/**
 * Computational implementation of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 */
/**
 * TO-DO - Decide on a hypothetical network radius for better clustering
 */

public class SatCom {
    /** Helper / Shared functions and constants */
    final static double EARTH_RADIUS = 6_371.01; // km

    final static double SPHERICAL_CAP_RADIUS = computeSphericalCapRadius();

    /** simplified serviceable radius based on a single line-of-sight microwave hop for a terrestrial earth station */
    final static int MICROWAVE_RADIUS = 50; // km

    final static int MICROWAVE_CAPACITY = 2000; // Mbps

    /** average cost per microwave hop */
    final static double COST_PER_HOP = 75_000.0; // USD

    /** how many users can use the network at once */
    final static int OVERSUBSCRIPTION_RATE = 20; 

    /** how much (at max) would every user be allowed to demand from the network in mbps */
    final static double PER_USER_DEMAND = 100.0;

    final static double OPTIC_PER_KM = 17_500.0; // USD

    /** The roof price for a two-way earth station */
    final static double ES_PRICE_ROOF = 1_000_000.0; // USD
    /* Compute the radius of a spherical cap placed on a sphere 
    * h - height of the spherical cap, determines its radius
    *     - h = 0 -> no cap
    *     - h = R -> cap covers exactly half of the sphere
    *     - h = 2R -> cap covers the entire sphere
    */
    public static double computeSphericalCapRadius() {
        double h = EARTH_RADIUS / 128; // around 50km height
        return Math.sqrt(2 * (EARTH_RADIUS * h) - Math.pow(h, 2));
    }   
    
    public static double convertDegreesToRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    /* Computes the great-circle distance between two points on a sphere (Earth) */
    public static double computeGCD(double x1, double y1, double x2, double y2) {

        // Convert degrees to radians
        x1 = convertDegreesToRadians(x1);
        y1 = convertDegreesToRadians(y1);
        x2 = convertDegreesToRadians(x2);
        y2 = convertDegreesToRadians(y2);

        // Compute the great circle distance between the two points
        double gcd = EARTH_RADIUS * Math.acos(Math.sin(x1) * Math.sin(x2) 
                                            + Math.cos(x1) * Math.cos(x2)
                                            * Math.cos(y1 - y2));

        return gcd;
    }

    /* Computes the net revenue per month for a teleport network, the pricing formula changes per type of network */
    public static double computeTotalEarningsPerStation(List<Integer> cluster, Map<Integer, List<Double>> P, boolean isMicrowave, double stationCost) {
        // set lifespan / loadedCost based on network type
        int lifespan = isMicrowave ? 120 : 240;
        double loadedCost = isMicrowave ? 300.0 : 150.0;
        double margin = 0.5;
        
        // get actual mbps usage for all users within the cluster
        double totalUsage = 0.0;
         for (int id : cluster) {
            totalUsage += P.get(id).get(2);
        }

        // Monthly operating cost for the capacity actually provisioned.
        double monthlyOpCost = totalUsage * loadedCost;

        // Monthly CapEx recovery, straight-line over the lifespan.
        double monthlyCapEx = stationCost / lifespan;

        double monthlyCostBase = monthlyOpCost + monthlyCapEx;

        // Price = cost × (1 + margin).
        return monthlyCostBase * (1 + margin);
    }

    /** Computes the cost of servicing users via optic cables */
    public static double computeOpticCost(double clusterDiameter) {
        return OPTIC_PER_KM * clusterDiameter;
    }

    /** Computes the cost of servicing users within a microwave network */
    public static double computeMicrowaveCost(double clusterDiameter) {
        int hops = isWithinMicrowaveHopDistance(clusterDiameter) ? 1 : (int) (clusterDiameter / MICROWAVE_RADIUS);
        return hops * (COST_PER_HOP * Math.ceil(clusterDiameter / MICROWAVE_RADIUS));
    }   

    public static boolean isCostEfficient(double cost) {
        return cost <= ES_PRICE_ROOF;
    }

    /** 
    * Checks if the user is within serviceable distance for microwave communication
    */
    public static boolean isWithinMicrowaveHopDistance(double distance) {
        return distance <= MICROWAVE_RADIUS;
    }

    /** Checks if the users are too many for the microwave option */
    public static boolean isWithinMicrowaveCapacity(List<Integer> cluster) {
        return (cluster.size() / (double) OVERSUBSCRIPTION_RATE) * PER_USER_DEMAND <= MICROWAVE_CAPACITY; 
    }

    /** Computes the cost of fiber optic cable per KM */
    public static double computeFiberOpticCost(double distance) {
        return distance * OPTIC_PER_KM;
    }

    public static double computeAverage(double a, double b) {
        return (a + b) / (double) 2;
    }

    /* A reusable helper function for printing out Dictionaries of variable Number data types with a custom message */
    public static <T extends Number> void DisplayInfo(Map<Integer, List<T>> collection, String message) {
        String[] messages = message.split(":");

        for (Map.Entry<Integer, List<T>> info : collection.entrySet()) {
            System.out.printf(messages[0], info.getKey());
            
            List<T> values = info.getValue();
            for (int i = 0; i < values.size(); i++) {
                if (values.size() < messages.length)
                    System.out.printf(messages[i + 1], values.get(i));
                else
                    System.out.printf(messages[1], values.get(i));
            }
            System.out.print("\n");
        }
    }
    /* 
    * Displays every ES and the users being serviced by it 
    * Returns a list holding the count of earth station in use (out of all) and the users being served by a station (out of all)
    */
    public static List<Integer> displayNetworks(Map<Map<Integer, List<Double>>, Map<Integer, Double>> PC) {
        int countOfUsersServed = 0;
        int countOfOpticNetworks = 0;
        int countOfMicrowaveNetworks = 0;

        int ESKey = 1;
        for (Map.Entry<Map<Integer, List<Double>>, Map<Integer, Double>> entry : PC.entrySet()) {
            // for each station
            Map<Integer, List<Double>> ES = entry.getKey();
            
            // get user count
            int servedUsers = entry.getValue().size();
            countOfUsersServed += servedUsers;

            // get the "values" of the "key"
            Map.Entry<Integer, List<Double>> ESEntry = ES.entrySet().iterator().next();
            double lat = ESEntry.getValue().get(0);
            double lon = ESEntry.getValue().get(1);
            double ESCost = ESEntry.getValue().get(2);
            double networkType = ESEntry.getValue().get(3);
            double earnings = ESEntry.getValue().get(4);

            String s = networkType == 0.0 ? "Microwave Network" : "Optic Fiber Network";

            countOfMicrowaveNetworks += networkType == 0.0 ? 1 : 0;
            countOfOpticNetworks += networkType == 1.0 ? 1 : 0;

            System.out.printf("ES_ID %d at (%.3f, %.3f) will be a %s, cost $%,.2f to be built, make $%,.2f/month, and be servicing %d users \n", ESKey, lat, lon, s, ESCost, earnings, servedUsers);
            ESKey++;
        }

        return List.of(countOfUsersServed, countOfMicrowaveNetworks, countOfOpticNetworks);
    }
    public static class SatComClusters {

        /* Find the middle point on a line between two points */
        private static List<Double> computeMidpoint(double x1, double y1, double x2, double y2) {
            double x_m = computeAverage(x1, x2);
            double y_m = computeAverage(y1, y2);
            
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

                    double distance = computeGCD(x1, y1, x2, y2);

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
                        double distance = computeGCD(currentUser.getValue().get(0), currentUser.getValue().get(1), 
                                                    users.getValue().get(0), users.getValue().get(1));

                        // distance should be within the hypotethical range of a network
                        if (distance <= SPHERICAL_CAP_RADIUS / 2) {
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
                    double distance = computeGCD(ESEntry.getValue().get(0), ESEntry.getValue().get(1), currentESCoordinates.get(0), currentESCoordinates.get(1));
                    
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
            double microwaveCost = computeMicrowaveCost(clusterDiameter);
            double opticFCost = computeOpticCost(clusterDiameter);

            if (microwaveCost > opticFCost) {
                if (isCostEfficient(opticFCost)) {
                    double netRevenuePerMonth = computeTotalEarningsPerStation(cluster, P, false, opticFCost);
                    // add the network cost to the coordinates of the ES
                    C.get(1).add(opticFCost);
                    // 1.0 to denote that it is an optic fiber network
                    C.get(1).add(1.0); 
                    C.get(1).add(netRevenuePerMonth);
                    networks.put(C, MapOfUsersAndUsage(cluster, P));
                }
            } else if (opticFCost > microwaveCost) {
                if (isCostEfficient(microwaveCost)) {
                    double netRevenuePerMonth = computeTotalEarningsPerStation(cluster, P, true, opticFCost);
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
        public Map<Map<Integer, List<Double>>, Map<Integer, Double>> buildNetwork(Map<Integer, List<Double>> P) {
            // Create clusters for each user, if applicable 
            Map<Integer, Set<Integer>> clustersPerUser = createCluters(P);

            // build networks
            Map<Map<Integer, List<Double>>, Map<Integer, Double>> networks = mostCostEffectiveNetwork(clustersPerUser, P);
            if (networks.isEmpty()) {
                System.out.println("No cost-efficient networks could be built.");
            } else {
                displayNetworks(networks);
            }
            return networks;
        }
    } 
    public static class SatComCapClusters {
        /**
        * Version 4.0:
        * The user count is hardcoded, the location of each user is generated within a pre-defined spherical cap on the surface of Earth
        * The earth station locations are to be computed based on the clustering of users
        * A network is established only if its cost effective
         */


        /* 
        * Chooses a random location for the center of a spherical cap and computes its radius
        * Returns a list of the coordinates and radius of the cap (no need for map since its a singular entity)
         */
        private static List<Double> buildSphericalCap() {
            double latitude = -90 + (Math.random() * 180);           
            double longitude = -180 + (Math.random() * 360);
            double sphericalCapRadius = SPHERICAL_CAP_RADIUS;
            System.out.printf("The radius of the spherical cap is %.2f\n", sphericalCapRadius);
            return List.of(latitude, longitude, sphericalCapRadius);
        }
        private static boolean userWithiNCap(double userLat, double userLong, List<Double> cap) {
            double capLat = cap.get(0);
            double capLong = cap.get(1);
            double capRadius = cap.get(2);

            double distance = computeGCD(userLat, userLong, capLat, capLong);

            return distance <= capRadius;
        }
        /* Similar to the general method, with one caveat -> it checks if the point formed by
        * the coordinates is within the radius of the spherical cap. 
         */
        private static Map<Integer, List<Double>> createRandomUsersWithinCap(int n, List<Double> cap) {
            Map<Integer, List<Double>> P = new LinkedHashMap<>();
            int users = 0;
            while(users < n) {
                double latitude = -90 + (Math.random() * 180);
                double longitude = -180 + (Math.random() * 360);

                if(userWithiNCap(latitude, longitude, cap)) {
                    double mbps = 1 + Math.random() * PER_USER_DEMAND;
                    P.put(users + 1, List.of(latitude, longitude, mbps));
                    users++;
                }
            }
            return P;
        }

        /* Creates the cap and generates the users within the cap
        * Calls SatComClusters to compute the clusters that form within the cap and computes 
        * the count of Earth Stations and their cost for each network
         */
        public Map<Map<Integer, List<Double>>, Map<Integer, Double>> buildNetworkWithinCap(int n) {
            List<Double> cap = buildSphericalCap();

            Map<Integer, List<Double>> P = createRandomUsersWithinCap(n, cap);
            String message = "User_ID %d is at :(%.3f:, %.3f): and accumulates %.2f Mbps of traffic";
            DisplayInfo(P, message);

            SatComClusters scs = new SatComClusters();
            return scs.buildNetwork(P);
        }
    }

    public static void main(String[] args) {
        // number of users
        int n = 20_000;

        SatComCapClusters sccc = new SatComCapClusters();
        Map<Map<Integer, List<Double>>, Map<Integer, Double>> _ = sccc.buildNetworkWithinCap(n);
    }
}
