package joshiminh.flappybird;

import joshiminh.flappybird.components.GameRenderer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

public class Launcher {

    // ─── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG      = new Color(0x1c1c2e);
    private static final Color SURFACE = new Color(0x2a2a45);
    private static final Color ACCENT  = new Color(0x4fc3f7);
    private static final Color FG      = new Color(0xeaeaea);
    private static final Color FG_DIM  = new Color(0x9090a0);
    private static final Font  LABEL_F = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  TITLE_F = new Font("Segoe UI", Font.BOLD, 22);

    // ─── Entry points ─────────────────────────────────────────────────────────

    public static void restartLauncher() {
        SwingUtilities.invokeLater(Launcher::showLauncher);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Launcher::showLauncher);
    }

    // ─── Main dialog ──────────────────────────────────────────────────────────

    private static void showLauncher() {
        Preferences prefs = Preferences.userNodeForPackage(Launcher.class);

        JTextField nameField  = styledTextField(prefs.get("LastPlayer", "Player"));
        JComboBox<String> themeBox = styledCombo(loadThemes(), "Original");
        JComboBox<String> diffBox  = styledCombo(
                new String[] { "Easy", "Normal", "Hard", "Impossible" }, "Normal");

        ImageIcon rawIcon = new ImageIcon("icon.png");
        ImageIcon icon    = GameRenderer.scaled(rawIcon, 50, 50);

        JDialog dialog = new JDialog((Frame) null, "Flappy Bird", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setIconImage(rawIcon.getImage());

        // ── Root panel ──
        JPanel root = new JPanel();
        root.setBackground(BG);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(28, 36, 24, 36));

        // Title
        JLabel title = new JLabel("Flappy Bird", icon, JLabel.CENTER);
        title.setFont(TITLE_F);
        title.setForeground(ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(22));

        // Form rows
        root.add(formRow("Player Name",    nameField));
        root.add(Box.createVerticalStrut(10));
        root.add(formRow("Theme",          themeBox));
        root.add(Box.createVerticalStrut(10));
        root.add(formRow("Difficulty",     diffBox));
        root.add(Box.createVerticalStrut(26));

        // Buttons
        final boolean[] play = { false };
        JButton playBtn = accentButton("PLAY");
        JButton exitBtn = mutedButton("EXIT");
        playBtn.addActionListener(e -> { play[0] = true; dialog.dispose(); });
        exitBtn.addActionListener(e -> { dialog.dispose(); System.exit(0); });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setBackground(BG);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRow.add(playBtn);
        btnRow.add(exitBtn);
        root.add(btnRow);

        // Footer
        root.add(Box.createVerticalStrut(18));
        JLabel footer = new JLabel("© JoshiMinh", SwingConstants.CENTER);
        footer.setFont(LABEL_F.deriveFont(11f));
        footer.setForeground(FG_DIM);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(footer);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);   // blocks (modal)

        if (play[0]) {
            String name  = nameField.getText().trim();
            String theme = (String) themeBox.getSelectedItem();
            int    diff  = diffBox.getSelectedIndex();
            prefs.put("LastPlayer", name);
            launchGame(name, theme, diff, rawIcon.getImage());
        }
    }

    private static void launchGame(String name, String theme, int diff, Image icon) {
        JFrame frame = new JFrame("Flappy Bird");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.add(new Game(theme, diff, name));
        frame.setIconImage(icon);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ─── Theme loader ─────────────────────────────────────────────────────────

    private static String[] loadThemes() {
        File dir = new File("resources/themes");
        String[] list = dir.isDirectory()
                ? dir.list((d, n) -> new File(d, n).isDirectory())
                : null;
        return list != null ? list : new String[0];
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────

    private static JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(BG);
        JLabel lbl = new JLabel(label);
        lbl.setFont(LABEL_F);
        lbl.setForeground(FG);
        lbl.setPreferredSize(new Dimension(110, 28));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private static JTextField styledTextField(String text) {
        JTextField f = new JTextField(text, 16);
        f.setBackground(SURFACE);
        f.setForeground(FG);
        f.setCaretColor(ACCENT);
        f.setFont(LABEL_F);
        f.setBorder(new CompoundBorder(
                new LineBorder(ACCENT, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private static <T> JComboBox<T> styledCombo(T[] items, String selected) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setSelectedItem(selected);
        box.setBackground(SURFACE);
        box.setForeground(FG);
        box.setFont(LABEL_F);
        box.setBorder(new LineBorder(ACCENT, 1, true));
        box.setRenderer((list, value, index, isSelected, focused) -> {
            JLabel lbl = new JLabel(value != null ? value.toString() : "");
            lbl.setBackground(isSelected ? ACCENT : SURFACE);
            lbl.setForeground(isSelected ? BG : FG);
            lbl.setOpaque(true);
            lbl.setFont(LABEL_F);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            return lbl;
        });
        return box;
    }

    private static JButton accentButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(BG);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton mutedButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SURFACE);
        btn.setForeground(FG_DIM);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}