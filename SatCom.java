import java.util.*;

/**
 * Computational implementation of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 */

public class SatCom {
    /** Helper / Shared functions and constants */
    final static double EARTH_RADIUS = 6_371.01; // km

    /** simplified serviceable radius based on a single line-of-sight microwave hop for a terrestrial earth station */
    final static int MICROWAVE_RADIUS = 50; // km

    final static int MICROWAVE_CAPACITY = 2000; // Mbps

    /** average cost per microwave hop */
    final static double COST_PER_HOP = 75_000.0; // USD

    /** how many users can use the network at once */
    final static int OVERSUBSCRIPTION_RATE = 20; 

    /** how much (at max) would every user be allowed to demand from the network in mbps */
    final static double PER_USER_DEMAND = 99.0;

    final static double OPTIC_PER_KM = 17_500.0; // USD

    /** standard pricing for a two-way earth station */
    final static double ES_PRICE = 400_000.0;

    /**
     * Returns a map of users at randomly generated locations with randomly generated usage in Mbps
     */
    public static Map<Integer, List<Double>> createRandomUsers(int n) {
        Map<Integer, List<Double>> P = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            // generate a random double value between -90 and 90 degrees for the latitude
            double latitude = -90 + (Math.random() * 180);
            // generate a random double value between -180 and 180 degrees for the longtitude 
            double longitude = -180 + (Math.random() * 360);
            // generate a random double value between 1 to max demand
            double mbps = 1 + Math.random() * PER_USER_DEMAND;

            P.put(i + 1, List.of(latitude, longitude, mbps));
        }
        return P;
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

    /** Computes the cost of servicing users via optic cables */
    public static double computeOpticCost(List<Integer> cluster, Map<Integer, List<Double>> C, Map<Integer, List<Double>> P) {
        double cost = 0.0;
        for (int i = 0; i < cluster.size(); i++) {
            List<Double> clusterUserLoc = P.get(cluster.get(i));
            List<Double> ESLoc = C.get(1);

            double distance = computeGCD(clusterUserLoc.get(0), clusterUserLoc.get(1), ESLoc.get(0), ESLoc.get(1));
            cost += OPTIC_PER_KM * distance;
        }

        return cost;
    }

    /** Computes the cost of servicing users within one micorwave hop */
    public static double computeMicrowaveCost(List<Integer> cluster, Map<Integer, List<Double>> C, Map<Integer, List<Double>> P) {
        double cost = 0.0;
        for (int i = 0; i < cluster.size(); i++) {
            List<Double> clusterUserLoc = P.get(cluster.get(i));
            List<Double> ESLoc = C.get(1);

            double distance = computeGCD(clusterUserLoc.get(0), clusterUserLoc.get(1), ESLoc.get(0), ESLoc.get(1));
            cost += COST_PER_HOP * Math.ceil(distance / MICROWAVE_RADIUS);
        }

        return cost;
    }   

