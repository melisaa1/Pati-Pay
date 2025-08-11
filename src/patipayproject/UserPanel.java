
 
package patipayproject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


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

    private JLabel profilePicLabel; // Profil fotoğrafını gösterecek label

    public UserPanel(int userId) {
        this.userId = userId;

        setTitle("🐾 PatiPay - Kullanıcı Paneli 🐾");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // Arka plan paneli (LoginFrame gibi değil, sade açık renk)
        BackgroundPanel backgroundPanel = new BackgroundPanel("src/assets/userpanel_bg.png"); // farklı arka plan
        backgroundPanel.setLayout(new BorderLayout(15, 15));
        setContentPane(backgroundPanel);

        buildHeader();
        buildCenterPanel();
        buildFooterDonate();

        refreshDonationTable();
        updateScoreLabel();

        setVisible(true);
    }

    // Kullanıcı paneline özgü farklı arka plan sınıfı (LoginFrame’den farklı)
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                backgroundImage = null;
            }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Kenar boşlukları bırakıp resmi ortala
                int imgWidth = backgroundImage.getWidth(this);
                int imgHeight = backgroundImage.getHeight(this);
                int x = (getWidth() - imgWidth) / 2;
                int y = (getHeight() - imgHeight) / 2;
                g.drawImage(backgroundImage, x, y, this);
            }
        }
    }

    private void buildHeader() {
        String username = UserService.getUsernameById(userId);

        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false); // arka plan şeffaf

        // Profil resmi yuvarlak ve mouse hover border efekti
        BufferedImage defaultImg = loadImage("src/assets/pic1.png"); // farklı profil resmi
        Image circleImage = defaultImg != null ? makeRoundedCorner(defaultImg, 70) : null;
        profilePicLabel = new JLabel(circleImage != null ? new ImageIcon(circleImage) : new JLabel("Foto yok").getIcon());
        profilePicLabel.setToolTipText("Profil fotoğrafını değiştir");
        profilePicLabel.setPreferredSize(new Dimension(80, 80));
        profilePicLabel.setOpaque(false);
        profilePicLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profilePicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                profilePicLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 3));
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                profilePicLabel.setBorder(null);
            }
            @Override
            public void mouseClicked(MouseEvent evt) {
                openProfileSelection();
            }
        });

        JButton selectProfileButton = new JButton("Profil Seç");
        styleButton(selectProfileButton, new Color(0, 120, 215));
        selectProfileButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectProfileButton.setFocusPainted(false);
        selectProfileButton.addActionListener(e -> openProfileSelection());

        JLabel welcomeLabel = new JLabel("Hoş geldin " + username + " 👋", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(0, 120, 215));

        // Çıkış butonu modern stil ile
        JButton exitButton = new JButton("✖");
        exitButton.setBackground(new Color(245, 245, 245));
        exitButton.setForeground(new Color(150, 0, 0));
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setFocusPainted(false);
        exitButton.setPreferredSize(new Dimension(20, 20));
        exitButton.setToolTipText("Çıkış yap");
        exitButton.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        exitButton.addActionListener(e -> {
            int onay = JOptionPane.showConfirmDialog(this, "Uygulamadan çıkmak istiyor musunuz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if (onay == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setOpaque(false);
        leftPanel.add(profilePicLabel);
        leftPanel.add(selectProfileButton);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        headerPanel.add(exitButton, BorderLayout.EAST);

        getContentPane().add(headerPanel, BorderLayout.NORTH);
    }

    // Profil seçim penceresi (aynı, küçük grid ile)
    private void openProfileSelection() {
        String[] profileImages = {"pic1.png", "pic2.png", "pic3.png", "pic4.png", "pic5.png", "pic6.png", "pic7.png",
                "pic8.png", "pic9.png", "pic10.png", "pic11.png", "pic12.png", "pic13.png", "pic14.png", "pic15.png"};

        JPanel panel = new JPanel(new GridLayout(3, 5, 10, 10));
        panel.setBackground(Color.WHITE);

        for (String imgName : profileImages) {
            String imgPath = "src/assets/" + imgName;
            BufferedImage img = loadImage(imgPath);
            if (img != null) {
                Image scaled = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                JButton imgButton = new JButton(new ImageIcon(scaled));
                imgButton.setPreferredSize(new Dimension(90, 90));
                imgButton.setFocusPainted(false);
                imgButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                imgButton.addActionListener(e -> {
                    BufferedImage rounded = makeRoundedCorner(img, 70);
                    profilePicLabel.setIcon(new ImageIcon(rounded));
                    SwingUtilities.getWindowAncestor(panel).dispose();
                });
                panel.add(imgButton);
            }
        }

        JDialog dialog = new JDialog(this, "Profil Fotoğrafı Seç", true);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void buildCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        centerPanel.add(buildFilterPanel(), BorderLayout.NORTH);
        centerPanel.add(buildCenterTable(), BorderLayout.CENTER);

        getContentPane().add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 120, 215)), "Filtrele"));

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
        styleButton(filterBtn, new Color(0, 120, 215));
        JButton clearBtn = new JButton("Temizle");
        styleButton(clearBtn, new Color(0, 120, 215));

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
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        donationTable = new JTable(donationModel);
        donationTable.setRowHeight(28);
        donationTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        donationTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        donationTable.getTableHeader().setBackground(new Color(0, 120, 215));
        donationTable.getTableHeader().setForeground(Color.WHITE);
        donationTable.setSelectionBackground(new Color(176, 224, 230));

        JScrollPane scroll = new JScrollPane(donationTable);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 120, 215)), "Yaptığın Bağışlar"));
        return scroll;
    }

    private void buildFooterDonate() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);

        JPanel donatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        donatePanel.setOpaque(false);

        typeCombo = new JComboBox<>(new String[]{"Mama", "Su", "Para"});
        typeCombo.addActionListener(e -> populateUnitsByType());

        amountField = new JTextField(8);
        unitCombo = new JComboBox<>();
        populateUnitsByType();

        JButton donateButton = new JButton("Bağış Yap");
        donateButton.setPreferredSize(new Dimension(110, 35));
        donateButton.setBackground(new Color(0, 120, 215));
        donateButton.setForeground(Color.BLACK);
        donateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        donateButton.setFocusPainted(false);
        donateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        donateButton.addActionListener(e -> handleDonate());

        donatePanel.add(new JLabel("Bağış Türü:"));
        donatePanel.add(typeCombo);
        donatePanel.add(new JLabel("Miktar:"));
        donatePanel.add(amountField);
        donatePanel.add(new JLabel("Birim:"));
        donatePanel.add(unitCombo);
        donatePanel.add(donateButton);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scorePanel.setOpaque(false);

        scoreValueLabel = new JLabel("🌟 0.00");
        scoreValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreValueLabel.setForeground(new Color(0, 120, 215));

        scorePanel.add(new JLabel("Toplam Puan: "));
        scorePanel.add(scoreValueLabel);

        footerPanel.add(donatePanel, BorderLayout.WEST);
        footerPanel.add(scorePanel, BorderLayout.EAST);

        getContentPane().add(footerPanel, BorderLayout.SOUTH);
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

    // Button stillendirme metodu (LoginFrame'den kopyalandı, renk ve hover ile)
    private void styleButton(JButton button, Color borderColor) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(borderColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(borderColor);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(borderColor);
            }
        });
    }

    // Image'ı BufferedImage'a çevirir
    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    // BufferedImage'ı yuvarlak köşeli yapar
      private BufferedImage makeRoundedCorner(BufferedImage image, int diameter) {
        BufferedImage output = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // Antialiasing aktif et
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Şeffaf fonksiyon
        g2.setComposite(AlphaComposite.Src);

        // Yuvarlak maske çiz
        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, diameter, diameter);

        // Maske olarak yuvarlak oluştur
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, diameter, diameter, null);

        g2.dispose();

        return output;
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Resim yüklenemedi: " + path);
            return null;
        }
    }
}
