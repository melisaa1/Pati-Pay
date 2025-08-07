
package patipayproject;



import javax.swing.*;
import javax.swing.table.*;
import javax.swing.SortOrder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.List;


public class AdminPanel extends JFrame {

    private JTextField usernameField;
    private JComboBox<String> typeCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    private JTable table;
    private DefaultTableModel model;

    public AdminPanel() {
        setTitle("Yönetici Paneli");
        setSize(1050, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        setLocationRelativeTo(null);

        buildFilterBar();
        buildTable();
        buildButtonsBar();

        applyFilters("date DESC");

        setVisible(true);
    }

    private void buildFilterBar() {
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

        add(p, BorderLayout.NORTH);
    }

    private void buildTable() {
        String[] columns = {"ID", "Kullanıcı Adı", "Tür", "Tarih", "Miktar", "Birim"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        JScrollPane pane = new JScrollPane(table);
        add(pane, BorderLayout.CENTER);
    }

    private void buildButtonsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton sortDateAscBtn  = new JButton("Tarih ↑");
        JButton sortDateDescBtn = new JButton("Tarih ↓");
        JButton sortTypeBtn     = new JButton("Tür Sırala");
        JButton summaryBtn      = new JButton("Özet (Adet/Toplam)");
        JButton typeBreakBtn    = new JButton("Tür Bazlı Özet");
        JButton exportBtn       = new JButton("CSV Dışa Aktar");
        JButton topDonorsBtn    = new JButton("En Çok Bağış Yapanlar");
        JButton exitBtn         = new JButton("Çıkış");

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

        topDonorsBtn.addActionListener(e -> {
            List<String> topDonors = donationDAO.getTopDonorsByScore();
            String message = String.join("\n", topDonors);
            JOptionPane.showMessageDialog(this, "En Çok Bağış Yapan 3 Kişi:\n" + message);
        });

        exitBtn.addActionListener(e -> {
            dispose();
            System.exit(0);
        });

        bar.add(sortDateAscBtn);
        bar.add(sortDateDescBtn);
        bar.add(sortTypeBtn);
        bar.add(summaryBtn);
        bar.add(typeBreakBtn);
        bar.add(exportBtn);
        bar.add(topDonorsBtn);
        bar.add(exitBtn);
        add(bar, BorderLayout.SOUTH);

        exportBtn.addActionListener(e -> exportTableToCsv());
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
                keys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));  // Tarih sütunu
            } else if (orderByOrNull.toLowerCase().startsWith("date desc")) {
                keys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
            } else if (orderByOrNull.toLowerCase().startsWith("type")) {
                keys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));  // Tür sütunu
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
            // Başlık satırı
            for (int col = 0; col < model.getColumnCount(); col++) {
                pw.print(model.getColumnName(col));
                if (col < model.getColumnCount() - 1) pw.print(",");
            }
            pw.println();

            // Satırlar
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object cellValue = model.getValueAt(row, col);
                    String cellText = cellValue == null ? "" : cellValue.toString();

                    // Eğer hücrede virgül varsa, tırnak içine al
                    if (cellText.contains(",")) {
                        cellText = "\"" + cellText + "\"";
                    }
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


}
