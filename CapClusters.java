import java.util.*;

/**
 * This class holds the methods for placing a spherical cap of varying size on a sphere with radius equal to that of Earth. 
 * It also holds a method for generating users within that spherical cap.
 * The class invokes Clusters() to analyze the clustering of the users within the cap, and to find the best place to build an Earth Station.
 */
public class CapClusters {
    /* 
    * Chooses a random location for the center of a spherical cap and computes its radius
    * Returns a list of the coordinates and radius of the cap (no need for map since its a singular entity)
        */
    private static List<Double> buildSphericalCap() {
        double latitude = -90 + (Math.random() * 180);           
        double longitude = -180 + (Math.random() * 360);
        double sphericalCapRadius = SatCom.SPHERICAL_CAP_RADIUS;
        System.out.printf("The center of the spherical cap is at (%.3f, %.3f) and the radius is %.2f\n", latitude, longitude, sphericalCapRadius);
        return List.of(latitude, longitude, sphericalCapRadius);
    }
    private static boolean userWithiNCap(double userLat, double userLong, List<Double> cap) {
        double capLat = cap.get(0);
        double capLong = cap.get(1);
        double capRadius = cap.get(2);

        double distance = SatCom.computeGCD(userLat, userLong, capLat, capLong);

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
                double mbps = 1 + Math.random() * SatCom.PER_USER_DEMAND;
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
        // String message = "User_ID %d is at :(%.3f:, %.3f): and accumulates %.2f Mbps of traffic";
        // SatCom.DisplayInfo(P, message);

        return Clusters.buildNetwork(P);
    }

    public static void main(String[] args) {

    }
}
