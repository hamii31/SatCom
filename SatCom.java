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
            System.out.printf("User_ID %d logged in from coordinates (%.3f, %.3f) with outflow weight %.3f\n", 
            userLoc.getKey(), userLoc.getValue().get(0), userLoc.getValue().get(1), userLoc.getValue().get(2));
        }

        // number of earth stations (ES)
        int m = 10;

        // Create a set of ES locations
        Map<Integer, List<Double>> C = new HashMap<>();
        for (int i = 0; i < m; i++) {
            double lattitude = -90 + (Math.random() * 90);
            double longtitude = -180 + (Math.random() * 180);

            C.put(i + 1, List.of(lattitude, longtitude));
        }

        for (Map.Entry<Integer, List<Double>> earthStations : C.entrySet()) {
            System.out.printf("ES_ID %d is online from (%.3f, %.3f)\n", 
            earthStations.getKey(), earthStations.getValue().get(0), earthStations.getValue().get(1));
        }

        // Create S_k of all users connected with C_k
        // S_k will hold the key of each ES, and the value will be a map of all users connected to that ES
        Map<Integer, Map<Integer, List<Double>>> S_k = new HashMap<>();

        // Mock P map that will be modified
        Map<Integer, List<Double>> P_mock = P;

        int beginIndex = 0;
        int endIndex = 0;
        for (int k = 1; k <= C.size(); k++) {
            Map<Integer, List<Double>> P_k = new HashMap<>();

            // Assign 100 users per 1 ES in the original order of P
            // Delete them from the mock P
            int userCounter = 0;
            for (Map.Entry<Integer, List<Double>> entry : P_mock.entrySet()) {
                if (userCounter == 100) {
                    beginIndex = endIndex;
                    endIndex += userCounter;
                    break;
                }
                    
                P_k.put(entry.getKey(), entry.getValue());

                userCounter++;
            }

            // remove the added users from the P_mock
            for (int key = beginIndex; key < endIndex; key++)
                P_mock.remove(key);

            S_k.put(k, P_k);
        }

        // Show all ES-User pairs
        for (Map.Entry<Integer, Map<Integer, List<Double>>> usersPerStation : S_k.entrySet()) {
            System.out.printf("Connected users to ES_ID %d: \n", usersPerStation.getKey());
            
            Map<Integer, List<Double>> tempUsrs = usersPerStation.getValue();
            for (Map.Entry<Integer, List<Double>> users : tempUsrs.entrySet()) {
                System.out.printf("\tUser_ID %d, coordinates - (%.3f, %.3f), flow - %.3f\n", 
                users.getKey(), users.getValue().get(0), users.getValue().get(1), users.getValue().get(2));
            }
        }
    }
}
