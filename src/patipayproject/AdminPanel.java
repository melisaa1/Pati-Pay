
package patipayproject;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class AdminPanel extends JFrame {

    private JTextField usernameField;
    private JComboBox<String> typeCombo;
    private JTextField startDateField;
    private JTextField endDateField;

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

        p.add(new JLabel("Başlangıç (yyyy-MM-dd):"));
        startDateField = new JTextField(10);
        p.add(startDateField);

        p.add(new JLabel("Bitiş (yyyy-MM-dd):"));
        endDateField = new JTextField(10);
        p.add(endDateField);

        JButton applyBtn = new JButton("Uygula");
        JButton resetBtn = new JButton("Sıfırla");

        applyBtn.addActionListener(e -> applyFilters(null));
        resetBtn.addActionListener(e -> {
            usernameField.setText("");
            typeCombo.setSelectedIndex(0);
            startDateField.setText("");
            endDateField.setText("");
            applyFilters("date DESC");
        });

        p.add(applyBtn);
        p.add(resetBtn);

        add(p, BorderLayout.NORTH);
    }

    /** Orta tablo */
    private void buildTable() {
        String[] columns={"ID", "Kullanıcı ID", "Bağış Türü", "Tarih", "Miktar", "Birim"};
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

        sortDateAscBtn.addActionListener(e -> applyFilters("date ASC, id ASC"));
        sortDateDescBtn.addActionListener(e -> applyFilters("date DESC, id DESC"));
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

        LocalDate start = parseDateOrNull(startDateField.getText().trim());
        LocalDate end   = parseDateOrNull(endDateField.getText().trim());

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
                keys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));  // 3 = Tarih
            } else if (orderByOrNull.toLowerCase().startsWith("date desc")) {
                keys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
            } else if (orderByOrNull.toLowerCase().startsWith("type")) {
                keys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));  // 2 = Tür
            }
            sorter.setSortKeys(keys);
        }
    }

    /** Tabloyu doldur */
    private void fillTable(List<Donation> data) {
        model.setRowCount(0);
        for (Donation d : data) {
            model.addRow(new Object[]{
                d.getId(),
                d.getUserId(),
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
        LocalDate start = parseDateOrNull(startDateField.getText().trim());
        LocalDate end   = parseDateOrNull(endDateField.getText().trim());

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
        LocalDate start = parseDateOrNull(startDateField.getText().trim());
        LocalDate end   = parseDateOrNull(endDateField.getText().trim());

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

    private LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Tarih formatı geçersiz. Örn: 2025-07-30");
            return null; 
        }
    }
}
