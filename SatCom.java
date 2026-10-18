import java.util.*;

/**
 * Computational implementation of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 */

public class SatCom {
    // the typical serviceable range of an L-band terrestrial station, with an average height of 50m and average power around 1KW
    final static double ANTENNA_HEIGHT = 50.0; // in meters
    // compute the horizon distance accounting for the Earth athmospheric refraction factor
    final static double SERVICEABLE_RADIUS = 4.12 * Math.sqrt(ANTENNA_HEIGHT);

    public static class SatCom1 {
        /**
        * Version 1.0:
        * The user count is hardcoded, the location and data usage of each user are randomly generated.
        * The earth station count is hardcoded, the location of each ES is randomly generated.
        * The cost function computes data usage (communication inflow and outflow) per GB using the latest pricing for
        * SatCom communication.
         */

        /**
         * Builds a Dicionary with unique keys and a collection of variables from the double
         * data type holding the location of each earth station
         */
        private static Map<Integer, List<Double>> createRandomEarthStations(int m) {
            Map<Integer, List<Double>> C = new HashMap<>();
            for (int i = 0; i < m; i++) {
                double latitude = -90 + (Math.random() * 180);
                double longitude = -180 + (Math.random() * 360);

                C.put(i + 1, List.of(latitude, longitude));
            }
            return C;
        }

        public Map<Integer, Map<Integer, Double>> buildRandomizedCPC(Map<Integer, List<Double>> P) {
            // number of earth stations (ES)
            int m = 1000;
            // Create a Dictionary of random ES locations
            Map<Integer, List<Double>> C = createRandomEarthStations(m);
            String message = "ES_ID %d is online at :(%.3f:-%.3f)";
            DisplayInfo(C, message);

            // Create a Dictionary CPC for each earth station and the users connected to it, where the key
            // is the ES Id, and the value is a Dictionary of the User Id and the cost associated to servicing that user
            Map<Integer, Map<Integer, Double>> rPC = buildCPC(P, C);

            int countOfESInUse = displayPC(rPC);
            System.out.print("\n");
            System.out.printf("There are %d ES in use out of %d for %d users\n", countOfESInUse, m, P.size());
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

         /* Creates a cluster for each user if the user has nearby users within a specified serviceable radius */
        private static Map<Integer, List<Integer>> createCluters(Map<Integer, List<Double>> P) {
            // Holds the key of each user that has a nearby user / cluster of users
            Map<Integer, List<Integer>> clustersPerUser = new HashMap<>();
            
            // for each user
            for (Map.Entry<Integer, List<Double>> currentUser : P.entrySet()) {
                List<Integer> nearbyUsers = new ArrayList<>();
                for (Map.Entry<Integer, List<Double>> users : P.entrySet()) {
                    if (!currentUser.getKey().equals(users.getKey())) {
                        // Compute gcd between each user
                        double distance = computeGCD(currentUser.getValue().get(0), currentUser.getValue().get(1), 
                                                    users.getValue().get(0), users.getValue().get(1));

                        // if the distance between the two users is within the serviceable range of an antenna
                        if (isWithinServiceableDistance(distance)) {
                            // add them to a list of nearby users
                            nearbyUsers.add(users.getKey());
                        }
                    }
                }
                    
                // simply do not create an instance in the Dictionary if the current user has no nearby users
                if (!nearbyUsers.isEmpty()) {
                    // create a key-value pair with the current user's ID as a key, and the nearby users as values
                    clustersPerUser.put(currentUser.getKey(), nearbyUsers);
                }
            }
            return clustersPerUser;   
        }
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

        /* Creates a list, appends the value and the key from the key-value pair to it, returns the list */
        private static List<Integer> retrieveCluster(Map.Entry<Integer,List<Integer>> clusterKeyValuePair) {
            List<Integer> cluster = clusterKeyValuePair.getValue();
            cluster.add(clusterKeyValuePair.getKey());
            return cluster;
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
                    // System.out.printf("The closest user to User_Id %d is User_Id %d\n", cluster.get(i), cluster.get(cluster.indexOf(closestUser)));
                }
            }
            return closestUsers;
        }

