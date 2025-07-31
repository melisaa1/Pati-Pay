
package patipayproject;



import javax.swing.*;
import javax.swing.table.*;
import javax.swing.RowSorter.SortKey;
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
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        setLocationRelativeTo(null);

        buildFilterBar();
        buildTable();
        buildButtonsBar();

        // İlk açılışta tüm veriler
        applyFilters("date DESC");

        setVisible(true);
    }

    /** Üst filtre barı */
    private void buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

        p.add(new JLabel("Kullanıcı adı:"));
        usernameField = new JTextField(12);
        p.add(usernameField);

        p.add(new JLabel("Tür:"));
        typeCombo = new JComboBox<>(new String[]{"Hepsi", "Mama", "Su", "Para"});
        p.add(typeCombo);

        // Spinner modeli (tarih tipi)
        SpinnerDateModel startModel = new SpinnerDateModel();
        SpinnerDateModel endModel = new SpinnerDateModel();

        // Spinner bileşenleri
        startDateSpinner = new JSpinner(startModel);
        endDateSpinner = new JSpinner(endModel);

        // Görünüm için tarih formatı ayarla
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
            // Tarih spinnerlarını bugünün tarihi yap (veya istersen null yap)
            startDateSpinner.setValue(new Date());
            endDateSpinner.setValue(new Date());
            applyFilters("date DESC");
        });

        p.add(applyBtn);
        p.add(resetBtn);

        add(p, BorderLayout.NORTH);
    }

    /** Orta tablo */
    private void buildTable() {
        String[] columns = {"ID", "Kullanıcı Adı", "Tür", "Tarih", "Miktar", "Birim"};
        model = new DefaultTableModel(columns,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        JScrollPane pane = new JScrollPane(table);
        add(pane, BorderLayout.CENTER);
    }

    /** Alt buton çubuğu */
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

        summaryBtn.addActionListener(e -> showSummaryDialog());
        typeBreakBtn.addActionListener(e -> showTypeBreakdownDialog());
        exportBtn.addActionListener(e -> exportTableToCsv());

        bar.add(sortDateAscBtn);
        bar.add(sortDateDescBtn);
        bar.add(sortTypeBtn);
        bar.add(summaryBtn);
        bar.add(typeBreakBtn);
        bar.add(exportBtn);

        add(bar, BorderLayout.SOUTH);
    }

    /** Filtreleri uygula ve tabloyu doldur */
    private void applyFilters(String orderByOrNull) {
        String username = usernameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        if ("Hepsi".equalsIgnoreCase(type)) type = null;

        // JSpinner'dan tarihleri al ve LocalDate'e çevir
        Date startUtil = (Date) startDateSpinner.getValue();
        Date endUtil = (Date) endDateSpinner.getValue();

        LocalDate start = null;
        LocalDate end = null;

        if (startUtil != null) {
            start = Instant.ofEpochMilli(startUtil.getTime())
                           .atZone(ZoneId.systemDefault())
                           .toLocalDate();
        }
        if (endUtil != null) {
            end = Instant.ofEpochMilli(endUtil.getTime())
                         .atZone(ZoneId.systemDefault())
                         .toLocalDate();
        }

        donationDAO dao = new donationDAO();
        List<Donation> list = dao.getDonationsWithFilters(
                username.isEmpty() ? null : username,
                type,
                start,
                end,
                orderByOrNull
        );

        fillTable(list);

        // JTable'ı programatik sırala (kullanıcı isterse başlığa tıklayarak da değiştirir)
        if (orderByOrNull != null && table.getRowSorter() != null) {
            TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
            List<RowSorter.SortKey> keys = new ArrayList<>();
            if (orderByOrNull.toLowerCase().startsWith("date asc")) {
                keys.add(new SortKey(3, SortOrder.ASCENDING));  // 3 = Tarih sütunu
            } else if (orderByOrNull.toLowerCase().startsWith("date desc")) {
                keys.add(new SortKey(3, SortOrder.DESCENDING));
            } else if (orderByOrNull.toLowerCase().startsWith("type")) {
                keys.add(new SortKey(2, SortOrder.ASCENDING));  // 2 = Tür sütunu
            }
            sorter.setSortKeys(keys);
        }
    }

    /** Tabloyu doldur */
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

    /** Özet (adet & toplam tutar) */
    private void showSummaryDialog() {
        String username = usernameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        if ("Hepsi".equalsIgnoreCase(type)) type = null;

        Date startUtil = (Date) startDateSpinner.getValue();
        Date endUtil = (Date) endDateSpinner.getValue();

        LocalDate start = null;
        LocalDate end = null;

        if (startUtil != null) {
            start = Instant.ofEpochMilli(startUtil.getTime())
                           .atZone(ZoneId.systemDefault())
                           .toLocalDate();
        }
        if (endUtil != null) {
            end = Instant.ofEpochMilli(endUtil.getTime())
                         .atZone(ZoneId.systemDefault())
                         .toLocalDate();
        }

        donationDAO dao = new donationDAO();
        Map<String, Object> sum = dao.getSummaryWithFilters(
                username.isEmpty() ? null : username, type, start, end
        );

        int count = (int) sum.get("count");
        double total = (double) sum.get("total");

        JOptionPane.showMessageDialog(this,
            String.format("Filtrelere göre Özet:\n- Toplam Bağış Adedi: %d\n- Toplam Miktar: %.2f (unit türüne göre değişir)",
                count, total),
            "Özet", JOptionPane.INFORMATION_MESSAGE
        );
    }

    /** Tür bazlı özet (hangi türden kaç adet, toplam miktar) */
    private void showTypeBreakdownDialog() {
        String username = usernameField.getText().trim();

        Date startUtil = (Date) startDateSpinner.getValue();
        Date endUtil = (Date) endDateSpinner.getValue();

        LocalDate start = null;
        LocalDate end = null;

        if (startUtil != null) {
            start = Instant.ofEpochMilli(startUtil.getTime())
                           .atZone(ZoneId.systemDefault())
                           .toLocalDate();
        }
        if (endUtil != null) {
            end = Instant.ofEpochMilli(endUtil.getTime())
                         .atZone(ZoneId.systemDefault())
                         .toLocalDate();
        }

        donationDAO dao = new donationDAO();
        List<Map<String, Object>> rows = dao.getTypeBreakdown(
                username.isEmpty() ? null : username, start, end
        );

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veri bulunamadı.");
            return;
        }

        StringBuilder sb = new StringBuilder("Tür Bazlı Özet:\n");
        for (var row : rows) {
            String t = String.valueOf(row.get("type")).toUpperCase();
            int cnt = (int) row.get("count");
            double total = (double) row.get("total");
            sb.append(String.format("- %s: %d adet, toplam %.2f\n", t, cnt, total));
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    /** CSV dışa aktarım (tablodaki mevcut görünümü yazar) */
    private void exportTableToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("CSV olarak kaydet");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                // başlık
                for (int c = 0; c < table.getColumnCount(); c++) {
                    pw.print(table.getColumnName(c));
                    if (c < table.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
                // satırlar
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
