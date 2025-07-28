import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecretFinder {
    record Point(BigInteger x, BigInteger y) {}
    public static void main(String[] args) {
        String testCase1 = """
        {
          "keys": { "n": "4", "k": "3" },
          "1": { "base": "10", "value": "4" },
          "2": { "base": "2", "value": "111" },
          "3": { "base": "10", "value": "12" },
          "6": { "base": "4", "value": "213" }
        }
        """;

        String testCase2 = """
        {
          "keys": { "n": "10", "k": "7" },
          "1": { "base": "6", "value": "114442114404455345511" },
          "2": { "base": "15", "value": "aec7015a346d63" },
          "3": { "base": "15", "value": "6aeeb0631c227c" },
          "4": { "base": "16", "value": "e3b5e0562d881" },
          "5": { "base": "8", "value": "316034514573652b20673" },
          "6": { "base": "3", "value": "212221120122221120210021020220200" },
          "7": { "base": "3", "value": "20120222112211000210020112011121" },
          "8": { "base": "6", "value": "202055435333024000224253" },
          "9": { "base": "12", "value": "45153788322a1255483" },
          "10": { "base": "7", "value": "1101613130135206313514143" }
        }
        """;

        BigInteger secret1 = solve(testCase1);
        BigInteger secret2 = solve(testCase2);

        System.out.println("Secret for Test Case 1: " + secret1);
        System.out.println("Secret for Test Case 2: " + secret2);
    }

    public static BigInteger solve(String jsonString) {
        // 1. Parse JSON manually
        String keysObjectStr = findJsonValue(jsonString, "keys");
        int n = Integer.parseInt(findJsonValue(keysObjectStr, "n"));
        int k = Integer.parseInt(findJsonValue(keysObjectStr, "k"));
        
        List<Point> points = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            String pointKey = String.valueOf(i);
            String pointObjectStr = findJsonValue(jsonString, pointKey);
            if (pointObjectStr != null) {
                try {
                    int base = Integer.parseInt(findJsonValue(pointObjectStr, "base"));
                    String value = findJsonValue(pointObjectStr, "value");
                    points.add(new Point(
                        BigInteger.valueOf(i),
                        new BigInteger(value, base)
                    ));
                } catch (Exception e) {
                }
            }
        }
        
        if (points.size() < k) {
             throw new IllegalArgumentException("Not enough valid points to solve for the secret.");
        }

        
        Map<BigInteger, Integer> secretFrequencies = new HashMap<>();
        generateCombinations(points, k, 0, new ArrayList<>(), secretFrequencies);

        
        return Collections.max(secretFrequencies.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    
    private static String findJsonValue(String json, String key) {
        if (json == null) return null;
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int valueStartIndex = keyIndex + searchKey.length();
        char firstChar = json.charAt(valueStartIndex);

        if (firstChar == '"') {
            int valueEndIndex = json.indexOf('"', valueStartIndex + 1);
            return json.substring(valueStartIndex + 1, valueEndIndex);
        } else if (firstChar == '{') { 
            int braceCount = 1;
            int currentPos = valueStartIndex + 1;
            while (braceCount > 0 && currentPos < json.length()) {
                if (json.charAt(currentPos) == '{') braceCount++;
                if (json.charAt(currentPos) == '}') braceCount--;
                currentPos++;
            }
            return json.substring(valueStartIndex, currentPos);
        }
        return null;
    }

    
    private static void generateCombinations(List<Point> allPoints, int k, int start,
                                             List<Point> currentCombo, Map<BigInteger, Integer> frequencies) {
        if (currentCombo.size() == k) {
            BigInteger secret = calculateLagrangeP0(currentCombo);
            if (secret != null) {
                frequencies.put(secret, frequencies.getOrDefault(secret, 0) + 1);
            }
            return;
        }

        for (int i = start; i < allPoints.size(); i++) {
            currentCombo.add(allPoints.get(i));
            generateCombinations(allPoints, k, i + 1, currentCombo, frequencies);
            currentCombo.remove(currentCombo.size() - 1); 
        }
    }

    
    private static BigInteger calculateLagrangeP0(List<Point> combo) {
        BigInteger totalNumerator = BigInteger.ZERO;
        BigInteger totalDenominator = BigInteger.ONE;

        for (int j = 0; j < combo.size(); j++) {
            BigInteger xj = combo.get(j).x();
            BigInteger yj = combo.get(j).y();

            BigInteger termNumerator = yj;
            BigInteger termDenominator = BigInteger.ONE;

            for (int m = 0; m < combo.size(); m++) {
                if (m == j) continue;
                BigInteger xm = combo.get(m).x();
                termNumerator = termNumerator.multiply(xm);
                termDenominator = termDenominator.multiply(xm.subtract(xj));
            }

            totalNumerator = totalNumerator.multiply(termDenominator).add(termNumerator.multiply(totalDenominator));
            totalDenominator = totalDenominator.multiply(termDenominator);
        }

        if (totalNumerator.remainder(totalDenominator).equals(BigInteger.ZERO)) {
            return totalNumerator.divide(totalDenominator);
        }
        return null;
    }
}
