package dsa_project;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;

//Add this class inside or alongside your WaveletGUI class
class GradientPanel extends JPanel {
 private Color color1;
 private Color color2;

 public GradientPanel(Color c1, Color c2) {
     this.color1 = c1;
     this.color2 = c2;
 }

 @Override
 protected void paintComponent(Graphics g) {
     super.paintComponent(g);
     Graphics2D g2d = (Graphics2D) g;
     g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
     int w = getWidth();
     int h = getHeight();

     // Create a linear gradient from top to bottom
     GradientPaint gp = new GradientPaint(
             0, 0, color1,
             0, h, color2);

     g2d.setPaint(gp);
     g2d.fillRect(0, 0, w, h);
 }
}

public class WaveletGUI extends JFrame {

    private WaveletController controller;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextArea outputArea;

    private final static String MAIN_MENU_PANEL = "MainMenu";
    private final static String QUERY_PANEL = "Query";

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public WaveletGUI(WaveletController controller) {
        this.controller = controller;
        setTitle("Wavelet Tree Processor"); // Updated Title
        setSize(850, 650);
        setLocationRelativeTo(null); // Center window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize panels
        mainPanel.add(createMainMenuPanel(), MAIN_MENU_PANEL);
        mainPanel.add(createQueryPanel(), QUERY_PANEL);

        add(mainPanel);
        setVisible(true);

        // Show main menu directly at startup
        showMainMenuPanel();
    }

    // --- MAIN MENU PANEL (Updated with Gradient and Exit Button Position) ---
    private JPanel createMainMenuPanel() {
        // Use a light, modern color for the background
        Color mainPanelBg = new Color(245, 245, 245); 
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(mainPanelBg);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); 

        // Top panel with title and exit button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(mainPanelBg); 

        // --- CHANGE: Use GradientPanel for the Title Background ---
        Color startColor = new Color(200, 220, 255); // Light Blue start
        Color endColor = new Color(230, 230, 250);   // Light Lavender end

