package dsa_project;

import dsa_project.WaveletTree;
import dsa_project.SignalProcessor;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WaveletController {

    private WaveletGUI cView;
    private WaveletTree currentWaveletTree;
    private String currentDataType = "";
    private String currentDataSequence = "";
    private String currentAlphabetMapping = ""; // To store the string-to-char map description

    public WaveletController() {
        cView = new WaveletGUI(this);
        // Start directly at main menu (no login)
        startApp();
    }

    // --- APP START ---
    public void startApp() {
        cView.showMainMenuPanel();
    }

    // --- USER INPUT: LOG FILE ---
    public void processLogFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Log File to Compress");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            currentDataType = "LOG TOKEN SEQUENCE";

            try {
                String rawLog = Files.readString(file.toPath());
                if (rawLog.trim().isEmpty()) {
                    cView.displayMessage("Error: File is empty.");
                    return;
                }

                // 1. Convert to uppercase
                String upperLog = rawLog.toUpperCase();
                
                // 2. Extract potential log tokens (groups of letters/numbers)
                Pattern pattern = Pattern.compile("[A-Z0-9]+");
                Matcher matcher = pattern.matcher(upperLog);

                // Collect tokens
                java.util.List<String> tokens = new java.util.ArrayList<>();
                while (matcher.find()) {
                    tokens.add(matcher.group());
                }
                
                if (tokens.isEmpty()) {
                     cView.displayMessage("Error: Could not extract valid log tokens (A-Z, 0-9) from file.");
                     return;
                }
                
                // 3. Map string tokens to a single-character sequence 
                currentDataSequence = LogPreprocessor.mapTokensToSequence(tokens.toArray(new String[0]));
                currentAlphabetMapping = LogPreprocessor.getAlphabetMap(); 
                
                currentWaveletTree = new WaveletTree(currentDataSequence);
                showCompressionStats();

            } catch (IOException e) {
                cView.displayMessage("Error reading file: " + e.getMessage());
            }
        }
    }

    // --- USER INPUT: SIGNAL DATA ---
    public void processSignalData() {
        String[] options = {"Enter manually", "Upload file"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose how to provide signal data:",
                "Signal Input",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        currentDataType = "USER SIGNAL DATA";
        double[] rawSignal;

        try {
            if (choice == 0) {
                String input = JOptionPane.showInputDialog(
                        "Enter comma-separated signal values (e.g., 12.3,45.6,23.9):"
                );
                rawSignal = Arrays.stream(input.split(","))
                        .mapToDouble(Double::parseDouble)
                        .toArray();

            } else if (choice == 1) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select a Signal Data File (txt or csv)");
                int result = fileChooser.showOpenDialog(null);
                if (result != JFileChooser.APPROVE_OPTION) {
                    cView.displayMessage("No file selected.");
                    return;
                }

                File file = fileChooser.getSelectedFile();
                String content = Files.readString(file.toPath());
                rawSignal = Arrays.stream(content.split("[,\\s]+"))
                        .filter(s -> !s.isEmpty())
                        .mapToDouble(Double::parseDouble)
                        .toArray();

            } else {
                cView.displayMessage("Operation cancelled.");
                return;
            }
            
            currentAlphabetMapping = "";

            currentDataSequence = SignalProcessor.quantizeSignal(rawSignal);
            currentWaveletTree = new WaveletTree(currentDataSequence);
            showCompressionStats();

        } catch (Exception e) {
            cView.displayMessage("Error processing signal: " + e.getMessage());
        }
    }

    // --- SHARED METHOD: COMPRESSION STATS (Modified for Console Output) ---
    private void showCompressionStats() {
        if (currentWaveletTree == null) return;
        
        int originalSize = currentDataSequence.length() * 8;
        int compressedSize = (int) (currentWaveletTree.sequenceLength *
                Math.ceil(Math.log(currentWaveletTree.alphabet.length) / Math.log(2)));
        double compressionRatio = (double) originalSize / compressedSize;
        
        // --- NEW: Get Tree Structure and print it to the CONSOLE ---
        String treeStructure = currentWaveletTree.getStructureString();
        System.out.println("\n" + treeStructure);
        
        // Display the token mapping if it's a log file (also to console)
        if (!currentAlphabetMapping.isEmpty()) {
            System.out.println("\n" + currentAlphabetMapping);
        }

        // Call GUI method (reverted to 5 arguments)
        cView.showQueryPanel(
                currentDataType,
                currentDataSequence.substring(0, Math.min(60, currentDataSequence.length())) + "...",
                originalSize,
                compressedSize,
                compressionRatio
        );
    }

    // --- QUERY METHODS ---
    public String runAccess(int index) {
        if (currentWaveletTree == null) return "Error: No data loaded.";
        try {
            char resultChar = currentWaveletTree.access(index);
            String resultToken = LogPreprocessor.getTokenForChar(resultChar);
            
            return String.format("Result: Character at index %d is '%c' (Token: %s)", 
                    index, resultChar, resultToken);
        } catch (Exception e) {
            return "Error during Access: " + e.getMessage();
        }
    }

    public String runRank(char c, int index) {
        if (currentWaveletTree == null) return "Error: No data loaded.";
        try {
            char charToQuery = LogPreprocessor.getCharForToken(String.valueOf(c));
            if (charToQuery == '?') charToQuery = c;
            
            int count = currentWaveletTree.rank(charToQuery, index);
            
            String tokenDisplay = LogPreprocessor.getTokenForChar(charToQuery);
            String charDisplay = String.valueOf(charToQuery);
            if (!tokenDisplay.contains("Unknown")) {
                 charDisplay = String.format("'%c' (Token: %s)", charToQuery, tokenDisplay);
            }
            
            return String.format("Result: Rank of %s up to index %d is %d", charDisplay, index, count);
        } catch (Exception e) {
            return "Error during Rank: " + e.getMessage();
        }
    }

    public String runSelect(char c, int k) {
        if (currentWaveletTree == null) return "Error: No data loaded.";
        try {
            char charToQuery = LogPreprocessor.getCharForToken(String.valueOf(c));
            if (charToQuery == '?') charToQuery = c;
            
            int index = currentWaveletTree.select(charToQuery, k);
            
            String tokenDisplay = LogPreprocessor.getTokenForChar(charToQuery);
            String charDisplay = String.valueOf(charToQuery);
            if (!tokenDisplay.contains("Unknown")) {
                 charDisplay = String.format("'%c' (Token: %s)", charToQuery, tokenDisplay);
            }
            
            return String.format("Result: %d-th occurrence of %s is at index %d", k, charDisplay, index);
        } catch (Exception e) {
            return "Error during Select: " + e.getMessage();
        }
    }

    public String runQuantile(int i, int j, int k) {
        if (currentWaveletTree == null) return "Error: No data loaded.";
        try {
            char resultChar = currentWaveletTree.quantile(i, j, k);
            String resultToken = LogPreprocessor.getTokenForChar(resultChar);
            
            return String.format(
                    "Result: The %d-th smallest element in range [%d, %d] is '%c' (Token: %s)",
                    k, i, j, resultChar, resultToken
            );
        } catch (Exception e) {
            return "Error during Quantile: " + e.getMessage();
        }
    }

    public String runRangeCount(int i, int j, char c1, char c2) {
        if (currentWaveletTree == null) return "Error: No data loaded.";
        try {
            char char1 = LogPreprocessor.getCharForToken(String.valueOf(c1));
            char char2 = LogPreprocessor.getCharForToken(String.valueOf(c2));
            
            if (char1 == '?') char1 = c1;
            if (char2 == '?') char2 = c2;

            if (char1 > char2) {
                char temp = char1; char1 = char2; char2 = temp;
            }
            
            int count = currentWaveletTree.rangeCount(i, j, char1, char2);
            
            String tokenDisplay1 = LogPreprocessor.getTokenForChar(char1);
            String tokenDisplay2 = LogPreprocessor.getTokenForChar(char2);
            
            String c1Display = (!tokenDisplay1.contains("Unknown")) ? String.format("'%c' (Token: %s)", char1, tokenDisplay1) : String.valueOf(char1);
            String c2Display = (!tokenDisplay2.contains("Unknown")) ? String.format("'%c' (Token: %s)", char2, tokenDisplay2) : String.valueOf(char2);

            return String.format(
                    "Result: Tokens in range [%d, %d] from %s to %s = %d",
                    i, j, c1Display, c2Display, count
            );
        } catch (Exception e) {
            return "Error during RangeCount: " + e.getMessage();
        }
    }
}
