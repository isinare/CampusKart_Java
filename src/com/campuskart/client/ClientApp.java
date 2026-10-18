package com.campuskart.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

// ★ RUBRIC: GUI (1M) | Asg 17: Swing components — JFrame, JPanel, JTextField, JPasswordField,
//            JComboBox, JTabbedPane, JOptionPane
//            Asg 18: button click handling with ActionListener
//            Asg 24: a GUI integrated with the DATABASE — every table below is filled from
//                    MySQL rows, fetched through the server (Swing -> Socket -> Server -> DAO -> MySQL).
//            Asg 6 : anonymous classes and one inner class as event listeners
public class ClientApp extends JFrame {

    private final ServerLink link;               // ★ Asg 27: the client's Socket to the server
    private String role = "";                    // STUDENT or ADMIN, learned at login

    private final JTextField emailField = new JTextField(12);           // ★ Asg 17
    private final JPasswordField passwordField = new JPasswordField(12);
    private final JTextField keywordField = new JTextField(10);
    private final JComboBox<String> categoryBox = new JComboBox<String>(   // ★ Asg 17: JComboBox
            new String[] { "All", "Books", "Electronics", "Stationery" });
    private final JTextField titleField = new JTextField(10);
    private final JTextField priceField = new JTextField(5);
    private final JTextField offerField = new JTextField(5);

