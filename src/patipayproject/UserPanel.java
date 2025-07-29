
 
package patipayproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class UserPanel extends JFrame {
    
    private JPanel donationListPanel;
    private int userId;

    public UserPanel(int userId) {
        this.userId = userId;

        setTitle("🐾PatiPay - Kullanıcı Paneli🐾");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // === ÜST ===
        String username = UserService.getUsernameById(userId);
        JLabel welcomeLabel = new JLabel("Hoş geldin, " + username + " 👋", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // === ORTA ===
        donationListPanel = new JPanel();
        donationListPanel.setLayout(new BoxLayout(donationListPanel, BoxLayout.Y_AXIS));
        refreshDonationList();

        JScrollPane scrollPane = new JScrollPane(donationListPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Yaptığın Bağışlar"));
        add(scrollPane, BorderLayout.CENTER);

        // === ALT: Bağış Yapma Paneli ===
        JPanel donatePanel = new JPanel(new FlowLayout());

        JLabel selectLabel = new JLabel("Bağış Türü Seç:");
        String[] types = {"Mama", "Su", "Para"};
        JComboBox<String> typeComboBox = new JComboBox<>(types);
        JButton donateButton = new JButton("Bağış Yap");
        donateButton.setBackground(new Color(255,153,51));

        donatePanel.add(selectLabel);
        donatePanel.add(typeComboBox);
        donatePanel.add(donateButton);

        // Bağış yap butonu işlevi
        donateButton.addActionListener(e -> {
            String selectedType = (String) typeComboBox.getSelectedItem();
            String today = LocalDate.now().toString();

            // Veritabanına bağış kaydet
            boolean success = UserService.makeDonation(UserService.getUsernameById(userId), selectedType, today);

            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Bağış kaydedildi!");
                refreshDonationList();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Bağış kaydedilemedi!");
            }
        });

        add(donatePanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Bağış listesini yenile
    private void refreshDonationList() {
        donationListPanel.removeAll();

        donationDAO donationDao = new donationDAO();
        List<Donation> donations = donationDao.getDonationsByUserId(userId);

        if (donations.isEmpty()) {
            JLabel noDonationLabel = new JLabel("Henüz bir bağış yapılmamış.");
            noDonationLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            donationListPanel.add(noDonationLabel);
        } else {
            for (Donation d : donations) {
                JLabel donationLabel = new JLabel(formatDonation(d));
                donationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                donationLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                donationListPanel.add(donationLabel);
            }
        }

        donationListPanel.revalidate();
        donationListPanel.repaint();
    }

    // İkon ve bilgiyle bağış yazısı
    private String formatDonation(Donation donation) {
        String icon = switch (donation.getTypeName().toLowerCase()) {
            case "mama" -> "🍖";
            case "su" -> "💧";
            case "para" -> "💰";
            default -> "❓";
        };

        return icon + " " + donation.getTypeName().toUpperCase() + " - 📅 " + donation.getDate();
    }
}
