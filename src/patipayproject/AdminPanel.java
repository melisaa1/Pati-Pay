
package patipayproject;

import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.util.List;


public class AdminPanel extends JFrame{
    
      public AdminPanel() {
    setTitle("Yönetici Paneli");
    setSize(500, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    JLabel label = new JLabel("Yönetici Girişi");
    add(label, BorderLayout.NORTH);

    donationDAO dao = new donationDAO();
    List<Donation> allDonations = dao.getAllDonations();

    JTextArea area = new JTextArea();
    for (Donation d : allDonations) {
        area.append(d.toString() + "\n");
    }

    add(new JScrollPane(area), BorderLayout.CENTER);
    setVisible(true);
}
    
}