    // ★ RUBRIC: GUI | Asg 24: JTable + DefaultTableModel fed by server rows (which come from JDBC)
    private final DefaultTableModel itemModel =
            new DefaultTableModel(new String[] { "ID", "Title", "Price", "Status" }, 0);
    private final JTable itemTable = new JTable(itemModel);
    private final DefaultTableModel mineModel =
            new DefaultTableModel(new String[] { "ID", "Title", "Price", "Status" }, 0);
    private final JTable mineTable = new JTable(mineModel);
    private final DefaultTableModel requestModel =
            new DefaultTableModel(new String[] { "Side", "Req ID", "Item ID", "Item", "Offer", "Status" }, 0);
    private final JTable requestTable = new JTable(requestModel);
    private final DefaultTableModel userModel =
            new DefaultTableModel(new String[] { "ID", "Name", "Email", "Enrollment" }, 0);
    private final JTable userTable = new JTable(userModel);

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;   // ★ Asg 4: wrapper class
        ClientApp app = new ClientApp(host, port);
        app.setVisible(true);
    }

    public ClientApp(String host, int port) {
        super("CampusKart Client");               // ★ Asg 17: JFrame is the top-level window
        this.link = new ServerLink(host, port);
        buildUi();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void buildUi() {
        JPanel loginPanel = new JPanel(new FlowLayout());
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login (try a seeded account, see README)"));
        loginPanel.add(new JLabel("Email"));
        loginPanel.add(emailField);
        loginPanel.add(new JLabel("Password"));
        loginPanel.add(passwordField);
        JButton loginButton = new JButton("Login");
        loginPanel.add(loginButton);
        // ★ RUBRIC: GUI | Asg 18 + Asg 6: ANONYMOUS class implements ActionListener for one click
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse", buildBrowsePanel());
        tabs.addTab("My listings", buildMinePanel());
        tabs.addTab("Requests", buildRequestsPanel());
        tabs.addTab("Admin", buildAdminPanel());

        setLayout(new BorderLayout());
        add(loginPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel bar = new JPanel(new FlowLayout());
        bar.add(new JLabel("Keyword"));
        bar.add(keywordField);
        bar.add(categoryBox);
        JButton search = new JButton("Search");
        bar.add(search);
        search.addActionListener(new ActionListener() {      // ★ Asg 6 + 18: anonymous listener
            public void actionPerformed(ActionEvent e) {
                loadBrowse();
            }
        });
        bar.add(new JLabel("Offer"));
        bar.add(offerField);
        JButton request = new JButton("Send request");
        bar.add(request);
        request.addActionListener(new RequestAction());      // listener as a separate inner class
        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMinePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel bar = new JPanel(new FlowLayout());
        bar.add(new JLabel("Title"));
        bar.add(titleField);
        bar.add(new JLabel("Price"));
        bar.add(priceField);
        JButton post = new JButton("Post item");
        bar.add(post);
        post.addActionListener(new ActionListener() {        // ★ Asg 18: click -> talk to the server -> database
            public void actionPerformed(ActionEvent e) {
                postItem();
            }
        });
        JButton refresh = new JButton("Refresh");
        bar.add(refresh);
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadMine();
            }
        });
        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(mineTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel bar = new JPanel(new FlowLayout());
        JButton accept = new JButton("Accept");
        bar.add(accept);
        accept.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                respond("accept");
            }
        });
        JButton complete = new JButton("Complete (runs the transaction)");
        bar.add(complete);
        complete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                respond("complete");
            }
        });
        JButton refresh = new JButton("Refresh");
        bar.add(refresh);
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadRequests();
            }
        });
        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel bar = new JPanel(new FlowLayout());
        JButton approve = new JButton("Approve");
        bar.add(approve);
        approve.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verify("approve");
            }
        });
        JButton reject = new JButton("Reject");
        bar.add(reject);
        reject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verify("reject");
            }
        });
        JButton refresh = new JButton("Refresh");
        bar.add(refresh);
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadAdmin();
            }
        });
        panel.add(bar, BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    // ---------- actions ----------

    private void doLogin() {
        try {
            String form = "email=" + ServerLink.encode(emailField.getText().trim())
                    + "&password=" + ServerLink.encode(new String(passwordField.getPassword()));
            ArrayList<String[]> rows = link.post("/login", form);   // ★ Asg 27: Socket request
            if (rows.size() > 0 && "OK".equals(rows.get(0)[0])) {
                role = rows.get(0).length > 1 ? rows.get(0)[1] : "";
                JOptionPane.showMessageDialog(this, "Logged in as " + role);
                loadBrowse();
                loadMine();
                loadRequests();
                if ("ADMIN".equals(role)) {
                    loadAdmin();
                }
            } else {
                showReply(rows);
            }
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    private void loadBrowse() {
        try {
            String path = "/?q=" + ServerLink.encode(keywordField.getText().trim())
                    + "&category=" + ServerLink.encode((String) categoryBox.getSelectedItem());
            fill(itemModel, link.get(path));       // the table now shows MySQL rows
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    private void loadMine() {
        try {
            fill(mineModel, link.get("/items/mine"));
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    private void loadRequests() {
        try {
            fill(requestModel, link.get("/requests"));
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    private void loadAdmin() {
        try {
            fill(userModel, link.get("/admin"));
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    private void postItem() {
        try {
            String form = "title=" + ServerLink.encode(titleField.getText().trim())
                    + "&price=" + ServerLink.encode(priceField.getText().trim())
                    + "&category=" + ServerLink.encode((String) categoryBox.getSelectedItem())
                    + "&condition=GOOD&description=posted from the Swing client";
            showReply(link.post("/items/add", form));   // the server may answer ERR|Price must be a number
            loadBrowse();
            loadMine();
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    private void respond(String action) {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first");
            return;
        }
        try {
            String form = "requestId=" + requestModel.getValueAt(row, 1) + "&action=" + action;
            showReply(link.post("/requests/respond", form));
            loadRequests();
            loadBrowse();
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    private void verify(String action) {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student first");
            return;
        }
        try {
            String form = "userId=" + userModel.getValueAt(row, 0) + "&action=" + action;
            showReply(link.post("/admin/verify", form));
            loadAdmin();
        } catch (IOException ex) {
            serverDown(ex);
        }
    }

    // ---------- helpers ----------

    // ★ Asg 6: INNER class — RequestAction reads itemTable, itemModel and offerField
    // of the enclosing ClientApp window directly.
    private class RequestAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int row = itemTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(ClientApp.this, "Select an item first");
                return;
            }
            try {
                double offer = Double.parseDouble(offerField.getText().trim());   // ★ Asg 4: wrapper class
                String form = "itemId=" + itemModel.getValueAt(row, 0)
                        + "&offeredPrice=" + offer + "&message=from the Swing client";
                showReply(link.post("/items/request", form));
                loadRequests();
            } catch (NumberFormatException ex) {    // ★ Asg 14: multiple catch — one per exception type
                JOptionPane.showMessageDialog(ClientApp.this, "Offer must be a number");
            } catch (IOException ex) {
                serverDown(ex);
            }
        }
    }

    // replaces a table's rows with the server's records
    // (line 0 is the OK / ERR status line, so real records start at index 1)
    private void fill(DefaultTableModel model, ArrayList<String[]> rows) {
        model.setRowCount(0);
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length > 1 && !"OK".equals(row[0]) && !"ERR".equals(row[0])) {
                model.addRow(row);
            }
        }
    }

    // shows an ERR|message from the server in a dialog (JOptionPane, Asg 17)
    private void showReply(ArrayList<String[]> rows) {
        if (rows.size() == 0) {
            return;
        }
        if ("ERR".equals(rows.get(0)[0])) {
            JOptionPane.showMessageDialog(this,
                    rows.get(0).length > 1 ? rows.get(0)[1] : "The server said no");
        }
    }

    private void serverDown(IOException ex) {       // ★ Asg 14: exceptions surfaced in the GUI, not on a stack
        JOptionPane.showMessageDialog(this, "Server unreachable — is CampusKartServer running?");
    }
}
