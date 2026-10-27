package dsa_project;

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * PROJECT: Real-Time Wavelet Tree Compression & Querying System
 * ============================================================================
 *
 * DEFINITION:
 * A Wavelet Tree is a succinct data structure that stores a sequence
 * in compressed form while allowing efficient queries such as:
 * - access(i)      : get character at index i
 * - rank(c, i)     : count occurrences of character c up to index i
 * - select(c, k)   : find index of k-th occurrence of c
 * - quantile(i,j,k): find k-th smallest character in range [i..j]
 * - rangeCount(i,j,c1,c2): count characters in [i..j] between c1..c2
 *
 * APPLICATIONS:
 * - Real-time log analysis (ERROR/WARN frequency)
 * - Signal compression and anomaly detection
 * - Streaming and database indexing
 * - Bandwidth-efficient monitoring
 *
 * COMPLEXITIES:
 * Build: O(N log σ), access: O(log σ), rank: O(log σ)
 * select: O(N) (non-succinct version)
 * quantile: O(log σ), rangeCount: O(log σ)
 * Space: O(N)
 *
 * (σ = alphabet size, N = sequence length)
 */


class WaveletTree {
    char[] alphabet;
    int[] bitmap; 
    WaveletTree left, right;
    boolean isLeaf;
    char leafChar;
    int sequenceLength;

    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    public WaveletTree(String data) {
        this.alphabet = extractAlphabet(data);
        this.sequenceLength = data.length();
        build(data);  // O(N log σ)
    }

    private WaveletTree(String data, char[] alphabet) {
        this.alphabet = alphabet;
        this.sequenceLength = data.length();
        build(data);  // O(N log σ)
    }

    // ------------------------------------------------------------
    // BUILD FUNCTION
    // ------------------------------------------------------------
    private void build(String data) {
        if (data.isEmpty()) return;
        if (alphabet.length == 1) { // Leaf node
            isLeaf = true;
            leafChar = alphabet[0];
            bitmap = new int[data.length()];
            return;
        }

        int mid = alphabet.length / 2;
        char[] leftAlphabet = Arrays.copyOfRange(alphabet, 0, mid);
        char[] rightAlphabet = Arrays.copyOfRange(alphabet, mid, alphabet.length);

        StringBuilder leftData = new StringBuilder();
        StringBuilder rightData = new StringBuilder();

        bitmap = new int[data.length()];
        int count = 0; // cumulative 1s
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (contains(leftAlphabet, c)) leftData.append(c);
            else { rightData.append(c); count++; }
            bitmap[i] = count;
        }

