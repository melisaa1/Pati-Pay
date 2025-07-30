
 
package patipayproject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class UserPanel extends JFrame {

    private final int userId;

    // Tablo bileşenleri
    private JTable donationTable;
    private DefaultTableModel donationModel;

    // Alt bağış paneli bileşenleri
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JComboBox<String> unitCombo;

    public UserPanel(int userId) {
        this.userId = userId;

        setTitle("🐾PatiPay - Kullanıcı Paneli🐾");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        buildHeader();
        buildCenterTable();   // <- JTable kur
        buildFooterDonate();  // <- Bağış giriş paneli

        // EKRANDA TABLOYU DOLDUR
        refreshDonationTable();

        setVisible(true);
    }

    private void buildHeader() {
        String username = UserService.getUsernameById(userId);
        JLabel welcomeLabel = new JLabel("Hoş geldin, " + username + " 👋", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(welcomeLabel, BorderLayout.NORTH);
    }

    /** ORTA KISIM: JTable kurulum */
    private void buildCenterTable() {
        String[] cols = {"ID", "Tür", "Tarih", "Miktar", "Birim"};
        donationModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        donationTable = new JTable(donationModel);
        donationTable.setAutoCreateRowSorter(true); // sütun başlığına tıklayıp sıralama

        JScrollPane scroll = new JScrollPane(donationTable);
        scroll.setBorder(BorderFactory.createTitledBorder("📋 Yaptığın Bağışlar"));
        add(scroll, BorderLayout.CENTER);
    }

    /** ALT KISIM: Bağış yapma paneli */
    private void buildFooterDonate() {
        JPanel donatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel selectLabel = new JLabel("Bağış Türü:");
        String[] types = {"Mama", "Su", "Para"};
        typeCombo = new JComboBox<>(types);

        JLabel amountLabel = new JLabel("Miktar:");
        amountField = new JTextField(8);

        JLabel unitLabel = new JLabel("Birim:");
        unitCombo = new JComboBox<>();

        // Tür değişince birimleri güncelle
        typeCombo.addActionListener(e -> populateUnitsByType());
        typeCombo.setSelectedIndex(0); // ilk açılışta tetikler (mama → kg/paket)

        JButton donateButton = new JButton("Bağış Yap");
        donateButton.setBackground(new Color(255, 153, 51));
        donateButton.addActionListener(e -> handleDonate());

        donatePanel.add(selectLabel);
        donatePanel.add(typeCombo);
        donatePanel.add(amountLabel);
        donatePanel.add(amountField);
        donatePanel.add(unitLabel);
        donatePanel.add(unitCombo);
        donatePanel.add(donateButton);

        add(donatePanel, BorderLayout.SOUTH);
    }

    /** Tür → birim listesi */
    private void populateUnitsByType() {
        String t = ((String) typeCombo.getSelectedItem()).toLowerCase();
        unitCombo.removeAllItems();
        switch (t) {
            case "mama":
                unitCombo.addItem("kg");
                unitCombo.addItem("paket");
                break;
            case "su":
                unitCombo.addItem("L");
                unitCombo.addItem("şişe");
                break;
            case "para":
                unitCombo.addItem("TRY");
                unitCombo.addItem("USD");
                unitCombo.addItem("EUR");
                break;
        }
        unitCombo.setSelectedIndex(0);
    }

    /** Bağış yap butonu davranışı */
    private void handleDonate() {
        String selectedType = (String) typeCombo.getSelectedItem();
        LocalDate today = LocalDate.now();

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen 0'dan büyük geçerli bir miktar girin.");
            return;
        }

        String unit = (String) unitCombo.getSelectedItem();
        if (unit == null || unit.isBlank()) {
            JOptionPane.showMessageDialog(this, "Lütfen birim seçin.");
            return;
        }

        boolean success = UserService.makeDonation(userId, selectedType, today, amount, unit);
        if (success) {
            JOptionPane.showMessageDialog(this, "✅ Bağış kaydedildi!");
            amountField.setText("");
            refreshDonationTable(); // TABLOYU YENİLE
        } else {
            JOptionPane.showMessageDialog(this, "❌ Bağış kaydedilemedi!");
        }
    }

    /** Tabloyu DB’den doldurur */
    private void refreshDonationTable() {
        donationModel.setRowCount(0);
        donationDAO dao = new donationDAO();
        List<Donation> donations = dao.getDonationsByUserId(userId);

        for (Donation d : donations) {
            donationModel.addRow(new Object[]{
                    d.getId(),
                    d.getTypeName().toUpperCase(),
                    d.getDate().toString(),
                    String.format("%.2f", d.getAmount()),
                    d.getUnit()
            });
        }
    }
}
