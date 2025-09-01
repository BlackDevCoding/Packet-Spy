package dev.blackdev.ui;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Vector;
import java.util.regex.Pattern;
public class PacketSpyWindow {
    private static PacketSpyWindow INSTANCE;
    private final JFrame frame;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField filterField;
    private final JLabel countLabel;
    private final int MAX_ROWS = 10000;
    private static final boolean HEADLESS = GraphicsEnvironment.isHeadless();
    private PacketSpyWindow() {
        if (HEADLESS) {
            frame = null;
            model = new DefaultTableModel(new Object[]{}, 0);
            table = new JTable(model);
            filterField = null;
            countLabel = null;
            return;
        }
        UIManager.put("Panel.background", new Color(20,20,23));
        UIManager.put("Table.background", new Color(27,29,33));
        UIManager.put("Table.foreground", new Color(238,238,238));
        UIManager.put("Table.gridColor", new Color(42,45,52));
        UIManager.put("Table.selectionBackground", new Color(51,102,153));
        UIManager.put("Table.selectionForeground", Color.white);
        UIManager.put("ScrollPane.background", new Color(20,20,23));
        UIManager.put("TextField.background", new Color(34,36,41));
        UIManager.put("TextField.foreground", new Color(238,238,238));
        UIManager.put("Button.background", new Color(35,38,43));
        UIManager.put("Button.foreground", new Color(238,238,238));
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        frame = new JFrame("PacketSpy");
        frame.setSize(980, 640);
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        model = new DefaultTableModel(new Object[]{"Time", "Direction", "Packet Class"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(model);
        table.setRowHeight(22);
        table.setFont(mono);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(650);
        JTableHeader header = table.getTableHeader();
        header.setForeground(new Color(240,240,240));
        header.setBackground(new Color(32,34,39));
        header.setFont(mono.deriveFont(Font.BOLD, 12f));
        DefaultTableCellRenderer stripe = new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                Component comp = super.getTableCellRendererComponent(t,v,s,f,r,c);
                comp.setFont(mono);
                Color bg = (r % 2 == 0) ? new Color(27,29,33) : new Color(24,26,30);
                if (s) bg = UIManager.getColor("Table.selectionBackground");
                comp.setBackground(bg);
                return comp;
            }
        };
        for (int i=0;i<table.getColumnCount();i++) table.getColumnModel().getColumn(i).setCellRenderer(stripe);
        JScrollPane sp = new JScrollPane(table);
        JPanel top = new JPanel(new BorderLayout(8,0));
        top.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel title = new JLabel("PacketSpy");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(240,240,240));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        left.setOpaque(false);
        left.add(title);
        filterField = new JTextField(28);
        JButton clear = new JButton("Clear");
        JButton alwaysOnTop = new JButton("Pin");
        countLabel = new JLabel("0 shown");
        countLabel.setForeground(new Color(204,204,204));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setOpaque(false);
        right.add(countLabel);
        right.add(alwaysOnTop);
        right.add(clear);
        top.add(left, BorderLayout.WEST);
        top.add(filterField, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(top, BorderLayout.NORTH);
        frame.getContentPane().add(sp, BorderLayout.CENTER);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        filterField.addActionListener(e -> {
            String q = filterField.getText().trim();
            if (q.isEmpty()) sorter.setRowFilter(null);
            else sorter.setRowFilter(RowFilter.regexFilter("(?i)"+Pattern.quote(q), 2));
            updateCount();
        });
        clear.addActionListener(e -> { model.setRowCount(0); updateCount(); });
        alwaysOnTop.addActionListener(e -> frame.setAlwaysOnTop(!frame.isAlwaysOnTop()));
        updateCount();
    }
    public static PacketSpyWindow getInstance() {
        if (INSTANCE == null) INSTANCE = new PacketSpyWindow();
        return INSTANCE;
    }
    public void showWindow() {
        if (HEADLESS || frame == null) return;
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
    public void append(String timeIso, String direction, String klass) {
        if (HEADLESS || model.getColumnCount() == 0) return;
        Vector<String> row = new Vector<>();
        row.add(timeIso);
        row.add(direction);
        row.add(klass);
        model.addRow(row);
        if (model.getRowCount() > MAX_ROWS) {
            int toRemove = model.getRowCount() - MAX_ROWS;
            for (int i = 0; i < toRemove; i++) model.removeRow(0);
        }
        int last = model.getRowCount() - 1;
        if (last >= 0) table.scrollRectToVisible(table.getCellRect(last, 0, true));
        updateCount();
    }
    private void updateCount() {
        if (HEADLESS || countLabel == null) return;
        int visible = table.getRowCount();
        countLabel.setText(visible + " shown");
    }
}
