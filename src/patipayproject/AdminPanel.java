
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

        sortDateAscBtn.addActionListener(e -> applyFilters("d.date ASC, d.id ASC"));
        sortDateDescBtn.addActionListener(e -> applyFilters("d.date DESC, d.id DESC"));
        sortTypeBtn.addActionListener(e -> applyFilters("type ASC, date DESC"));

        summaryBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            if ("Hepsi".equalsIgnoreCase(type)) type = null;

            Date startUtil = (Date) startDateSpinner.getValue();
            Date endUtil = (Date) endDateSpinner.getValue();

            LocalDate start = startUtil == null ? null : Instant.ofEpochMilli(startUtil.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = endUtil == null ? null : Instant.ofEpochMilli(endUtil.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

            donationDAO dao = new donationDAO();
            Map<String, Object> sum = dao.getSummaryWithFilters(
                    username.isEmpty() ? null : username,
                    type,
                    start,
                    end
            );

            int count = (int) sum.get("count");
            double total = (double) sum.get("total");

            fillTableWithSummary(count, total);
        });

        typeBreakBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();

            Date startUtil = (Date) startDateSpinner.getValue();
            Date endUtil = (Date) endDateSpinner.getValue();

            LocalDate start = startUtil == null ? null : Instant.ofEpochMilli(startUtil.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = endUtil == null ? null : Instant.ofEpochMilli(endUtil.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

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
        
       JButton topDonorsBtn = new JButton("En Çok Bağış Yapanlar");
       topDonorsBtn.addActionListener(e -> {
       List<String> topDonors = donationDAO.getTopDonorsByScore();
       String message = String.join("\n", topDonors);
       JOptionPane.showMessageDialog(this, "En Çok Bağış Yapan 3 Kişi:\n" + message);
       });


       JButton exitBtn = new JButton("Exıt");
       exitBtn.addActionListener(e -> {
       dispose();
       System.exit(0); 
      });


        exportBtn.addActionListener(e -> exportTableToCsv());

        bar.add(sortDateAscBtn);
        bar.add(sortDateDescBtn);
        bar.add(sortTypeBtn);
        bar.add(summaryBtn);
        bar.add(typeBreakBtn);
        bar.add(exportBtn);
        bar.add(topDonorsBtn);
        bar.add(exitBtn);
        add(bar, BorderLayout.SOUTH);
    }

    private void applyFilters(String orderByOrNull) {
        String username = usernameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        if ("Hepsi".equalsIgnoreCase(type)) type = null;

        Date startUtil = (Date) startDateSpinner.getValue();
        Date endUtil = (Date) endDateSpinner.getValue();

        LocalDate start = startUtil == null ? null : Instant.ofEpochMilli(startUtil.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endUtil == null ? null : Instant.ofEpochMilli(endUtil.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

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

    private void fillTableWithSummary(int count, double total) {
        String[] cols = {"Toplam Bağış Adedi", "Toplam Miktar"};
        model.setColumnIdentifiers(cols);
        model.setRowCount(0);
        model.addRow(new Object[]{count, String.format("%.2f", total)});
    }

    private void fillTableWithTypeSummary(List<Map<String, Object>> summaryData) {
        String[] cols = {"Tür", "Adet", "Toplam Miktar"};
        model.setColumnIdentifiers(cols);
        model.setRowCount(0);

        for (Map<String, Object> row : summaryData) {
            String type = String.valueOf(row.get("type")).toUpperCase();
            int count = (int) row.get("count");
            double total = (double) row.get("total");
            model.addRow(new Object[]{type, count, String.format("%.2f", total)});
        }
    }

    private void exportTableToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("CSV olarak kaydet");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
              
                for (int c = 0; c < table.getColumnCount(); c++) {
                    pw.print(table.getColumnName(c));
                    if (c < table.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
              
                for (int r = 0; r < table.getRowCount(); r++) {
                    for (int c = 0; c < table.getColumnCount(); c++) {
                        Object val = table.getValueAt(r, c);
                        String cell = val == null ? "" : val.toString().replace("\"", "\"\"");
                        if (cell.contains(",") || cell.contains("\"")) {
                            pw.print("\"" + cell + "\"");
                        } else {
                            pw.print(cell);
                        }
                        if (c < table.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "CSV kaydedildi: " + file.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Dosya yazma hatası: " + ex.getMessage());
            }
        }
    }

}
