package za.co.wethinkcode.robots.client.gui;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;

import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Consumer;

public class ClientGui {

    private final JFrame frame = new JFrame("Robot World — Client");
    private final JTextArea log = new JTextArea();
    private final JTextField input = new JTextField();
    private final JButton sendBtn = new JButton("Send");
    private final JLabel status = new JLabel(" connecting… ");
    private final WorldPanel world = new WorldPanel();
    private Consumer<String> onSend = s -> {};

    public ClientGui(String host, int port) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // ---- Left: log + input ----
        JPanel left = new JPanel(new BorderLayout());
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setBackground(new Color(15, 15, 18));
        log.setForeground(new Color(220, 220, 220));
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createTitledBorder("Server log"));
        left.add(scroll, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        inputRow.add(new JLabel("command >"), BorderLayout.WEST);
        inputRow.add(input, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);
        left.add(inputRow, BorderLayout.SOUTH);

        // ---- Right: world ----
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("World"));
        right.add(world, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.4);
        frame.add(split, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        status.setText(" not connected ");
        frame.add(status, BorderLayout.SOUTH);

        // ---- Send wiring ----
        Runnable submit = () -> {
            String text = input.getText().trim();
            if (text.isEmpty()) return;
            onSend.accept(text);
            input.setText("");
        };
        sendBtn.addActionListener(e -> submit.run());
        input.addActionListener(e -> submit.run());

        frame.setPreferredSize(new Dimension(1200, 720));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        setStatus("connecting to " + host + ":" + port + " …");
    }

    public void setOnSend(Consumer<String> handler) {
        this.onSend = handler == null ? s -> {} : handler;
    }

    public void appendLog(String line) {
        SwingUtilities.invokeLater(() -> {
            log.append(line);
            if (!line.endsWith("\n")) log.append("\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    public void setStatus(String s) {
        SwingUtilities.invokeLater(() -> status.setText(" " + s + " "));
    }

    public void setSelfName(String name) {
        SwingUtilities.invokeLater(() -> {
            world.setSelfName(name);
            frame.setTitle(name != null ? "Robot World — " + name : "Robot World — Client");
        });
    }

    public void updateRobot(String name, Position pos, Directions dir) {
        SwingUtilities.invokeLater(() -> world.updateRobot(name, pos, dir));
    }

    public void removeRobot(String name) {
        SwingUtilities.invokeLater(() -> world.removeRobot(name));
    }

    public void flashBullet(String shooter, Directions dir, int distance, boolean hit) {
        SwingUtilities.invokeLater(() -> world.flashBullet(shooter, dir, distance, hit));
    }

    public void setPickups(List<Position> list) {
        SwingUtilities.invokeLater(() -> world.setPickups(list));
    }

    public void setHud(int lives, int shots, int magMax, int shield, int kills) {
        SwingUtilities.invokeLater(() -> world.setHud(lives, shots, magMax, shield, kills));
    }

    public void setAllRobots(List<za.co.wethinkcode.robots.models.ServerResponseRobot> snapshot) {
        SwingUtilities.invokeLater(() -> world.setAllRobots(snapshot));
    }
}
