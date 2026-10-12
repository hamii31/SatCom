import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Implementing algorithms from https://research.ebsco.com/plink/c34e4d37-7312-36a3-83c0-b3baf39ab050

public class SatCom {
    public static void main(String[] args) {
        // number of users
        int n = 1_000;

        // Create a set of users P with random coordinates and outgoing communication volume
        Map<Integer, List<Double>> P = new HashMap<>();
        for (int i = 0; i < n; i++) {
            double lattitude = -90 + (Math.random() * 90);
            double longtitude = -180 + (Math.random() * 180);
            double weight = 0.0001 + Math.random();

            P.put(i + 1, List.of(lattitude, longtitude, weight));
        }

        for (Map.Entry<Integer, List<Double>> userLoc : P.entrySet()) {
            System.out.printf("UserID %d logged in from coordinates (%.3f, %.3f) with outflow weight %.3f\n", 
            userLoc.getKey(), userLoc.getValue().get(0), userLoc.getValue().get(1), userLoc.getValue().get(2));
        }

        // number of earth stations (ES)
        int m = 10;

        // Create a set of ES locations
        Map<Integer, List<Double>> C = new HashMap<Integer, List<Double>>();
        for (int i = 0; i < m; i++) {
            double lattitude = -90 + (Math.random() * 90);
            double longtitude = -180 + (Math.random() * 180);

            C.put(i + 1, List.of(lattitude, longtitude));
        }

        for (Map.Entry<Integer, List<Double>> earthStations : C.entrySet()) {
            System.out.printf("ES_ID %d is online from (%.3f, %.3f)\n", 
            earthStations.getKey(), earthStations.getValue().get(0), earthStations.getValue().get(1));
        }
    }
}
