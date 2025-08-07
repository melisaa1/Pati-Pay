
 
package patipayproject;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;


// GÜNCELLENMİŞ VE DÜZENLENMİŞ TAM SINIF

public class UserPanel extends JFrame {
    private final int userId;
    private JTable donationTable;
    private DefaultTableModel donationModel;
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JComboBox<String> unitCombo;
    private JLabel scoreValueLabel;

    private JComboBox<String> filterTypeCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    public UserPanel(int userId) {
        this.userId = userId;

        setTitle("🐾 PatiPay - User Panel 🐾");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        buildHeader();
        buildCenterPanel();
        buildFooterDonate();

        refreshDonationTable();
        updateScoreLabel();

        setVisible(true);
    }

    private void buildHeader() {
        String username = UserService.getUsernameById(userId);

        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImageIcon defaultIcon = new ImageIcon("src/assets/default_user.png");
        Image scaledImage = defaultIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel profilePicLabel = new JLabel(new ImageIcon(scaledImage));
        profilePicLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profilePicLabel.setToolTipText("Profil fotoğrafını değiştir");

        profilePicLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                int result = chooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    ImageIcon newIcon = new ImageIcon(file.getAbsolutePath());
                    Image resized = newIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    profilePicLabel.setIcon(new ImageIcon(resized));
                }
            }
        });

        JLabel welcomeLabel = new JLabel("Hoş geldin " + username + " 👋", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(60, 60, 60));

        JButton exitButton = new JButton("✖");
        exitButton.setBackground(new Color(230, 230, 230));
        exitButton.setForeground(new Color(150, 0, 0));
        exitButton.setFont(new Font("Arial", Font.BOLD, 13));
        exitButton.setFocusPainted(false);
        exitButton.setPreferredSize(new Dimension(30, 25));
        exitButton.setToolTipText("Çıkış yap");
        exitButton.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        exitButton.addActionListener(e -> {
            int onay = JOptionPane.showConfirmDialog(this, "Uygulamadan çıkmak istiyor musunuz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if (onay == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(profilePicLabel, BorderLayout.WEST);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        headerPanel.add(exitButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void buildCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.add(buildFilterPanel(), BorderLayout.NORTH);
        centerPanel.add(buildCenterTable(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(new Color(250, 250, 250));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrele"));

        SpinnerDateModel startModel = new SpinnerDateModel();
        SpinnerDateModel endModel = new SpinnerDateModel();
        startDateSpinner = new JSpinner(startModel);
        endDateSpinner = new JSpinner(endModel);

        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));

        filterTypeCombo = new JComboBox<>(new String[]{"Tümü", "Mama", "Su", "Para"});

        filterPanel.add(new JLabel("Başlangıç Tarihi:"));
        filterPanel.add(startDateSpinner);
        filterPanel.add(new JLabel("Bitiş Tarihi:"));
        filterPanel.add(endDateSpinner);
        filterPanel.add(new JLabel("Tür:"));
        filterPanel.add(filterTypeCombo);

        JButton filterBtn = new JButton("Filtrele");
        JButton clearBtn = new JButton("Temizle");

        filterBtn.addActionListener(e -> applyFilter());
        clearBtn.addActionListener(e -> {
            startDateSpinner.setValue(new java.util.Date());
            endDateSpinner.setValue(new java.util.Date());
            filterTypeCombo.setSelectedIndex(0);
            refreshDonationTable();
        });

        filterPanel.add(filterBtn);
        filterPanel.add(clearBtn);

        return filterPanel;
    }

    private JScrollPane buildCenterTable() {
        String[] cols = {"ID", "Tür", "Tarih", "Miktar", "Birim"};
        donationModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        donationTable = new JTable(donationModel);
        donationTable.setRowHeight(28);
        donationTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        donationTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        donationTable.getTableHeader().setBackground(new Color(230, 230, 230));
        donationTable.getTableHeader().setForeground(new Color(0, 120, 215));
        donationTable.setSelectionBackground(new Color(176, 224, 230));

        JScrollPane scroll = new JScrollPane(donationTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Yaptığın Bağışlar"));
        return scroll;
    }

    private void buildFooterDonate() {
        JPanel footerPanel = new JPanel(new BorderLayout());

        // Sol: Bağış yapma formu
        JPanel donatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        donatePanel.setBackground(new Color(245, 245, 245));

        typeCombo = new JComboBox<>(new String[]{"Mama", "Su", "Para"});
        typeCombo.addActionListener(e -> populateUnitsByType());

        amountField = new JTextField(8);
        unitCombo = new JComboBox<>();
        populateUnitsByType();

        JButton donateButton = new JButton("Bağış Yap");
        donateButton.setPreferredSize(new Dimension(110, 35));
        donateButton.setBackground(new Color(255, 140, 0));
        donateButton.setFont(new Font("Arial", Font.BOLD, 14));
        donateButton.setFocusPainted(false);
        donateButton.addActionListener(e -> handleDonate());

        donatePanel.add(new JLabel("Bağış Türü:"));
        donatePanel.add(typeCombo);
        donatePanel.add(new JLabel("Miktar:"));
        donatePanel.add(amountField);
        donatePanel.add(new JLabel("Birim:"));
        donatePanel.add(unitCombo);
        donatePanel.add(donateButton);

        // Sağ: Puan kutusu
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scorePanel.setBackground(new Color(245, 245, 245));

        scoreValueLabel = new JLabel("🌟 0.00");
        scoreValueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreValueLabel.setForeground(new Color(0, 120, 215));

        scorePanel.add(new JLabel("Toplam Puan: "));
        scorePanel.add(scoreValueLabel);

        footerPanel.add(donatePanel, BorderLayout.WEST);
        footerPanel.add(scorePanel, BorderLayout.EAST);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void populateUnitsByType() {
        String t = ((String) typeCombo.getSelectedItem()).toLowerCase();
        unitCombo.removeAllItems();
        switch (t) {
            case "mama" -> {
                unitCombo.addItem("kg");
                unitCombo.addItem("paket");
            }
            case "su" -> {
                unitCombo.addItem("L");
                unitCombo.addItem("şişe");
            }
            case "para" -> {
                unitCombo.addItem("TRY");
                unitCombo.addItem("USD");
                unitCombo.addItem("EUR");
            }
        }
        unitCombo.setSelectedIndex(0);
    }

    private void handleDonate() {
        String selectedType = (String) typeCombo.getSelectedItem();
        LocalDate today = LocalDate.now();

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir miktar girin.");
            return;
        }

        String unit = (String) unitCombo.getSelectedItem();
        if (unit == null || unit.isBlank()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir birim seçin.");
            return;
        }

        boolean success = UserService.makeDonation(userId, selectedType, today, amount, unit);
        if (success) {
            double earnedScore = calculateScore(selectedType, amount);
            JOptionPane.showMessageDialog(this,
                    "✅ Bağış kaydedildi!\n🎉 Tebrikler, bu bağıştan " +
                            String.format("%.2f", earnedScore) + " puan kazandınız!");

            amountField.setText("");
            refreshDonationTable();
            updateScoreLabel();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Bağış kaydedilemedi.");
        }
    }

    private double calculateScore(String type, double amount) {
        return switch (type.toLowerCase()) {
            case "para" -> amount;
            case "mama" -> amount * 0.8;
            case "su" -> amount * 0.6;
            default -> 0;
        };
    }

    private void refreshDonationTable() {
        donationModel.setRowCount(0);
        List<Donation> donations = UserService.getDonationsByUserId(userId);
        fillTable(donations);
    }

    private void applyFilter() {
        java.util.Date startDateRaw = (java.util.Date) startDateSpinner.getValue();
        java.util.Date endDateRaw = (java.util.Date) endDateSpinner.getValue();
        LocalDate startDate = LocalDate.ofInstant(startDateRaw.toInstant(), java.time.ZoneId.systemDefault());
        LocalDate endDate = LocalDate.ofInstant(endDateRaw.toInstant(), java.time.ZoneId.systemDefault());

        String type = (String) filterTypeCombo.getSelectedItem();

        List<Donation> filtered = UserService.getFilteredDonations(userId, type, startDate, endDate);
        donationModel.setRowCount(0);
        fillTable(filtered);
    }

    private void fillTable(List<Donation> data) {
        if (data.isEmpty()) {
            donationModel.addRow(new Object[]{"-", "Bağış bulunamadı", "-", "-", "-"});
            return;
        }

        for (Donation d : data) {
            donationModel.addRow(new Object[]{
                    d.getId(),
                    d.getTypeName(),
                    d.getDate().toString(),
                    String.format("%.2f", d.getAmount()),
                    d.getUnit()
            });
        }
    }

    private void updateScoreLabel() {
        double score = UserService.getTotalScoreByUserId(userId);
        scoreValueLabel.setText("🌟 " + String.format("%.2f", score));
    }
}