        if (leftData.length() > 0) left = new WaveletTree(leftData.toString(), leftAlphabet);
        if (rightData.length() > 0) right = new WaveletTree(rightData.toString(), rightAlphabet);
    }

    // Extract sorted unique characters
    private char[] extractAlphabet(String data) {
        return data.chars()
                   .mapToObj(c -> (char)c)
                   .distinct()
                   .sorted()
                   .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                   .toString().toCharArray();
    }

    private boolean contains(char[] arr, char c) {
        for (char x : arr) if (x == c) return true;
        return false;
    }

    // ------------------------------------------------------------
    // CORE OPERATIONS (Re-implemented for structure/functionality)
    // ------------------------------------------------------------

    // Rank helpers
    private int rankZero(int i) { return (i<0)?0:(i+1)-bitmap[i]; }
    private int rankOne(int i) { return (i<0)?0:bitmap[i]; }

    /** * ACCESS(i) - Get the character at index i. */
    public char access(int i) {
        if (i < 0 || i >= sequenceLength) return '?'; 
        WaveletTree curr = this;
        int current_i = i;

        while (curr.left != null || curr.right != null) {
            if (curr.isLeaf) return curr.leafChar;
            int bit = curr.rankOne(current_i) - curr.rankOne(current_i - 1);
            if (bit == 0) {
                if (curr.left == null) return '?';
                current_i = curr.rankZero(current_i) - 1; 
                curr = curr.left;
            } else {
                if (curr.right == null) return '?';
                current_i = curr.rankOne(current_i) - 1;
                curr = curr.right;
            }
        }
        return curr.leafChar;
    }

    /** RANK(c, i) - Count occurrences of character c in prefix [0..i]. */
    public int rank(char c, int i) {
        if (i < 0) return 0;
        if (isLeaf) return (leafChar == c) ? (i + 1) : 0;
        
        boolean goLeft = false;
        if (left != null) goLeft = contains(left.alphabet, c);
        
        if (goLeft) return (left != null) ? left.rank(c, rankZero(i) - 1) : 0;
        else return (right != null) ? right.rank(c, rankOne(i) - 1) : 0;
    }

    /** SELECT(c, k) - Find index of k-th occurrence of c. (Non-succinct) */
    public int select(char c, int k) {
        if (k <= 0) return -1;
        if (isLeaf) return (leafChar == c) ? k - 1 : -1;
        
        boolean goLeft = false;
        if (left != null) goLeft = contains(left.alphabet, c);

        if (goLeft) {
             int child_pos = left.select(c, k);
             return (child_pos != -1) ? findPosition(child_pos, false) : -1;
        } else {
             int child_pos = right.select(c, k);
             return (child_pos != -1) ? findPosition(child_pos, true) : -1;
        }
    }

    private int findPosition(int pos, boolean rightSide) {
        int count = -1;
        for (int i = 0; i < bitmap.length; i++) {
            int bit = rankOne(i) - rankOne(i - 1);
            if ((bit == 1 && rightSide) || (bit == 0 && !rightSide)) {
                count++;
                if (count == pos) return i;
            }
        }
        return -1;
    }

    /** QUANTILE(i, j, k) - k-th smallest in range [i..j]. */
    public char quantile(int i, int j, int k) {
        if (i < 0 || j >= sequenceLength || i > j || k <= 0 || k > (j - i + 1)) return '?';
        return quantileRecursive(i, j, k);
    }

    private char quantileRecursive(int i, int j, int k) {
        if (isLeaf) return leafChar;
        
        int count1 = rankOne(j) - rankOne(i - 1);
        int count0 = (j - i + 1) - count1; 
        
        if (k <= count0)
            return left.quantileRecursive(rankZero(i-1), rankZero(j)-1, k);
        else
            return right.quantileRecursive(rankOne(i-1), rankOne(j)-1, k-count0);
    }

    /** RANGECOUNT(i,j,c1,c2) - Count symbols between c1..c2 in [i..j]. */
    public int rangeCount(int i, int j, char c1, char c2) {
        if (i < 0 || j >= sequenceLength || i > j || c1 > c2) return 0;
        return rangeCountRecursive(i,j,c1,c2);
    }

    private int rangeCountRecursive(int i, int j, char c1, char c2) {
        if (i > j) return 0;
        
        char low = alphabet[0], high = alphabet[alphabet.length-1];
        if (c1 > high || c2 < low) return 0;
        if (c1 <= low && c2 >= high) return j-i+1;
        
        if (isLeaf) return 0;

        char midChar = alphabet[alphabet.length/2];
        
        int rank0_i = rankZero(i-1), rank0_j = rankZero(j)-1;
        int rank1_i = rankOne(i-1), rank1_j = rankOne(j)-1;
        int count = 0;

        if (left != null && c1 < midChar) 
            count += left.rangeCountRecursive(rank0_i, rank0_j, c1, (char)(midChar-1));
        
        if (right != null && c2 >= midChar) 
            count += right.rangeCountRecursive(rank1_i, rank1_j, midChar, c2);
        
        return count;
    }


    // ------------------------------------------------------------
    // NEW: STRUCTURE VISUALIZATION (OUTPUTS TO CONSOLE VIA CONTROLLER)
    // ------------------------------------------------------------
    /**
     * Generates a text-based, tree-like structure visualization.
     */
    public String getStructureString() {
        if (alphabet.length == 0) return "Wavelet Tree: Structure is empty.";
        return "================= WAVELET TREE STRUCTURE ================\n" + 
               getStructureStringRecursive(this, "", true) + 
               "=======================================================\n";
    }

    private String getStructureStringRecursive(WaveletTree curr, String prefix, boolean isTail) {
        if (curr == null) return "";
        StringBuilder sb = new StringBuilder();
        
        // Node line format: [Alphabet range]
        String alphaStr = new String(curr.alphabet);
        String range = (alphaStr.length() > 1) 
                       ? String.format("['%c'...'%c'] (Length: %d)", alphaStr.charAt(0), alphaStr.charAt(alphaStr.length() - 1), curr.sequenceLength)
                       : String.format("['%c'] (Leaf, Length: %d)", alphaStr.charAt(0), curr.sequenceLength);

        sb.append(prefix).append(isTail ? "└── " : "├── ").append(range).append("\n");

        // Recurse right first, then left for better visual representation
        String childPrefix = prefix + (isTail ? "    " : "│   ");
        if (curr.right != null) {
            sb.append(getStructureStringRecursive(curr.right, childPrefix, false));
        }
        if (curr.left != null) {
            sb.append(getStructureStringRecursive(curr.left, childPrefix, true));
        }
        
        return sb.toString();
    }
}

