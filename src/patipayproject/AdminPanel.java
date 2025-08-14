
package patipayproject;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.border.TitledBorder;

public class AdminPanel extends JFrame {

    private JTextField usernameField;
    private JComboBox<String> typeCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JTable table;
    private DefaultTableModel model;

    public AdminPanel(String username) {
        setTitle("🐾 PatiPay - Yönetici Paneli 🐾");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new BorderLayout());

        String welcomeText = "Hoş geldin " + username + " 👋";
        JLabel welcomeLabel = new JLabel(welcomeText, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        welcomeLabel.setForeground(new Color(0, 120, 215));

        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(buildTopRightButtons(username), BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel filtrePanel = new JPanel(new BorderLayout());
        Color filterBorderColor = new Color(0, 120, 215);
        filtrePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(filterBorderColor, 1),
                "Filtrele",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                filtrePanel.getFont().deriveFont(Font.BOLD, 14f),
                filterBorderColor
        ));
        filtrePanel.add(buildFilterBar(), BorderLayout.CENTER);

        centerPanel.add(filtrePanel, BorderLayout.NORTH);
        centerPanel.add(buildTable(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        add(buildButtonsBar(), BorderLayout.SOUTH);

        applyFilters("date DESC");
        setVisible(true);
    }

    private JPanel buildTopRightButtons(String username) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 8));

        JButton changePassBtn = new JButton("🔑");
        Color red = new Color(180, 0, 0);
        changePassBtn.setBackground(Color.WHITE);
        changePassBtn.setForeground(red);
        changePassBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        changePassBtn.setFocusPainted(false);
        changePassBtn.setBorder(BorderFactory.createLineBorder(red, 2));
        changePassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePassBtn.setPreferredSize(new Dimension(40, 28));

        changePassBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                changePassBtn.setBackground(red);
                changePassBtn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                changePassBtn.setBackground(Color.WHITE);
                changePassBtn.setForeground(red);
            }
        });

        changePassBtn.addActionListener(e -> showChangePasswordDialog(username));

        JButton exitBtn = new JButton("✖");
        Color redExit = new Color(180, 0, 0);
        exitBtn.setBackground(Color.WHITE);
        exitBtn.setForeground(redExit);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        exitBtn.setFocusPainted(false);
        exitBtn.setBorder(BorderFactory.createLineBorder(redExit, 2));
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.setPreferredSize(new Dimension(45, 30));

        exitBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                exitBtn.setBackground(redExit);
                exitBtn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                exitBtn.setBackground(Color.WHITE);
                exitBtn.setForeground(redExit);
            }
        });

        exitBtn.addActionListener(e -> {
            int onay = JOptionPane.showConfirmDialog(this, "Uygulamadan çıkmak istiyor musunuz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if (onay == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        panel.add(changePassBtn);
        panel.add(exitBtn);
        return panel;
    }

   private void showChangePasswordDialog(String username) {
    JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));

    JPasswordField oldPass = new JPasswordField();
    JPasswordField newPass = new JPasswordField();
    JPasswordField confirmPass = new JPasswordField();

    JButton showOldBtn = new JButton("👁");
    JButton showNewBtn = new JButton("👁");
    JButton showConfirmBtn = new JButton("👁");

    Dimension eyeSize = new Dimension(30, 25);
    showOldBtn.setPreferredSize(eyeSize);
    showNewBtn.setPreferredSize(eyeSize);
    showConfirmBtn.setPreferredSize(eyeSize);

    ActionListener toggleVisibility = e -> {
        JButton btn = (JButton) e.getSource();
        JPasswordField target;
        if (btn == showOldBtn) target = oldPass;
        else if (btn == showNewBtn) target = newPass;
        else target = confirmPass;

        if (target.getEchoChar() == '\u2022') {
            target.setEchoChar((char) 0);
        } else {
            target.setEchoChar('\u2022');
        }
    };

    showOldBtn.addActionListener(toggleVisibility);
    showNewBtn.addActionListener(toggleVisibility);
    showConfirmBtn.addActionListener(toggleVisibility);

    JPanel oldPassPanel = new JPanel(new BorderLayout());
    oldPassPanel.add(oldPass, BorderLayout.CENTER);
    oldPassPanel.add(showOldBtn, BorderLayout.EAST);

    JPanel newPassPanel = new JPanel(new BorderLayout());
    newPassPanel.add(newPass, BorderLayout.CENTER);
    newPassPanel.add(showNewBtn, BorderLayout.EAST);

    JPanel confirmPassPanel = new JPanel(new BorderLayout());
    confirmPassPanel.add(confirmPass, BorderLayout.CENTER);
    confirmPassPanel.add(showConfirmBtn, BorderLayout.EAST);

    panel.add(new JLabel("Eski Şifre:"));
    panel.add(oldPassPanel);
    panel.add(new JLabel("Yeni Şifre:"));
    panel.add(newPassPanel);
    panel.add(new JLabel("Yeni Şifre(Tekrar):"));
    panel.add(confirmPassPanel);

    int result = JOptionPane.showConfirmDialog(this, panel, "Şifre Değiştir", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        String oldP = new String(oldPass.getPassword());
        String newP = new String(newPass.getPassword());
        String confirmP = new String(confirmPass.getPassword());

        if (oldP.isEmpty() || newP.isEmpty() || confirmP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun.");
            return;
        }
        if (!newP.equals(confirmP)) {
            JOptionPane.showMessageDialog(this, "Yeni şifreler eşleşmiyor.");
            return;
        }
        if (!AdminService.checkPasswordByUsername(username, oldP)) {
            JOptionPane.showMessageDialog(this, "Eski şifre yanlış.");
            return;
        }
         if (oldP.equals(newP)) {
            JOptionPane.showMessageDialog(this, "Yeni şifre eski şifreyle aynı olamaz!");
            return;
        }

        boolean updated = AdminService.updatePasswordByUsername(username, oldP, newP);
        if (updated) {
            JOptionPane.showMessageDialog(this, "Şifre başarıyla değiştirildi.");
        } else {
            JOptionPane.showMessageDialog(this, "Şifre güncellenirken hata oluştu.");
        }
    }
   }

    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

        p.add(new JLabel("Kullanıcı adı:"));
        usernameField = new JTextField(12);
        p.add(usernameField);

        p.add(new JLabel("Tür:"));
        typeCombo = new JComboBox<>(new String[]{"Hepsi", "Mama", "Su", "Para"});
        p.add(typeCombo);

        SpinnerDateModel startModel = new SpinnerDateModel();
        SpinnerDateModel endModel = new SpinnerDateModel();

        startDateSpinner = new JSpinner(startModel);
        endDateSpinner = new JSpinner(endModel);

        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        endDateSpinner.setEditor(endEditor);

        p.add(new JLabel("Başlangıç Tarihi:"));
        p.add(startDateSpinner);

        p.add(new JLabel("Bitiş Tarihi:"));
        p.add(endDateSpinner);

        JButton applyBtn = new JButton("Filtrele");
        JButton resetBtn = new JButton("Sıfırla");

        Color blue = new Color(0, 120, 215);
        styleButton(applyBtn, blue);
        styleButton(resetBtn, blue);

        applyBtn.addActionListener(e -> applyFilters(null));
        resetBtn.addActionListener(e -> {
            usernameField.setText("");
            typeCombo.setSelectedIndex(0);
            startDateSpinner.setValue(new Date());
            endDateSpinner.setValue(new Date());
            applyFilters("date DESC");
        });

        p.add(applyBtn);
        p.add(resetBtn);

        return p;
    }

    private JScrollPane buildTable() {
        String[] columns = {"ID", "Kullanıcı Adı", "Tür", "Tarih", "Miktar", "Birim"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setBackground(new Color(0, 120, 215));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        return new JScrollPane(table);
    }

    private JPanel buildButtonsBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Color defaultBtnColor = new Color(0, 120, 215);

        JButton sortDateAscBtn  = new JButton("Tarih ↑");
        JButton sortDateDescBtn = new JButton("Tarih ↓");
        JButton sortTypeBtn     = new JButton("Tür Sırala");
        JButton summaryBtn      = new JButton("Özet (Adet/Toplam)");
        JButton typeBreakBtn    = new JButton("Tür Bazlı Özet");
        JButton exportBtn       = new JButton("CSV Dışa Aktar");
        JButton topDonorsBtn    = new JButton("En Çok Bağış Yapanlar");

        styleButton(sortDateAscBtn, defaultBtnColor);
        styleButton(sortDateDescBtn, defaultBtnColor);
        styleButton(sortTypeBtn, defaultBtnColor);
        styleButton(summaryBtn, defaultBtnColor);
        styleButton(typeBreakBtn, defaultBtnColor);
        styleButton(exportBtn, defaultBtnColor);
        styleButton(topDonorsBtn, defaultBtnColor);

        sortDateAscBtn.addActionListener(e -> applyFilters("d.date ASC, d.id ASC"));
        sortDateDescBtn.addActionListener(e -> applyFilters("d.date DESC, d.id DESC"));
        sortTypeBtn.addActionListener(e -> applyFilters("type ASC, date DESC"));

        summaryBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            if ("Hepsi".equalsIgnoreCase(type)) type = null;

            LocalDate start = convertDateToLocalDate((Date) startDateSpinner.getValue());
            LocalDate end = convertDateToLocalDate((Date) endDateSpinner.getValue());

            donationDAO dao = new donationDAO();
            Map<String, Object> sum = dao.getSummaryWithFilters(
                    username.isEmpty() ? null : username,
                    type,
                    start,
                    end
            );

            int count = (int) sum.getOrDefault("count", 0);
            double total = (double) sum.getOrDefault("total", 0.0);

            fillTableWithSummary(count, total);
        });

        typeBreakBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();

            LocalDate start = convertDateToLocalDate((Date) startDateSpinner.getValue());
            LocalDate end = convertDateToLocalDate((Date) endDateSpinner.getValue());

            donationDAO dao = new donationDAO();
            List<Map<String, Object>> rows = dao.getTypeBreakdown(
                    username.isEmpty() ? null : username, start, end
            );

            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veri bulunamadı.");
                return;
            }

            fillTableWithTypeSummary(rows);
        });

        exportBtn.addActionListener(e -> exportTableToCsv());

        topDonorsBtn.addActionListener(e -> {
            List<String> topDonors = donationDAO.getTopDonorsByScore();
            String message = String.join("\n", topDonors);
            JOptionPane.showMessageDialog(this, "En Çok Bağış Yapan 3 Kişi:\n" + message);
        });

        JButton[] buttons = {
            sortDateAscBtn,
            sortDateDescBtn,
            sortTypeBtn,
            summaryBtn,
            typeBreakBtn,
            exportBtn,
            topDonorsBtn
        };

        for (int i = 0; i < buttons.length; i++) {
            bar.add(buttons[i]);
            if (i < buttons.length - 1) {
                bar.add(Box.createRigidArea(new Dimension(32, 0)));
            }
        }

        return bar;
    }

    private void applyFilters(String orderByOrNull) {
        String username = usernameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        if ("Hepsi".equalsIgnoreCase(type)) type = null;

        LocalDate start = convertDateToLocalDate((Date) startDateSpinner.getValue());
        LocalDate end = convertDateToLocalDate((Date) endDateSpinner.getValue());

        donationDAO dao = new donationDAO();
        List<Donation> list = dao.getDonationsWithFilters(
                username.isEmpty() ? null : username,
                type,
                start,
                end,
                orderByOrNull
        );

        fillTable(list);

        if (orderByOrNull != null && table.getRowSorter() != null) {
            TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
            List<RowSorter.SortKey> keys = new ArrayList<>();
            if (orderByOrNull.toLowerCase().startsWith("date asc")) {
                keys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
            } else if (orderByOrNull.toLowerCase().startsWith("date desc")) {
                keys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
            } else if (orderByOrNull.toLowerCase().startsWith("type")) {
                keys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
            }
            sorter.setSortKeys(keys);
        }
    }

    private void fillTable(List<Donation> data) {
        String[] cols = {"ID", "Kullanıcı Adı", "Tür", "Tarih", "Miktar", "Birim"};
        model.setColumnIdentifiers(cols);
        model.setRowCount(0);
        for (Donation d : data) {
            model.addRow(new Object[]{
                    d.getId(),
                    d.getUsername(),
                    d.getTypeName().toUpperCase(),
                    d.getDate().toString(),
                    String.format("%.2f", d.getAmount()),
                    d.getUnit()
            });
        }
    }

    private LocalDate convertDateToLocalDate(Date date) {
        if (date == null) return null;
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void fillTableWithSummary(int count, double total) {
        String[] cols = {"Adet", "Toplam Miktar"};
        model.setColumnIdentifiers(cols);
        model.setRowCount(0);
        model.addRow(new Object[]{count, total});
    }

    private void exportTableToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("CSV olarak kaydet");

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try (PrintWriter pw = new PrintWriter(fileToSave, StandardCharsets.UTF_8)) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    pw.print(model.getColumnName(col));
                    if (col < model.getColumnCount() - 1) pw.print(",");
                }
                pw.println();

                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object cellValue = model.getValueAt(row, col);
                        String cellText = cellValue == null ? "" : cellValue.toString();
                        if (cellText.contains(",")) cellText = "\"" + cellText + "\"";
                        pw.print(cellText);
                        if (col < model.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }

                JOptionPane.showMessageDialog(this, "CSV başarıyla kaydedildi.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "CSV kaydedilirken hata oluştu: " + e.getMessage());
            }
        }
    }

    private void fillTableWithTypeSummary(List<Map<String, Object>> rows) {
        String[] cols = {"Tür", "Adet", "Toplam Miktar"};
        model.setColumnIdentifiers(cols);
        model.setRowCount(0);
        for (Map<String, Object> row : rows) {
            model.addRow(new Object[]{
                    ((String) row.get("type")).toUpperCase(),
                    row.get("count"),
                    String.format("%.2f", row.get("total"))
            });
        }
    }

    private void styleButton(JButton button, Color borderColor) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(borderColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent evt) {
                button.setBackground(borderColor);
                button.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(borderColor);
            }
        });
    }
}
