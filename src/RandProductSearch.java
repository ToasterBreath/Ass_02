import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RandProductSearch extends JFrame {

    public static final int ID_LENGTH = 6;
    public static final int NAME_LENGTH = 35;
    public static final int DESCRIPTION_LENGTH = 75;
    public static final int COST_LENGTH = 8;
    public static final int RECORD_SIZE = ID_LENGTH + 2 + NAME_LENGTH + 2 + DESCRIPTION_LENGTH + 2 + COST_LENGTH + 1; // 131 bytes

    public static final String DEFAULT_FILE_NAME = "data/ProductTestData.csv";

    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JButton exitButton;
    private JTextArea resultsArea;
    private JLabel statusLabel;

    private RandomAccessFile randomAccessFile;
    private File dataFile;

    public RandProductSearch() {
        this(DEFAULT_FILE_NAME);
    }

    public RandProductSearch(String fileName) {
        super("Random Product Search");
        dataFile = new File(fileName);
        initFile();
        initUI();
    }

    private void initFile() {
        try {
            if (dataFile.exists()) {
                randomAccessFile = new RandomAccessFile(dataFile, "r");
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Could not open data file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Window closing handler to properly close the file
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeFile();
            }
        });

        // Header & Search Control Panel (North)
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Random Product Search");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titlePanel.add(titleLabel);
        northPanel.add(titlePanel);

        // Search Input Panel
        JPanel searchInputPanel = new JPanel(new GridBagLayout());
        searchInputPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 15, 5, 15),
                new TitledBorder("Search Products")
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel searchPrompt = new JLabel("Product Name (full or partial):");
        searchPrompt.setFont(new Font("Arial", Font.PLAIN, 14));
        searchInputPanel.add(searchPrompt, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchInputPanel.add(searchField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.addActionListener(e -> performSearch());
        searchInputPanel.add(searchButton, gbc);

        gbc.gridx = 3;
        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 14));
        clearButton.addActionListener(e -> clearSearch());
        searchInputPanel.add(clearButton, gbc);

        northPanel.add(searchInputPanel);
        add(northPanel, BorderLayout.NORTH);

        // Results Display Panel (Center)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 15, 5, 15),
                new TitledBorder("Search Results")
        ));

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultsArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Footer Panel (South)
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new EmptyBorder(5, 15, 15, 15));

        statusLabel = new JLabel("Enter a search term above and click Search.");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        southPanel.add(statusLabel, BorderLayout.WEST);

        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exitButton.addActionListener(e -> {
            closeFile();
            System.exit(0);
        });
        southPanel.add(exitButton, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);

        // Bind Enter key to search
        getRootPane().setDefaultButton(searchButton);

        pack();
        setSize(850, 520);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);
    }

    private void performSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a product name or partial name to search.",
                    "Input Warning", JOptionPane.WARNING_MESSAGE);
            searchField.requestFocus();
            return;
        }

        if (!dataFile.exists() || dataFile.length() == 0) {
            resultsArea.setText("");
            statusLabel.setText("Data file is empty or does not exist (" + dataFile.getPath() + ").");
            JOptionPane.showMessageDialog(this, "The data file does not exist or contains no records:\n" + dataFile.getPath(),
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (randomAccessFile == null) {
                randomAccessFile = new RandomAccessFile(dataFile, "r");
            }

            long fileLength = randomAccessFile.length();
            int totalRecords = (int) (fileLength / RECORD_SIZE);
            List<Product> matches = new ArrayList<>();

            byte[] recordBuffer = new byte[RECORD_SIZE];
            for (int i = 0; i < totalRecords; i++) {
                randomAccessFile.seek((long) i * RECORD_SIZE);
                randomAccessFile.readFully(recordBuffer);

                String recordLine = new String(recordBuffer, StandardCharsets.US_ASCII);
                Product product = parseRecord(recordLine);

                if (product != null && product.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    matches.add(product);
                }
            }

            displayResults(searchTerm, matches, totalRecords);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading from data file: " + e.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Product parseRecord(String recordLine) {
        try {
            // Record format: ID (6), Name (35), Description (75), Cost (8) separated by ", "
            if (recordLine.length() < RECORD_SIZE - 1) { // -1 in case of trailing newline variation
                return null;
            }

            String idPart = recordLine.substring(0, ID_LENGTH).trim();
            int nameStart = ID_LENGTH + 2;
            String namePart = recordLine.substring(nameStart, nameStart + NAME_LENGTH).trim();
            int descStart = nameStart + NAME_LENGTH + 2;
            String descPart = recordLine.substring(descStart, descStart + DESCRIPTION_LENGTH).trim();
            int costStart = descStart + DESCRIPTION_LENGTH + 2;
            String costPart = recordLine.substring(costStart, costStart + COST_LENGTH).trim();

            double cost = Double.parseDouble(costPart);
            return new Product(idPart, namePart, descPart, cost);
        } catch (Exception e) {
            // Fallback parsing by splitting comma
            try {
                String[] parts = recordLine.trim().split(",\\s*");
                if (parts.length >= 4) {
                    return new Product(parts[0].trim(), parts[1].trim(), parts[2].trim(), Double.parseDouble(parts[3].trim()));
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private void displayResults(String searchTerm, List<Product> matches, int totalRecords) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Search results for: \"%s\"\n", searchTerm));
        sb.append(String.format("Matches found: %d of %d total record(s)\n\n", matches.size(), totalRecords));

        if (matches.isEmpty()) {
            sb.append("No products matched the search criteria.\n");
            statusLabel.setText(String.format("No matches found for \"%s\" (%d records scanned).", searchTerm, totalRecords));
        } else {
            String formatHeader = "%-8s %-35s %-45s %10s\n";
            String formatRow = "%-8s %-35s %-45s %10.2f\n";

            sb.append(String.format(formatHeader, "ID", "Name", "Description", "Cost ($)"));
            sb.append(String.format(formatHeader, "========", "===================================", "=============================================", "=========="));

            for (Product p : matches) {
                String desc = p.getDescription();
                if (desc.length() > 45) {
                    desc = desc.substring(0, 42) + "...";
                }
                sb.append(String.format(formatRow, p.getID(), p.getName(), desc, p.getCost()));
            }

            statusLabel.setText(String.format("Found %d match(es) for \"%s\" out of %d total record(s).",
                    matches.size(), searchTerm, totalRecords));
        }

        resultsArea.setText(sb.toString());
        resultsArea.setCaretPosition(0);
    }

    private void clearSearch() {
        searchField.setText("");
        resultsArea.setText("");
        statusLabel.setText("Search cleared. Enter a new search term.");
        searchField.requestFocus();
    }

    private void closeFile() {
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                System.err.println("Error closing RandomAccessFile: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RandProductSearch frame = new RandProductSearch();
            frame.setVisible(true);
        });
    }
}