        /** Retrieve coordinates of best match */
        private static List<Double> retrieveBestLocationForCurrentCluster(int indexOfBestLocation, Map<Integer, List<Double>> P) {
            List<Double> coordinates = new ArrayList(P.get(indexOfBestLocation));
            // remove the data traffic param
            coordinates.remove(2);
            return coordinates;
        }

        private static double computeAverage(double a, double b) {
            return (a + b) / (double) 2;
        }

        /* Find the middle point on a line between two points */
        private static List<Double> computeMidpoint(List<Integer> cluster, Map<Integer, List<Double>> P) {
            double x_m = computeAverage(P.get(cluster.get(0)).get(0), P.get(cluster.get(1)).get(0)); // x1, x2
            double y_m = computeAverage(P.get(cluster.get(0)).get(1), P.get(cluster.get(1)).get(1)); // y1, y2
            
            return List.of(x_m, y_m);
        }

        /** Retrieve the data usage of a user based on index */
        public static double getUserDataUsageBasedOnIndexInCluster(List<Integer> cluster, Map<Integer, List<Double>> P, int index) {
            return P.get(cluster.get(index)).get(2);
        }

        /** Builds a Dictionary of ES based on the most lucrative location for each cluster of users */
        private static Map<Integer, List<Double>> buildEAAtTheBestLocations(Map<Integer, List<Integer>> clustersPerUser, 
            Map<Integer, List<Double>> P) {

            Map<Integer, List<Double>> C = new HashMap<>();
            
            int ESKey = 1;
            for (Map.Entry<Integer, List<Integer>> clusterInfo : clustersPerUser.entrySet()) {
                // Get all users from the cluster
                List<Integer> cluster = retrieveCluster(clusterInfo);
                int size = cluster.size();

                // no need for iterative operations when there are only 2 users
                if (size == 2) {
                    // the best coordinates will be the midpoint between the two points
                    List<Double> coordinates = computeMidpoint(cluster, P);
                    C.put(ESKey++, coordinates);
                    continue;
                }

                // holds the closest user from each iteration
                List<Integer> closestUsers = computeClosestUsers(size, cluster, P);
                // the closest user to the most users is where the ES must be placed
                int mostOccuring = findMostCommonUserByIndex(closestUsers);

                // get best coordinates for ES for current cluster
                List<Double> coordinates = retrieveBestLocationForCurrentCluster(mostOccuring, P);

                // build an ES and append it to C
                C.put(ESKey++, coordinates);
            }

            return C;
        }

