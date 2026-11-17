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

public class RandProductMaker extends JFrame {

    public static final int ID_LENGTH = 6;
    public static final int NAME_LENGTH = 35;
    public static final int DESCRIPTION_LENGTH = 75;
    public static final int COST_LENGTH = 8;
    public static final int RECORD_SIZE = ID_LENGTH + 2 + NAME_LENGTH + 2 + DESCRIPTION_LENGTH + 2 + COST_LENGTH + 1; // 6 + 2 + 35 + 2 + 75 + 2 + 8 + 1 = 131 bytes (including \n)

    public static final String DEFAULT_FILE_NAME = "data/ProductTestData.csv";

    private JTextField idField;
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField costField;
    private JTextField recordCountField;

    private JButton addButton;
    private JButton clearButton;
    private JButton exitButton;

    private RandomAccessFile randomAccessFile;
    private int recordCount = 0;
    private File dataFile;

    public RandProductMaker() {
        this(DEFAULT_FILE_NAME);
    }

    public RandProductMaker(String fileName) {
        super("Random Product Maker");
        dataFile = new File(fileName);
        initFile();
        initUI();
    }

    private void initFile() {
        try {
            if (dataFile.getParentFile() != null && !dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            randomAccessFile = new RandomAccessFile(dataFile, "rw");
            long fileLength = randomAccessFile.length();
            recordCount = (int) (fileLength / RECORD_SIZE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error creating/opening file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file length: " + e.getMessage(),
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

        // Header Panel
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Random Product Maker");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        headerPanel.add(titleLabel);
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 20, 10, 20),
                new TitledBorder("Enter Product Data")
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Product ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Product ID (max 6 chars):"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        idField = new JTextField(15);
        formPanel.add(idField, gbc);

        // Row 1: Product Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Product Name (max 35 chars):"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(25);
        formPanel.add(nameField, gbc);

        // Row 2: Description
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Description (max 75 chars):"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        descriptionField = new JTextField(35);
        formPanel.add(descriptionField, gbc);

        // Row 3: Cost
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Cost ($):"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        costField = new JTextField(15);
        formPanel.add(costField, gbc);

        // Row 4: Record Count
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Record Count:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        recordCountField = new JTextField(String.valueOf(recordCount), 10);
        recordCountField.setEditable(false);
        recordCountField.setFocusable(false);
        recordCountField.setBackground(new Color(235, 235, 235));
        formPanel.add(recordCountField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 15, 10));

        addButton = new JButton("Add Record");
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.addActionListener(e -> addRecord());

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 14));
        clearButton.addActionListener(e -> clearInputFields());

        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exitButton.addActionListener(e -> {
            closeFile();
            System.exit(0);
        });

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(520, 380));
        setLocationRelativeTo(null);
    }

    private void addRecord() {
        String idText = idField.getText().trim();
        String nameText = nameField.getText().trim();
        String descText = descriptionField.getText().trim();
        String costText = costField.getText().trim();

        // 1. Confirm that all data fields are entered
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product ID cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            idField.requestFocus();
            return;
        }

        if (nameText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (descText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product Description cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            descriptionField.requestFocus();
            return;
        }

        if (costText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product Cost cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            costField.requestFocus();
            return;
        }

        // 2. Validate field lengths and types
        if (idText.length() > ID_LENGTH) {
            JOptionPane.showMessageDialog(this, "Product ID cannot exceed " + ID_LENGTH + " characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            idField.requestFocus();
            return;
        }

        if (nameText.length() > NAME_LENGTH) {
            JOptionPane.showMessageDialog(this, "Product Name cannot exceed " + NAME_LENGTH + " characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (descText.length() > DESCRIPTION_LENGTH) {
            JOptionPane.showMessageDialog(this, "Product Description cannot exceed " + DESCRIPTION_LENGTH + " characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            descriptionField.requestFocus();
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costText);
            if (cost < 0) {
                JOptionPane.showMessageDialog(this, "Product Cost cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                costField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Product Cost must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            costField.requestFocus();
            return;
        }

        // 3. Create Product object
        Product product = new Product(idText, nameText, descText, cost);

        // 4. Pad fields and write to RandomAccessFile
        try {
            if (randomAccessFile == null) {
                if (dataFile.getParentFile() != null && !dataFile.getParentFile().exists()) {
                    dataFile.getParentFile().mkdirs();
                }
                randomAccessFile = new RandomAccessFile(dataFile, "rw");
            }
            // Move file pointer to end of file
            randomAccessFile.seek(randomAccessFile.length());

            // Get padded CSV record from Product with newline
            String paddedRecord = product.toPaddedCSV() + "\n";

            // Write record as ASCII bytes
            randomAccessFile.write(paddedRecord.getBytes(StandardCharsets.US_ASCII));

            // Increment and update record count
            recordCount = (int) (randomAccessFile.length() / RECORD_SIZE);
            recordCountField.setText(String.valueOf(recordCount));

            JOptionPane.showMessageDialog(this, "Record added successfully!\nRecord count: " + recordCount,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // 5. Clear all field displays each time between record input
            clearInputFields();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error writing record to file: " + e.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInputFields() {
        idField.setText("");
        nameField.setText("");
        descriptionField.setText("");
        costField.setText("");
        idField.requestFocus();
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
            RandProductMaker frame = new RandProductMaker();
            frame.setVisible(true);
        });
    }
}
