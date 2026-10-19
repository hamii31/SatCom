import java.util.*;

/**
 * Computational implementation of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 */

public class SatCom {
    /** Helper / Shared functions and constants */
    final static double EARTH_RADIUS = 6_371.01;
    // effective-earth factor
    final static double K_FACTOR = 4 / (double) 3;
    // the typical serviceable range of an L-band terrestrial station, with an average height of 50m and average power around 1KW
    final static double ANTENNA_HEIGHT = 50.0; // in meters
    // receiver antenna height in meters
    final static double RX_HEIGHT = 2.0; 

    /* Computes the radio horizon distance accounting for the K factor and Earth radius */
    public static double radioHorizon(double h) {
        return Math.sqrt(2.0 * K_FACTOR * EARTH_RADIUS * (h / 1000.0));
    } 

    // serviceable radius in km
    final static double SERVICEABLE_RADIUS = radioHorizon(ANTENNA_HEIGHT) + radioHorizon(RX_HEIGHT);

    /**
     * Returns a map of users at randomly generated locations with randomly generated data traffic
     */
    private static Map<Integer, List<Double>> createRandomUsers(int n) {
        Map<Integer, List<Double>> P = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            // generate a random double value between -90 and 90 degrees for the latitude
            double latitude = -90 + (Math.random() * 180);
            // generate a random double value between -180 and 180 degrees for the longtitude 
            double longitude = -180 + (Math.random() * 360);
            // generate a random double value between 1 to 999
            double dataTraffic = 1 + Math.random() * 999;

            P.put(i + 1, List.of(latitude, longitude, dataTraffic));
        }
        return P;
    }
    
    private static double convertDegreesToRadians(double degrees) {
        return degrees * (Math.PI / 180);
    }

    /* Computes the great-circle distance between two points on a sphere (Earth) */
    private static double computeGCD(double x1, double y1, double x2, double y2) {

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

    /** Checks if the user is within serviceable distance */
    public static boolean isWithinServiceableDistance(double distance) {
        return distance <= SERVICEABLE_RADIUS;
    }

    /** Computes the cost of data transmission in GBs */
    private static double computeCost(double traffic) {
        // an price range for satcom is $0.2 - $1.5 per GB as per 2026
        // we will use the average price
        final double costPerGB = 0.85;
        return traffic * costPerGB;
    }

    public static double computeAverage(double a, double b) {
        return (a + b) / (double) 2;
    }

    /* A reusable helper function for printing out Dictionaries of variable Number data types with a custom message */
    private static <T extends Number> void DisplayInfo(Map<Integer, List<T>> collection, String message) {
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
    private static List<Integer> displayPC(Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> PC) {
        int countOfESInUse = 0;
        int countOfUsersServed = 0;

        double totalCapital = 0.0;
        for (Map.Entry<Map<Integer, List<Double>>, List<Map<Integer, Double>>> entry : PC.entrySet()) {
            // for each station
            Map<Integer, List<Double>> ES = entry.getKey();
            for (Map.Entry<Integer, List<Double>> ESEntry : ES.entrySet()) {
                // get the ID and location
                int ESKey = ESEntry.getKey();
                double lat = ESEntry.getValue().get(0);
                double lon = ESEntry.getValue().get(1);

                // get user count
                int servedUsers = entry.getValue().size();
                if (servedUsers != 0) {
                    countOfESInUse++;
                }

                System.out.printf("ES_ID %d (%.3f, %.3f) is servicing %d users: \n", 
                    ESKey, lat, lon, servedUsers);

                double monthlyTotal = 0.0;
                // get the list of user maps for the current station
                List<Map<Integer, Double>> userMaps = entry.getValue();
                int userCount = userMaps.size();

                countOfUsersServed += userCount;
    
                // iterate over the list
                for (int i = 0; i < userCount; i++) {
                    // get the map of the user at index i in userMaps
                    Map<Integer, Double> user = userMaps.get(i);
                    // iterate through the user map
                    for (Map.Entry<Integer, Double> userEntry : user.entrySet()) {
                        // count monthly total for the station
                        monthlyTotal += userEntry.getValue();
                        System.out.printf("\tUser_ID %d - $%,.2f\n", userEntry.getKey(), userEntry.getValue());
                    }
                    System.out.printf("Monthly total for ES_ID %d - $%,.2f\n", ESKey, monthlyTotal);
                    // count total for all stations
                    totalCapital += monthlyTotal;
                }
            }
        }
        System.out.printf("Total capital - $%,.2f\n", totalCapital);
        return List.of(countOfESInUse, countOfUsersServed);
    }

    public static class SatCom1 {
        /**
        * Version 1.0:
        * The user count is hardcoded, the location and data usage of each user are randomly generated.
        * The earth station count is hardcoded, the location of each ES is randomly generated.
        * The cost function computes data usage (communication inflow and outflow) per GB using the latest pricing for
        * SatCom communication.
         */

        /**
         * Returns a map of earth stations at randomly generated locations
         */
        private static Map<Integer, List<Double>> createRandomEarthStations(int m) {
            Map<Integer, List<Double>> C = new LinkedHashMap<>();
            for (int i = 0; i < m; i++) {
                double latitude = -90 + (Math.random() * 180);
                double longitude = -180 + (Math.random() * 360);

                C.put(i + 1, List.of(latitude, longitude));
            }
            return C;
        }

        /** 
        * Iteratively computes the distance between each user and each earth station 
        * Returns a complex map with:
        *  - Key -> Map -> Earth Station Key, List of double values representing the coordinates of the Earth Station
        *  - Value -> List of Maps -> User_ID, double value representing the user data traffic in GBs
        */
        public static Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> computeGCDforEachUserAndES(Map<Integer, List<Double>> P, 
            Map<Integer, List<Double>> C) {
            
            Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> PC = new LinkedHashMap<>();

            // For each user
            for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
                double ESLat = ESInfo.getValue().get(0);
                double ESLong = ESInfo.getValue().get(1);

                // initialize an empty list of user maps to hold the serviceable users for the current ES
                List<Map<Integer, Double>> tempUserList = new ArrayList<>();
                for (Map.Entry<Integer, List<Double>> userInfo : P.entrySet()) { 
                    double userLat =  userInfo.getValue().get(0);
                    double userLong = userInfo.getValue().get(1);

                    // compute the dsitance of the user to the station
                    double distance = computeGCD(userLat, userLong, ESLat, ESLong);
                    // if the user is within the serviceable range of the current ES
                    if (isWithinServiceableDistance(distance)) {
                        Map<Integer, Double> tempUser = new HashMap<>();
                        tempUser.put(userInfo.getKey(), computeCost(userInfo.getValue().get(2)));
                        tempUserList.add(tempUser);
                    }
                }
                // assign the current station and append the user map list related to that station in the PC collection
                PC.put(Map.of(ESInfo.getKey(), List.of(ESLat, ESLong)), tempUserList);
            }

            return PC;
        }
        /* Create a Dictionary for each station and the users that fall within its serviceable range  */
        public static Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> buildPC (Map<Integer, List<Double>> P, Map<Integer, List<Double>> C) {
            return computeGCDforEachUserAndES(P, C);
        }

        /** Generates m Earth Stations in random locations */
        public Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> buildRandomizedPC(Map<Integer, List<Double>> P, int m) {
            // Create a Dictionary of random ES locations
            Map<Integer, List<Double>> C = createRandomEarthStations(m);
            String message = "ES_ID %d is online at :(%.3f:, %.3f)";
            DisplayInfo(C, message);

            // Create a Dictionary CPC for each earth station and the users connected to it, where the key
            // is the ES Id, and the value is a Dictionary of the User Id and the cost associated to servicing that user
            Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> rPC = buildPC(P, C);

            List<Integer> counts = displayPC(rPC);
            System.out.print("\n");
            System.out.printf("There are %d ES in use out of %d, and they are serving %d users out of %d\n", counts.get(0), m, counts.get(1), P.size());
            return rPC;
        }

    }

    public static class SatCom2 {
        /**
        * Version 2.0 states:
        * The user count is hardcoded, the location and data usage of each user are randomly generated.
        * The earth station count and locations are to be computed based on the clustering of users
        * The cost function computes data usage (communication inflow and outflow) per GB using the latest pricing for
        * SatCom communication.
         */

        private static int findMostCommonUserByIndex(List<Integer> list) {
            int size = list.size();
            // when there are two 
            int mostOccuring = list.get(0);
            int maxOccurance = 0;
            for (int i = 0; i < size; i++) {
                int occurence = 0;
                for (int j = i + 1; j < size; j++) {
                    if (list.get(i).equals(list.get(j))) {
                        occurence++;
                    }
                }

                if (occurence > maxOccurance) {
                    maxOccurance = occurence;
                    mostOccuring = list.get(i);
                }
            }

            return mostOccuring;
        }

        /** Finds the closest user to the current one for each user within a cluster*/
        private static List<Integer> computeClosestUsers(int size, List<Integer> cluster, Map<Integer, List<Double>> P) {
            List<Integer> closestUsers = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                int closestUser = -1;
                double smallestDistance = Double.MAX_VALUE;

                for (int j = i + 1; j < size; j++) {
                    double distance = computeGCD(P.get(cluster.get(i)).get(0), P.get(cluster.get(i)).get(1), 
                                                P.get(cluster.get(j)).get(0), P.get(cluster.get(j)).get(1));

                    if (distance < smallestDistance) {
                        // assign a new smallest distance and assign a new "closestUser" 
                        smallestDistance = distance;
                        closestUser = cluster.get(j);
                    }            
                }
                    
                // closestUser is -1 when i + 1 is beyond the scope of cluster
                if (closestUser == -1) {
                    break;
                } else {
                    // collect the closest user for this iteration
                    closestUsers.add(closestUser);
                }
            }
            return closestUsers;
        }

        /** Retrieve coordinates of best match */
        private static List<Double> retrieveBestLocationForCurrentCluster(int indexOfBestLocation, Map<Integer, List<Double>> P) {
            List<Double> coordinates = new ArrayList<>(P.get(indexOfBestLocation));
            // remove the data traffic param
            coordinates.remove(2);
            return coordinates;
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
        private static List<Map<Integer, Double>> listOfMapOfUsersAndTraffic(List<Integer> cluster, Map<Integer, List<Double>> P) {
             List<Map<Integer, Double>> usersAndTraffics = new ArrayList<>();

            for (int i = 0; i < cluster.size(); i++) {
                // Create a new map for each user
                Map<Integer, Double> userAndTraffic = new HashMap<>();
                userAndTraffic.put(cluster.get(i), getTrafficOfUserInCluster(cluster, P, i));
                usersAndTraffics.add(userAndTraffic);
            }
            return usersAndTraffics;
        }

        /* Find the middle point on a line between two points */
        private static List<Double> computeMidpoint(List<Integer> cluster, Map<Integer, List<Double>> P) {
            double x_m = computeAverage(P.get(cluster.get(0)).get(0), P.get(cluster.get(1)).get(0)); // x1, x2
            double y_m = computeAverage(P.get(cluster.get(0)).get(1), P.get(cluster.get(1)).get(1)); // y1, y2
            
            return List.of(x_m, y_m);
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
                        if (isWithinServiceableDistance(distance)) {
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
            String message = "User_ID %d has a cluster with :User_ID %d ";
            DisplayInfo(clustersPerUser, message);
            System.out.printf("%d clusters formed from the %,d users\n", clustersPerUser.size(), P.size());

            return clustersPerUser;   
        }

        /** 
         * Iterates over the clusters of users and finds the best location for an earth station for each cluster
         * Returns a complex map with:
         *  - Key -> Map -> Earth Station Key, List of double values representing coordinates of Earth Station
         *  - Value -> List of Maps -> User_ID, double value representing the user data traffic in GBs
         */
        private static Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> bestLocationsInClusters(Map<Integer, List<Integer>> clustersPerUser, 
            Map<Integer, List<Double>> P) {
        
            Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> cPC = new LinkedHashMap<>();
            
            int ESKey = 1;
            // iterate over the clusters
            for (Map.Entry<Integer, List<Integer>> clusterInfo : clustersPerUser.entrySet()) {
                Map<Integer, List<Double>> C = new HashMap<>();
                // Get all users from the cluster
                List<Integer> clusterUsers = retrieveCluster(clusterInfo);
                int size = clusterUsers.size();

                // no need for iterative operations when there are only 2 users
                if (size == 2) {
                    // compute the midpoint between the two users within the cluster
                    List<Double> coordinates = computeMidpoint(clusterUsers, P);
                    // use the ESKey and the midpoint coordinates to build the ES for the cluster
                    C.put(ESKey++, coordinates);
                    // assign the two users to the ES
                    cPC.put(C, listOfMapOfUsersAndTraffic(clusterUsers, P));
                    continue;
                }

                // Retrieve the users that are closest to most other users
                List<Integer> closestUsers = computeClosestUsers(size, clusterUsers, P);
                // From them, get the most occurring user
                int mostOccurring = findMostCommonUserByIndex(closestUsers);
                // that user's location is chosen as the best location for an ES
                List<Double> coordinates = retrieveBestLocationForCurrentCluster(mostOccurring, P);
                // create an ES with the ESKey and best coordinates
                C.put(ESKey++, coordinates);
                // for that ES, assign the users within the current cluster
                cPC.put(C, listOfMapOfUsersAndTraffic(clusterUsers, P));
            }

            return cPC;
        }

        /**
         * Creates user clusters
         * Returns a map of earth station maps, each holding a list of user maps as a value
         */
        public Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> buildClusterBasedPC(Map<Integer, List<Double>> P) {
            // Create clusters for each user, if applicable 
            Map<Integer, List<Integer>> clustersPerUser = createCluters(P);

            // build CPC
            Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> cPC = bestLocationsInClusters(clustersPerUser, P);
            List<Integer> counts = displayPC(cPC);
            System.out.printf("\nThere are %d ES in use out of %d, and they are serving %d users out of %d\n", counts.get(0), cPC.size(), counts.get(1), P.size());
            return cPC;
        }
    }

    public static void main(String[] args) {
        // number of users
        int n = 10_000;
        // Create a Dictionary of users P with random coordinates (lat, long) and outgoing communication volume (weight for i-th user)
        Map<Integer, List<Double>> P = createRandomUsers(n);
        String message = "User_ID %d is at :(%.3f:, %.3f): and accumulates %.3f GB of traffic";
        DisplayInfo(P, message);

        SatCom1 sc1 = new SatCom1();
        Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> _ = sc1.buildRandomizedPC(P, 1_000);
        
        // TO-DO: Less stations, faster
        SatCom2 sc2 = new SatCom2();
        Map<Map<Integer, List<Double>>, List<Map<Integer, Double>>> _ = sc2.buildClusterBasedPC(P);
    }
}