    public static boolean isCostEfficient(double cost) {
        return cost <= ES_PRICE;
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
    public static List<Integer> displayPC(Map<Map<Integer, List<Double>>, Map<Integer, Double>> PC) {
        int countOfESInUse = 0;
        int countOfUsersServed = 0;
        int countOfOpticNetworks = 0;
        int countOfMicrowaveNetworks = 0;

        for (Map.Entry<Map<Integer, List<Double>>, Map<Integer, Double>> entry : PC.entrySet()) {
            // for each station
            Map<Integer, List<Double>> ES = entry.getKey();
            for (Map.Entry<Integer, List<Double>> ESEntry : ES.entrySet()) {
                double lat = ESEntry.getValue().get(0);
                double lon = ESEntry.getValue().get(1);
                double ESCost = ESEntry.getValue().get(2);
                double networkType = ESEntry.getValue().get(3);

                String s = networkType == 0.0 ? "Microwave Network" : "Optic Fiber Network";

                countOfMicrowaveNetworks += networkType == 0.0 ? 1 : 0;
                countOfOpticNetworks += networkType == 1.0 ? 1 : 0;

                // get user count
                int servedUsers = entry.getValue().size();
                countOfUsersServed += servedUsers;
                if (servedUsers != 0) {
                    countOfESInUse++;
                }

                System.out.printf("ES_ID at (%.3f, %.3f) is a/an %s and costs $%,.2f and is servicing %d users \n", 
                    lat, lon, s, ESCost, servedUsers);
            }
        }
        return List.of(countOfESInUse, countOfUsersServed, countOfMicrowaveNetworks, countOfOpticNetworks);
    }
    public static class SatComClusters {
        /**
        * Version 3.0:
        * The user count is hardcoded, the location and data usage of each user are randomly generated.
        * The earth station locations are to be computed based on the clustering of users
        * A network is established only if its cost effective
         */

        /* Find the middle point on a line between two points */
        private static List<Double> computeMidpoint(double x1, double y1, double x2, double y2) {
            double x_m = computeAverage(x1, x2);
            double y_m = computeAverage(y1, y2);
            
            return List.of(x_m, y_m);
        }

        /** Finds the furthest two users within the cluster */
        private static List<Double> getClusterDiameter(List<Integer> cluster, Map<Integer, List<Double>> P) {
            double biggestDistance = 0.0;
            List<Double> arr = new ArrayList<>();
            for (int i = 0; i < cluster.size(); i++) {
                // reset for each user
                biggestDistance = Double.MIN_VALUE;

                double x1 = P.get(cluster.get(i)).get(0);
                double y1 = P.get(cluster.get(i)).get(1);

                for (int j = i + 1; j < cluster.size(); j++) {
                    double x2 = P.get(cluster.get(j)).get(0);
                    double y2 = P.get(cluster.get(j)).get(1);

                    double distance = computeGCD(x1, y1, x2, y2);

                    if (distance > biggestDistance) {
                        arr = new ArrayList<>(computeMidpoint(x1, y1, x2, y2));

                        biggestDistance = distance;
                    }            
                }
            }

            arr.add(biggestDistance);
            return arr;
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
        private static List<Integer> retrieveCluster(Map.Entry<Integer,List<Integer>> clusterKeyValuePair) {
            List<Integer> cluster = clusterKeyValuePair.getValue();
            cluster.add(clusterKeyValuePair.getKey());
            return cluster;
        }

        /* Creates a cluster for each user if the user has nearby users within a serviceable radius */
        private static Map<Integer, List<Integer>> createCluters(Map<Integer, List<Double>> P) {
            // Holds the key of each user that has a nearby user / cluster of users
            Map<Integer, List<Integer>> clustersPerUser = new LinkedHashMap<>();
            
            // for each user
            for (Map.Entry<Integer, List<Double>> currentUser : P.entrySet()) {
                // initialize a collection for the keys of the users near the current user
                List<Integer> nearbyUsers = new ArrayList<>();
                // iterate over P
                for (Map.Entry<Integer, List<Double>> users : P.entrySet()) {
                    if (!currentUser.getKey().equals(users.getKey())) {
                        // Compute distance between each user
                        double distance = computeGCD(currentUser.getValue().get(0), currentUser.getValue().get(1), 
                                                    users.getValue().get(0), users.getValue().get(1));

                        // if the distance between is within the serviceable range of an antenna add them to a list of nearby users
                        // REWORK THIS
                        if (isWithinMicrowaveHopDistance(distance)) {
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

        /** 
         * Iterates over each cluster, finds the midpoint coordinates of the biggest diameter for each cluster, 
         * creates an ES at the midpoint of each cluster, computes the cost for both a Microwave and Optic Network within each cluster,
         * and checks which network is more cost-effective
         * Returns a map of:
         *  - Keys -> Map -> Earth Station Key, List of double values representing coordinates, cost and network type
         *  - Values -> Map -> User_ID, double value representing the user data usage in Mbps
         */
        private static Map<Map<Integer, List<Double>>, Map<Integer, Double>> mostCostEffectiveNetwork(Map<Integer, List<Integer>> clustersPerUser, 
            Map<Integer, List<Double>> P) {
        
            Map<Map<Integer, List<Double>>, Map<Integer, Double>> networks = new LinkedHashMap<>();
            
            // iterate over the clusters
            for (Map.Entry<Integer, List<Integer>> clusterInfo : clustersPerUser.entrySet()) {
                Map<Integer, List<Double>> C = new HashMap<>();
                // Get all users from the cluster
                List<Integer> clusterUsers = retrieveCluster(clusterInfo);

                List<Double> arr = getClusterDiameter(clusterUsers, P);
                Double clusterDiameter = arr.get(2);
                List<Double> coordinates = new ArrayList<>(List.of(arr.get(0), arr.get(1)));

                // create an ES with the ESKey and best coordinates
                C.put(1, coordinates);

                double microwaveCost = computeMicrowaveCost(clusterUsers, C,  P);
                double opticFCost = computeOpticCost(clusterUsers, C, P);

                if (microwaveCost > opticFCost && isCostEfficient(opticFCost)) {
                    // add the network cost to the coordinates of the ES
                    C.get(1).add(opticFCost);
                    // 1.0 to denote that it is an optic fiber network
                    C.get(1).add(1.0); 
                    networks.put(C, MapOfUsersAndUsage(clusterUsers, P));
                } else if (opticFCost > microwaveCost && isCostEfficient(microwaveCost)) {
                    if(isWithinMicrowaveCapacity(clusterUsers) && isWithinMicrowaveHopDistance(clusterDiameter)) {
                        C.get(1).add(microwaveCost);
                        // 0.0 to denote that it is a microwave network
                        C.get(1).add(0.0);
                        networks.put(C, MapOfUsersAndUsage(clusterUsers, P));
                    }
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
            Map<Integer, List<Integer>> clustersPerUser = createCluters(P);

            // build networks
            Map<Map<Integer, List<Double>>, Map<Integer, Double>> networks = mostCostEffectiveNetwork(clustersPerUser, P);
            List<Integer> counts = displayPC(networks);
            System.out.printf("\nThere are %,d ES in use out of %,d, and they are serving %,d users out of %,d\n", counts.get(0), networks.size(), counts.get(1), P.size());
            System.out.printf("There are %d Microwave Networks and %d Optic Networks.\n", counts.get(2), counts.get(3));
            return networks;
        }
    }

    public static void main(String[] args) {
        // number of users
        int n = 20_000;
        // Create a Dictionary of users P with random coordinates (lat, long) and outgoing communication volume (weight for i-th user)
        Map<Integer, List<Double>> P = createRandomUsers(n);
        String message = "User_ID %d is at :(%.3f:, %.3f): and accumulates %.3f GB of traffic";
        DisplayInfo(P, message);

        SatComClusters scs = new SatComClusters();
        Map<Map<Integer, List<Double>>, Map<Integer, Double>> _ = scs.buildNetwork(P);
    }
}
