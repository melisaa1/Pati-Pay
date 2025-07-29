
package patipayproject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminPanel extends JFrame{
    
    public AdminPanel() {
    setTitle("Yönetici Paneli");
    setSize(500, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10,10));

    JLabel label = new JLabel("Yönetici Girişi",SwingConstants.CENTER);
    add(label, BorderLayout.NORTH);

    donationDAO dao = new donationDAO();
    List<Donation> allDonations = dao.getAllDonations();

    String[] columns={"ID", "Kullanıcı ID", "Bağış Türü", "Tarih"};
    
    DefaultTableModel model=new DefaultTableModel(columns,0);
    
     for (Donation d : allDonations) {
            Object[] row = {
                d.getId(),
                d.getUserId(),
                d.getTypeName().toUpperCase(),
                d.getDate().toString()
            };
            model.addRow(row);
        }
     
     JTable table=new JTable(model);

     JScrollPane pane=new JScrollPane(table);  
     
     add(pane, BorderLayout.CENTER);
     
    

     
     setVisible(true);
     
     JButton winnerButton = new JButton("Winner");

     
     JPanel buttonPanel = new JPanel();
     buttonPanel.add(winnerButton);
     add(buttonPanel, BorderLayout.SOUTH);
     
     winnerButton.addActionListener(e -> {
     Optional<Map.Entry<Integer, Integer>> winnerOpt = dao.getTopDonorOfMonth();
     if (winnerOpt.isPresent()) {
        int userId = winnerOpt.get().getKey();
        int totalDonations = winnerOpt.get().getValue();
        String username = UserService.getUsernameById(userId);

        JOptionPane.showMessageDialog(this,
            "Ayın en çok bağış yapanı:\n" + username +
            "\nToplam bağış sayısı: " + totalDonations);
    } else {
        JOptionPane.showMessageDialog(this, "Bu ay bağış yapan kullanıcı yok.");
    }
});

}
    
}
