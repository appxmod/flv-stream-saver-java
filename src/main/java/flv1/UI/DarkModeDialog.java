package flv1.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static flv1.IO.VU.debug;

public class DarkModeDialog {
    public static void MsgBox(String text) {
        // Create the frame
        JFrame frame = new JFrame("Dark Mode Dialog Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(630, 370);

        // Create the dialog
        JDialog dialog = new JDialog(frame, "JavaMessage", true);
        // Set dark mode for the dialog
        dialog.getContentPane().setBackground(Color.BLACK);
        dialog.setSize(630, 370);
        dialog.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea(10, 20);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                debug(e.getKeyCode());
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W) {
                    debug("close");
                    dialog.dispose();
                    frame.dispose();
                }
            }
        });

        // Create a text area
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);

        textArea.setText(text);

        // Create a scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add the scroll pane to the dialog
        dialog.add(scrollPane, BorderLayout.CENTER);


        dialog.setLocationRelativeTo(null);
        // Show the dialog
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            debug(e);
        }
        dialog.setVisible(true);

    }
}