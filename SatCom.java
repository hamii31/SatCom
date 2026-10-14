import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computational implementation of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 * The user count and locations are known.
 * The earth station count and locations are known
 */

public class SatCom {
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
            double longtitude = -180 + (Math.random() * 360);
            // generate a random double value between 1 to 999
            double dataTraffic = 1 + Math.random() * 999;

            P.put(i + 1, List.of(latitude, longtitude, dataTraffic));
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
            double longtitude = -180 + (Math.random() * 360);

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
    * Create a Dictionary for each station and the users that fall within its serviceable range
    * by comparing the gcd of every individual user and each ES
    */
    private static Map<Integer, Map<Integer, Double>> computeGCDforEachUserAndES (Map<Integer, List<Double>> P, Map<Integer, List<Double>> C) {
        // the typical serviceable range of ES is 200 square km
        final double SERVICEABLE_AREA = 200.0;
        final double SERVICEABLE_RADIUS = Math.sqrt(SERVICEABLE_AREA / Math.PI);

        // Holds the keys of each ES and the Users associated to it
        Map<Integer, Map<Integer, Double>> S = new HashMap<>();
        // Append all keys with empty lists
        for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
            S.put(ESInfo.getKey(), new HashMap<>());
        }

        // Compute the distance between each user and each ES
        for (Map.Entry<Integer, List<Double>> userInfo : P.entrySet()) {
            double userLat = userInfo.getValue().get(0);
            double userLong = userInfo.getValue().get(1);

            for (Map.Entry<Integer, List<Double>> ESInfo : C.entrySet()) {
                double ESLat = ESInfo.getValue().get(0);
                double ESLong = ESInfo.getValue().get(1);

                double distance = computeGCD(userLat, userLong, ESLat, ESLong);
                // if the user is within the serviceable range of the current ES
                if (distance <= SERVICEABLE_RADIUS) {
                    // get the key of the that ES
                    int ESKey = ESInfo.getKey();

                    // and add the user to the ES users list
                    if (S.containsKey(ESKey)) {
                        S.get(ESKey).put(userInfo.getKey(), distance);
                    }
                }
            }
        }
        return S;
    }

    public static void main(String[] args) {
        // number of users
        int n = 100_000;
        // Create a Dictionary of users P with random coordinates (lat, long) and outgoing communication volume (weight for i-th user)
        Map<Integer, List<Double>> P = createUsers(n);
        // Display the users map
        for (Map.Entry<Integer, List<Double>> userInfo : P.entrySet()) {
            System.out.printf("User_ID %d logged in from coordinates (%.3f, %.3f) with data traffic of %.3f GB.\n", 
            userInfo.getKey(), userInfo.getValue().get(0), userInfo.getValue().get(1), userInfo.getValue().get(2));
        }
        System.out.print("\n");


        // number of earth stations (ES)
        int m = 1000;
        // Create a Dictionary of ES locations
        Map<Integer, List<Double>> C = createEarthStations(m);
        // Display the earth stations
        for (Map.Entry<Integer, List<Double>> earthStations : C.entrySet()) {
            System.out.printf("ES_ID %d is online from (%.3f, %.3f)\n", 
            earthStations.getKey(), earthStations.getValue().get(0), earthStations.getValue().get(1));
        }
        System.out.print("\n");

        
        // Create a Dictionary S for each earth station and the users connected to it, where the key
        // is the ES Id, and the value is a Dictionary of the User Id and the distance from the ES
        Map<Integer, Map<Integer, Double>> S = computeGCDforEachUserAndES(P, C);

        for (Map.Entry<Integer, Map<Integer, Double>> entry : S.entrySet()) {
            int ESKey = entry.getKey();
            System.out.printf("ES_ID %d is servicing %d users: \n", 
            ESKey, entry.getValue().size());

            Map<Integer, Double> user = entry.getValue();
            for (Map.Entry<Integer, Double> userEntry : user.entrySet()) {
                System.out.printf("\tUser_ID %d - %.2f km from the station\n", userEntry.getKey(), userEntry.getValue());
            }
        }
        System.out.print("\n");
    }
}
