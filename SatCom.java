import java.util.*;

/**
 * Computational implementation of Verkhovsky, B.S. (2011) ‘Satellite communication networks: Configuration design 
 * of terrestrial subnetworks’, Journal of Telecommunications Management, 3(4), pp.360–373. 
 * Available at: https://research.ebsco.com/plink/986e42a1-fdd3-39ba-9f84-85467bb238e6 (Accessed: 14 October 2026).
 */

/**
 * This is the main class of the project. It holds the global constants and global methods for the project.
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

    public static void main(String[] args) {
        // let's try out five different scenarios
        CapClusters[] scenarios = new CapClusters[5];

        for (int i = 0; i < scenarios.length; i++) {
            System.out.printf("Scenario %d: \n", i + 1);
            scenarios[i] = new CapClusters();
            scenarios[i].buildNetworkWithinCap(20_000);
        }
    }
}