        // Replace the regular JPanel with the custom GradientPanel
        GradientPanel titlePanel = new GradientPanel(startColor, endColor);
        titlePanel.setLayout(new GridLayout(2, 1));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); 
        
        // Add a subtle border to the title panel
        Border line = BorderFactory.createLineBorder(new Color(170, 190, 220)); // Border matches gradient
        Border margin = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createCompoundBorder(margin, titlePanel.getBorder())));


        JLabel mainTitle = new JLabel("Log & Signal Processor", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Arial", Font.BOLD, 30)); 
        mainTitle.setForeground(new Color(40, 40, 100)); // Dark Blue Color

        JLabel subTitle = new JLabel("Choose Data Type to Compress & Query", SwingConstants.CENTER);
        subTitle.setFont(new Font("Arial", Font.PLAIN, 18));
        subTitle.setForeground(new Color(60, 60, 60)); // Darker Gray

        titlePanel.add(mainTitle);
        titlePanel.add(subTitle);
        
        // Exit button at top-right
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14)); 
        exitButton.setBackground(new Color(220, 50, 50)); 
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> System.exit(0));

        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); 
        exitPanel.setBackground(mainPanelBg); 
        // MODIFIED: Removed top padding to align the button to the absolute top-right of the North area
        exitPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); 
        exitPanel.add(exitButton);

        
        JPanel northContainer = new JPanel(new BorderLayout(0, 30));
        northContainer.setBackground(mainPanelBg);
        
        // This keeps the title panel centered in the top area and the exit button to the right
        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(exitPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);


        // Center buttons panel
        JPanel buttonWrapper = new JPanel(new GridBagLayout()); 
        buttonWrapper.setBackground(mainPanelBg);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 20, 20)); 
        buttonPanel.setBackground(mainPanelBg);
        
        // Button Renaming and Styling (No change here, keeping the previous styling)
        JButton logButton = new JButton("Process Log File");
        JButton signalButton = new JButton("Process Signal Data");

        Font buttonFont = new Font("Arial", Font.BOLD, 16); 
        logButton.setFont(buttonFont);
        signalButton.setFont(buttonFont);
        
        Dimension buttonSize = new Dimension(280, 50); 
        logButton.setPreferredSize(buttonSize);
        signalButton.setPreferredSize(buttonSize);
        
        Color buttonColor = new Color(100, 149, 237); // Cornflower Blue
        logButton.setBackground(buttonColor); 
        signalButton.setBackground(buttonColor);
        logButton.setForeground(Color.WHITE);
        signalButton.setForeground(Color.WHITE);
        logButton.setFocusPainted(false);
        signalButton.setFocusPainted(false);
        
        logButton.addActionListener(e -> controller.processLogFile());
        signalButton.addActionListener(e -> controller.processSignalData());

        buttonPanel.add(logButton);
        buttonPanel.add(signalButton);
        
        buttonWrapper.add(buttonPanel); 
        
        panel.add(buttonWrapper, BorderLayout.CENTER); 
        
        return panel;
    }

    // --- QUERY PANEL (Button Renaming and Visual Improvements) ---
    private JPanel createQueryPanel() {
        Color panelBg = new Color(250, 250, 255); // Very light blue/white
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(panelBg);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top info panel
        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 5, 5)); 
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY.darker()), 
            "Compression & Data Info", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14), 
            new Color(40, 40, 100) // Dark Blue for title
        ));
        infoPanel.setBackground(Color.WHITE); 
        infoPanel.setName("InfoPanel");
        panel.add(infoPanel, BorderLayout.NORTH);

        // Center output area
        outputArea = new JTextArea(12, 70);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setBackground(new Color(240, 240, 240)); 
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom query buttons
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15)); 
        optionsPanel.setBackground(panelBg);
        optionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY.darker()), 
            "Wavelet Tree Core Queries", 
            TitledBorder.CENTER, 
            TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14), 
            new Color(40, 40, 100)
        ));

        // --- Button Definitions ---
        JButton accessButton = new JButton("Event");
        JButton rankButton = new JButton("Count");
        JButton selectButton = new JButton("Locate");
        JButton quantileButton = new JButton("Severity");
        JButton rangeCountButton = new JButton("Range");
        JButton backButton = new JButton("⬅ Back to Main Menu");

        // --- MODIFIED: ADDED TOOLTIP TEXT ---
        accessButton.setToolTipText("Retrieves the mapped character (and original token) stored at a specific index 'i' in the sequence.");
        rankButton.setToolTipText("Counts the number of occurrences of a specific token 'c' in the sequence from the start (index 0) up to index 'i'.");
        selectButton.setToolTipText("Finds the index 'i' where the k-th occurrence of a specific token 'c' appears in the sequence.");
        quantileButton.setToolTipText("Finds the k-th smallest element (token) within a specified sub-range [i..j] of the sequence.");
        rangeCountButton.setToolTipText("Counts the total number of elements (tokens) in a specified range [i..j] that fall between the starting token 'c1' and the ending token 'c2'.");
        backButton.setToolTipText("Return to the main screen to load a different log or signal file.");


        // --- Visual Styling for Query Buttons ---
        Font queryButtonFont = new Font("Arial", Font.PLAIN, 13);
        Color queryButtonColor = new Color(70, 130, 180); // Steel Blue

        JButton[] queryButtons = {accessButton, rankButton, selectButton, quantileButton, rangeCountButton};
        for (JButton btn : queryButtons) {
            btn.setFont(queryButtonFont);
            btn.setBackground(queryButtonColor);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
        }
        
        backButton.setFont(queryButtonFont);
        backButton.setBackground(new Color(150, 150, 150)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);


        // ACCESS
        accessButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter index (i):");
            try {
                int i = Integer.parseInt(input);
                if (i < 0) throw new NumberFormatException();
                displayMessage(controller.runAccess(i));
            } catch (Exception ex) {
                displayMessage("⚠ Invalid index. Please enter a positive integer.");
            }
        });

        // RANK
        rankButton.addActionListener(e -> {
            // NOTE: cInput is the string token (e.g., "ERROR"), which the controller will translate to a char
            String cInput = JOptionPane.showInputDialog(this, "Enter token (e.g., ERROR) OR its mapped symbol (e.g., '!') for rank:");
            String iInput = JOptionPane.showInputDialog(this, "Enter index (i) for prefix [0..i]:");
            try {
                // We use only the first character. The controller handles lookup/translation.
                char c = cInput.toUpperCase().charAt(0); 
                int i = Integer.parseInt(iInput);
                displayMessage(controller.runRank(c, i));
            } catch (Exception ex) {
                displayMessage("⚠ Invalid input for token/symbol or index.");
            }
        });

        // SELECT
        selectButton.addActionListener(e -> {
            String cInput = JOptionPane.showInputDialog(this, "Enter token (e.g., ERROR) OR its mapped symbol (e.g., '!') for select:");
            String kInput = JOptionPane.showInputDialog(this, "Enter k (k-th occurrence):");
            try {
                char c = cInput.toUpperCase().charAt(0);
                int k = Integer.parseInt(kInput);
                if (k <= 0) throw new NumberFormatException();
                displayMessage(controller.runSelect(c, k));
            } catch (Exception ex) {
                displayMessage("⚠ Invalid input for token/symbol or k.");
            }
        });

        // QUANTILE
        quantileButton.addActionListener(e -> {
            try {
                String iInput = JOptionPane.showInputDialog(this, "Enter i (start index):");
                String jInput = JOptionPane.showInputDialog(this, "Enter j (end index):");
                String kInput = JOptionPane.showInputDialog(this, "Enter k (k-th smallest):");

                int i = Integer.parseInt(iInput);
                int j = Integer.parseInt(jInput);
                int k = Integer.parseInt(kInput);

                if (i < 0 || j < 0 || i > j || k <= 0) {
                    displayMessage("⚠ Invalid range or k value. Ensure i ≤ j and k > 0.");
                    return;
                }

                displayMessage(controller.runQuantile(i, j, k));

            } catch (Exception ex) {
                displayMessage("⚠ Invalid input. Enter integers for i, j, and k.");
            }
        });

        // RANGECOUNT
        rangeCountButton.addActionListener(e -> {
            try {
                String iInput = JOptionPane.showInputDialog(this, "Enter i (start index):");
                String jInput = JOptionPane.showInputDialog(this, "Enter j (end index):");
                String c1Input = JOptionPane.showInputDialog(this, "Enter c1 (start token/symbol):");
                String c2Input = JOptionPane.showInputDialog(this, "Enter c2 (end token/symbol):");

                int i = Integer.parseInt(iInput);
                int j = Integer.parseInt(jInput);
                
                // We use only the first character. The controller handles lookup/translation.
                char c1 = c1Input.toUpperCase().charAt(0);
                char c2 = c2Input.toUpperCase().charAt(0);

                if (i < 0 || j < 0 || i > j) {
                    displayMessage("⚠ Invalid index range. Ensure i ≤ j.");
                    return;
                }

                displayMessage(controller.runRangeCount(i, j, c1, c2));

            } catch (Exception ex) {
                displayMessage("⚠ Invalid input for indices or characters/tokens.");
            }
        });

        // BACK
        backButton.addActionListener(e -> showMainMenuPanel());

        optionsPanel.add(accessButton);
        optionsPanel.add(rankButton);
        optionsPanel.add(selectButton);
        optionsPanel.add(quantileButton);
        optionsPanel.add(rangeCountButton);
        optionsPanel.add(backButton);

        panel.add(optionsPanel, BorderLayout.SOUTH);
        return panel;
    }

    // --- PANEL CONTROL METHODS ---
    public void showMainMenuPanel() {
        outputArea.setText("");
        cardLayout.show(mainPanel, MAIN_MENU_PANEL);
    }

    public void showQueryPanel(String type, String snippet, int originalSize, int compressedSize, double ratio) {
        JPanel queryPanel = (JPanel) mainPanel.getComponent(1);
        JPanel infoPanel = null;

        for (Component comp : queryPanel.getComponents()) {
            if (comp instanceof JPanel && "InfoPanel".equals(comp.getName())) {
                infoPanel = (JPanel) comp;
                break;
            }
        }

        if (infoPanel != null) {
            infoPanel.removeAll();
            infoPanel.add(new JLabel("Data Type: " + type));
            infoPanel.add(new JLabel("Data Snippet: " + snippet));
            infoPanel.add(new JLabel("Original Size (bits): " + originalSize));
            infoPanel.add(new JLabel("Compressed Size (bits): " + compressedSize));
            infoPanel.add(new JLabel("Compression Ratio: " + df.format(ratio)));
            infoPanel.revalidate();
            infoPanel.repaint();
        }

        outputArea.setText("✅ Data compression complete.\n\nYou can now perform Wavelet Tree queries.\n");
        cardLayout.show(mainPanel, QUERY_PANEL);
    }

    public void displayMessage(String message) {
        outputArea.append(">> " + message + "\n");
    }
}
