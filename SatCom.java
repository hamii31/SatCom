import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computational implementation of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 * Version 1.0 states:
 * The user count is hardcoded, the location and data usage of each user are randomly generated.
 * The earth station count is hardcoded, the location of each ES is randomly generated.
 * The cost function computes data usage (communication inflow and outflow) per GB using the latest pricing for
 * SatCom communication.
 * 
 * Version 2.0 states:
 * The user count is hardcoded, the location and data usage of each user are randomly generated.
 * The earth station count and locations are to be computed based on the clustering of users
 * The cost function computes data usage (communication inflow and outflow) per GB using the latest pricing for
 * SatCom communication.
 */

public class SatCom {
    // the typical serviceable range of an L-band terrestrial station, with an average height of 50m and average power around 1KW
    final static double ANTENNA_HEIGHT = 50.0; // in meters
    // compute the horizon distance accounting for the Earth athmospheric refraction factor
    final static double SERVICEABLE_RADIUS = 4.12 * Math.sqrt(ANTENNA_HEIGHT);

    /**
     * Builds a Dictionary with unique keys and a collection of variables from the double type 
     * holding the geographical location and communication volume in gigabytes for each user
     */
    private static Map<Integer, List<Double>> createUsers(int n) {
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

    /**
     * Builds a Dicionary with unique keys and a collection of variables from the double
     * type holding the location of each earth station
     */
    private static Map<Integer, List<Double>> createEarthStations(int m) {
        Map<Integer, List<Double>> C = new HashMap<>();
        for (int i = 0; i < m; i++) {
            double latitude = -90 + (Math.random() * 180);
            double longitude = -180 + (Math.random() * 360);

            C.put(i + 1, List.of(latitude, longitude));
        }
        return C;
    }

    /**
     * Computes the great-circle distance between two points on a sphere (Earth)
     */
    private static double computeGCD(double x1, double y1, double x2, double y2) {
        final double earthRadius = 6_371.01;

        // Convert degrees to radians
        x1 = x1 * (Math.PI / 180);
        y1 = y1 * (Math.PI / 180);
        x2 = x2 * (Math.PI / 180);
        y2 = y2 * (Math.PI / 180);

        // Compute the great circle distance between the two points
        double gcd = earthRadius * Math.acos(Math.sin(x1) * Math.sin(x2) 
                                            + Math.cos(x1) * Math.cos(x2)
                                            * Math.cos(y1 - y2));

        return gcd;
    }

    private static double computeCost(double traffic) {
        // an price range for satcom is $0.2 - $1.5 per GB as per 2026
        // we will use the average price
        final double costPerGB = 0.85;
        return traffic * costPerGB;
    }

   /**
    * Iteratively compute the distance between each user and each earth station
    * Create a Dictionary for each station and the users that fall within its serviceable range
    * by comparing the gcd of every individual user and each ES
    */
    private static Map<Integer, Map<Integer, Double>> computeGCDforEachUserAndES (Map<Integer, List<Double>> P, Map<Integer, List<Double>> C) {
        // Holds the keys of each ES and the Users associated to it
        Map<Integer, Map<Integer, Double>> CPC = new HashMap<>();
        // Append all keys with empty lists
        for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
            CPC.put(ESInfo.getKey(), new HashMap<>());
        }

        // For each user
        for (Map.Entry<Integer, List<Double>> userInfo : P.entrySet()) {
            double userLat = userInfo.getValue().get(0);
            double userLong = userInfo.getValue().get(1);

            // Compute the distance from each ES
            for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
                double ESLat = ESInfo.getValue().get(0);
                double ESLong = ESInfo.getValue().get(1);

                double distance = computeGCD(userLat, userLong, ESLat, ESLong);
                // if the user is within the serviceable range of the current ES
                if (distance <= SERVICEABLE_RADIUS) {
                    // get the key of the that ES
                    int ESKey = ESInfo.getKey();

                    // check if the ESkey exists in CPC
                    if (CPC.containsKey(ESKey)) {
                        // compute cost (wi, Pi, Ck) and assign the user and their cost to the relevant ES within CPC
                        CPC.get(ESKey).put(userInfo.getKey(), computeCost(userInfo.getValue().get(2)));
                    }
                }
            }
        }
        return CPC;
    }

    /**
     * A reusable helper function for printing out entire Dictionaries in a customizeable format
     */
    private static void DisplayInfoI(Map<Integer, List<Double>> collection, String message) {
        String[] messages = message.split(":");

        for (Map.Entry<Integer, List<Double>> info : collection.entrySet()) {
            System.out.printf(messages[0], info.getKey());
            
            List<Double> values = info.getValue();
            for (int i = 0; i < values.size(); i++) {
                System.out.printf(messages[i + 1], values.get(i));
            }
            System.out.print("\n");
        }
    }

    private static void DisplayInfoII(Map<Integer, List<Integer>> collection, String message) {
        String[] messages = message.split(":");

        for (Map.Entry<Integer, List<Integer>> info : collection.entrySet()) {
            System.out.printf(messages[0], info.getKey());
            
            List<Integer> values = info.getValue();
            for (int i = 0; i < values.size(); i++) {
                if (values.size() < messages.length)
                    System.out.printf(messages[i + 1], values.get(i));
                else
                    System.out.printf(messages[1], values.get(i));
            }
            System.out.print("\n");
        }
    }

    public static void main(String[] args) {
        final int VERSION = 2;

        // number of users
        int n = 10_000;
        // Create a Dictionary of users P with random coordinates (lat, long) and outgoing communication volume (weight for i-th user)
        Map<Integer, List<Double>> P = createUsers(n);
        String message = "User_ID %d is at :(%.3f:-%.3f): and accumulates %.3f GB of traffic";
        DisplayInfoI(P, message);

        if (VERSION == 1) {

            // number of earth stations (ES)
            int m = 1000;
            // Create a Dictionary of ES locations
            Map<Integer, List<Double>> C = createEarthStations(m);
            message = "ES_ID %d is online at :(%.3f:-%.3f)";
            DisplayInfoI(C, message);

            // Create a Dictionary CPC for each earth station and the users connected to it, where the key
            // is the ES Id, and the value is a Dictionary of the User Id and the cost associated to servicing that user
            Map<Integer, Map<Integer, Double>> CPC = computeGCDforEachUserAndES(P, C);

            for (Map.Entry<Integer, Map<Integer, Double>> entry : CPC.entrySet()) {
                int ESKey = entry.getKey();
                System.out.printf("ES_ID %d is servicing %d users: \n", 
                ESKey, entry.getValue().size());

                Map<Integer, Double> user = entry.getValue();
                for (Map.Entry<Integer, Double> userEntry : user.entrySet()) {
                    System.out.printf("\tUser_ID %d - $%.2f\n", userEntry.getKey(), userEntry.getValue());
                }
            }
            System.out.print("\n");
        }
        else if (VERSION == 2) {
            // Hold the key of each user and a collection of the users closest to them, forming a cluster for that user
            Map<Integer, List<Integer>> clustersPerUser = new HashMap<>();

            // create a cluster for each user
            for (Map.Entry<Integer, List<Double>> currentUser : P.entrySet()) {
                List<Integer> closestUsers = new ArrayList<>();
                for (Map.Entry<Integer, List<Double>> users : P.entrySet()) {
                    if (!currentUser.getKey().equals(users.getKey())) {
                        // Compute gcd between each user, group users based on distance
                        double distance = computeGCD(currentUser.getValue().get(0), currentUser.getValue().get(1), 
                                                     users.getValue().get(0), users.getValue().get(1));

                        // if the distance between the two users is within the range of an antenna
                        if (distance <= SERVICEABLE_RADIUS) {
                            // collect them in a list
                            closestUsers.add(users.getKey());
                        }
                    }
                }
                
                if (!closestUsers.isEmpty()) {
                    clustersPerUser.put(currentUser.getKey(), closestUsers);
                }
            }

            // Display the clusters
            message = "User_ID %d has a cluster with :User_ID %d ";
            DisplayInfoII(clustersPerUser, message);
        }
    }
}