        public Map<Integer, Map<Integer, Double>> buildClusterBasedCPC(Map<Integer, List<Double>> P) {
            // Create clusters for each user, if applicable 
            Map<Integer, List<Integer>> clustersPerUser = createCluters(P);

            // Display the clusters
            String message = "User_ID %d has a cluster with :User_ID %d ";
            DisplayInfo(clustersPerUser, message);
            System.out.printf("%d clusters formed from the %,d users\n", clustersPerUser.size(), P.size());

            // Build a collection of custom Earth Stations C
            Map<Integer, List<Double>> C = buildEAAtTheBestLocations(clustersPerUser, P);
            message = "ES_ID %d is online at :(%.3f:-%.3f)";
            DisplayInfo(C, message);
            
            // build CPC
            Map<Integer, Map<Integer, Double>> cPC = buildCPC(P, C);
            int countOfESInUse = displayPC(cPC);
            System.out.print("\n");
            System.out.printf("There are %d ES in use out of %d for %d users\n", countOfESInUse, C.size(), P.size());
            return cPC;
        }
    }

    /** HELPER / SHARED FUNCTIONS */
    /**
     * Builds a Dictionary with unique keys and a collection of variables from the double type 
     * holding the geographical location and communication volume in gigabytes for each user
     */
    private static Map<Integer, List<Double>> createRandomUsers(int n) {
        Map<Integer, List<Double>> P = new HashMap<>();
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
        final double EARTH_RADIUS = 6_371.01;

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

    /** Creates a Dictionary of Dictionaries with keys and empty Dictionaries as values 
     * Takes Map<Integer, List<T>> keyMotherDict, from which it takes only the keys and copies them to keyReceiverDict
    */
    public static <T extends Number> Map<Integer, Map<Integer, T>> buildEmptyDictionaryOfDictionaries(Map<Integer, List<T>> keyMotherDict) {
        Map<Integer, Map <Integer, T>> keyReceiverDict = new HashMap<>();
        
        for (Map.Entry<Integer, List<T>> entry : keyMotherDict.entrySet()) {
            keyReceiverDict.put(entry.getKey(), new HashMap<>());
        }
        return keyReceiverDict;
    }

    /** Computes the cost of data transmission in GBs */
    private static double computeCost(double traffic) {
        // an price range for satcom is $0.2 - $1.5 per GB as per 2026
        // we will use the average price
        final double costPerGB = 0.85;
        return traffic * costPerGB;
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

    /** Iteratively compute the distance between each user and each earth station */
    public static  Map<Integer, Map<Integer, Double>> computeGCDforEachUserAndES(Map<Integer, List<Double>> P, 
        Map<Integer, List<Double>> C, Map<Integer, Map<Integer, Double>> PC ) {
                
        // For each user
        for (Map.Entry<Integer, List<Double>> userInfo : P.entrySet()) {
            double userLat =  userInfo.getValue().get(0);
            double userLong = userInfo.getValue().get(1);

            // Compute the distance from each ES
            for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
                double ESLat = ESInfo.getValue().get(0);
                double ESLong = ESInfo.getValue().get(1);

                double distance = computeGCD(userLat, userLong, ESLat, ESLong);
                // if the user is within the serviceable range of the current ES
                if (isWithinServiceableDistance(distance)) {
                    // get the key of the that ES
                    int ESKey = ESInfo.getKey();

                    // check if the ESkey exists in CPC
                    if (PC.containsKey(ESKey)) {
                        // compute cost (wi, Pi, Ck) and assign the user and their cost to the relevant ES within CPC
                        PC.get(ESKey).put(userInfo.getKey(), computeCost(userInfo.getValue().get(2)));
                    }
                }
            }
        }

        return PC;
    }
    /* Create a Dictionary for each station and the users that fall within its serviceable range  */
    public static Map<Integer, Map<Integer, Double>> buildCPC (Map<Integer, List<Double>> P, Map<Integer, List<Double>> C) {
        // Holds the keys of each ES and the Users associated to it
        Map<Integer, Map<Integer, Double>> PC = buildEmptyDictionaryOfDictionaries(C);

        PC = computeGCDforEachUserAndES(P, C, PC);

        return PC;
    }
   
    /* Displays every ES and the users being serviced by it */
    private static int displayPC(Map<Integer, Map<Integer, Double>> PC) {
        int countOfESInUse = 0;
        double totalCapital = 0.0;
        for (Map.Entry<Integer, Map<Integer, Double>> entry : PC.entrySet()) {
            int ESKey = entry.getKey();
            int servedUsers = entry.getValue().size();
            if (servedUsers != 0) {
                countOfESInUse++;
            }

            System.out.printf("ES_ID %d is servicing %d users: \n", 
                ESKey, servedUsers);

            double monthlyTotal = 0.0;
            Map<Integer, Double> user = entry.getValue();
            for (Map.Entry<Integer, Double> userEntry : user.entrySet()) {
                monthlyTotal += userEntry.getValue();
                System.out.printf("\tUser_ID %d - $%,.2f\n", userEntry.getKey(), userEntry.getValue());
            }
            System.out.printf("Monthly total for ES_ID %d - $%,.2f\n", ESKey, monthlyTotal);
            totalCapital += monthlyTotal;
        }
        System.out.printf("Total capital - $%,.2f\n", totalCapital);
        return countOfESInUse;
    }

    public static void main(String[] args) {
        // number of users
        int n = 10_000;
        // Create a Dictionary of users P with random coordinates (lat, long) and outgoing communication volume (weight for i-th user)
        Map<Integer, List<Double>> P = createRandomUsers(n);
        String message = "User_ID %d is at :(%.3f:-%.3f): and accumulates %.3f GB of traffic";
        DisplayInfo(P, message);

        SatCom1 sc1 = new SatCom1();
        Map<Integer, Map<Integer, Double>> _ = sc1.buildRandomizedCPC(P);
        
        SatCom2 sc2 = new SatCom2();
        Map<Integer, Map<Integer, Double>> _ = sc2.buildClusterBasedCPC(P);
    }
}
