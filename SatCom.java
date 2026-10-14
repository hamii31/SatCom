import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computational implementation of Case 1 of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 */

public class SatCom {
    /**
     * Builds a Dictionary with unique keys and a collection of variables from the double type 
     * holding the geographical location and communication volume of each user
     */
    private static Map<Integer, List<Double>> createUsers(int n) {
        Map<Integer, List<Double>> P = new HashMap<>();
        for (int i = 0; i < n; i++) {
            // generate a random double value between -90 and 90 degrees for the latitude
            double latitude = -90 + (Math.random() * 90);
            // generate a random double value between -180 and 180 degrees for the longtitude 
            double longtitude = -180 + (Math.random() * 180);
            // generate a random double value between 0.0001 and 1.0 for the weight
            double weight = 0.0001 + Math.random();

            P.put(i + 1, List.of(latitude, longtitude, weight));
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
            double latitude = -90 + (Math.random() * 90);
            double longtitude = -180 + (Math.random() * 180);

            C.put(i + 1, List.of(latitude, longtitude));
        }
        return C;
    }

    /**
     * Computes the distance between two points on Earth
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

   /**
    * Iteratively compute the distance between each user and each earth station
    * Create a Dictionary for each station and its closest users by comparing the gcd of 
    * every individual user and each ES. The smaller the gcd, the better.
    */
    private static Map<Integer, List<Integer>> createSetOfUsersAndES (Map<Integer, List<Double>> P, Map<Integer, List<Double>> C) {
        
        // Holds the keys of each ES and the Users associated to it
        Map<Integer, List<Integer>> C_k = new HashMap<>();
        // Append all keys with empty lists
        for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
            C_k.put(ESInfo.getKey(), new ArrayList<>());
        }

        // Compute the distance between each user and each ES
        for (Map.Entry<Integer, List<Double>> userInfo : P.entrySet()) {
            double userLat = userInfo.getValue().get(0);
            double userLong = userInfo.getValue().get(1);

            double minDistance = Integer.MAX_VALUE;
            int ESKey = -1;
            for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
                double ESLat = ESInfo.getValue().get(0);
                double ESLong = ESInfo.getValue().get(1);

                double distance = computeGCD(userLat, userLong, ESLat, ESLong);
                if (distance < minDistance) {
                    minDistance = distance;
                    // get the key of the closest ES
                    ESKey = ESInfo.getKey();
                }
            }

            // add the user to the ES
            if (C_k.containsKey(ESKey)) {
                C_k.get(ESKey).add(userInfo.getKey());
            }
        }

        return C_k;
    }

    public static void main(String[] args) {
        // number of users
        int n = 1_000;
        // Create a Dictionary of users P with random coordinates (lat, long) and outgoing communication volume (weight for i-th user)
        Map<Integer, List<Double>> P = createUsers(n);
        // Display the users map
        for (Map.Entry<Integer, List<Double>> userInfo : P.entrySet()) {
            System.out.printf("User_ID %d logged in from coordinates (%.3f, %.3f) with outflow weight %.3f\n", 
            userInfo.getKey(), userInfo.getValue().get(0), userInfo.getValue().get(1), userInfo.getValue().get(2));
        }

        // number of earth stations (ES)
        int m = 10;
        // Create a Dictionary of ES locations
        Map<Integer, List<Double>> C = createEarthStations(m);
        // Display the earth stations
        for (Map.Entry<Integer, List<Double>> earthStations : C.entrySet()) {
            System.out.printf("ES_ID %d is online from (%.3f, %.3f)\n", 
            earthStations.getKey(), earthStations.getValue().get(0), earthStations.getValue().get(1));
        }

        // Create a Dictionary for each earth station and the users connected to it
        // C[k], where k is the key referring to the current Earth Station, and the value is a list of user IDs (and potentially the distance from the ES)
        Map<Integer, List<Integer>> C_k = createSetOfUsersAndES(P, C);

        for (Map.Entry<Integer, List<Integer>> entry : C_k.entrySet()) {
            System.out.printf("ES_ID %d is servicing the following users: %s\n", 
            entry.getKey(), entry.getValue().toString());
        }
    }
}