// ============================================================================
// LOG PREPROCESSOR
// ============================================================================
class LogPreprocessor {
    private static Map<String, Character> tokenToCharMap = new HashMap<>();
    private static Map<Character, String> charToTokenMap = new HashMap<>();
    private static final char START_CHAR = '!';
    private static final char END_CHAR = '~';
    private static final int MAX_TOKENS = END_CHAR - START_CHAR + 1;

    public static String mapTokensToSequence(String[] tokens) {
        tokenToCharMap.clear();
        charToTokenMap.clear();
        StringBuilder sequence = new StringBuilder();
        char nextChar = START_CHAR; 

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (!tokenToCharMap.containsKey(token)) {
                if (nextChar > END_CHAR) {
                    System.err.println("Warning: Alphabet limit (" + MAX_TOKENS + ") reached. Skipping token: " + token);
                    continue; 
                }
                charToTokenMap.put(nextChar, token);
                tokenToCharMap.put(token, nextChar);
                nextChar = (char) (nextChar + 1);
            }
            sequence.append(tokenToCharMap.get(token));
        }
        return sequence.toString();
    }

    public static String getTokenForChar(char c) {
        return charToTokenMap.getOrDefault(c, "? (Unknown/Out of Alphabet)");
    }
    
    public static char getCharForToken(String token) {
        return tokenToCharMap.getOrDefault(token, '?');
    }

    public static String getAlphabetMap() {
        if (charToTokenMap.isEmpty()) return "No token mapping established.";
        List<Character> sortedChars = new ArrayList<>(charToTokenMap.keySet());
        Collections.sort(sortedChars);
        
        StringBuilder sb = new StringBuilder("Symbol Mapping (for Queries):\n");
        for (char k : sortedChars) {
             sb.append(String.format("  '%c' (ASCII %d) -> \"%s\"\n", k, (int)k, charToTokenMap.get(k)));
        }
        sb.append(String.format("\nMaximum capacity: %d unique symbols ('%c' to '%c').", MAX_TOKENS, START_CHAR, END_CHAR));
        return sb.toString();
    }
}


// ============================================================================
// SIGNAL PROCESSOR
// ============================================================================
class SignalProcessor {
    public static String quantizeSignal(double[] signalData) {
        if (signalData.length == 0) return "";
        double minVal = signalData[0], maxVal = signalData[0];
        for (double val : signalData) { if (val < minVal) minVal = val; if (val > maxVal) maxVal = val; }

        int alphabetSize = 8;
        double range = maxVal - minVal;
        double interval = (range <= 1e-6) ? 1.0 : range/alphabetSize;

        StringBuilder quantizedSignal = new StringBuilder();
        for (double val : signalData) {
            int index = (int)((val - minVal)/interval);
            index = Math.max(0, Math.min(alphabetSize-1,index));
            quantizedSignal.append((char)('A'+index));
        }
        return quantizedSignal.toString();
    }

    public static double[] createSimulatedSignal(int size) {
        double[] signal = new double[size];
        for(int i=0;i<size;i++){
            double rhythm = Math.sin(i/10.0)*0.5;
            double spike = (i%50==0)?1.5:0;
            double noise = Math.random()*0.1;
            signal[i] = rhythm + spike + noise;
        }
        return signal;
    }
}

// ============================================================================
// API FETCHER
// ============================================================================
class ApiFetcher {
    public static String fetchLogData(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if(conn.getResponseCode()==200){
                try(BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
                    String line;
                    while((line=rd.readLine())!=null) result.append(line).append("\n");
                }
            } else System.err.println("HTTP Error: "+conn.getResponseCode());
            conn.disconnect();
        } catch(Exception e){
            System.err.println("Network error: "+e.getMessage());
        }
        return result.toString();
    }
}

// ============================================================================
// MAIN APPLICATION
// ============================================================================
public class RealTimeWaveletApp {
    public static void main(String[] args) {
    	 javax.swing.SwingUtilities.invokeLater(() -> {
             new WaveletController().startApp();
         });
         
    }
}
