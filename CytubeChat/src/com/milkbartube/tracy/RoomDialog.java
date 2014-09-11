package com.milkbartube.tracy;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPasswordField;

public class RoomDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTextField roomTextField;
    private JPanel buttonPane;
    private JPanel passwordPanel;
    private JLabel lblRoomPassword;
    private JPasswordField passwordField;
    private String room;
    private String password;
    private String server;
    private JTextField serverTextField;

    /**
     * Launch the application.
     */
    public RoomDialog(){
        buildRoomDialog();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Create the dialog.
     */
    public void buildRoomDialog() {
        setBounds(100, 100, 328, 276);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel roomLabel = new JLabel("Enter Room");
        roomLabel.setBounds(0, 6, 95, 28);
        contentPanel.setLayout(null);
        roomTextField = new JTextField();
        roomTextField.setBounds(0, 34, 210, 28);
        roomTextField.setColumns(10);
        contentPanel.add(roomTextField);
        contentPanel.add(roomLabel);
        {
            buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setRoom(roomTextField.getText());

                        char[] passwordCharArray = passwordField.getPassword();
                        setPassword(new String(passwordCharArray));
                        setServer(serverTextField.getText());
                        setVisible(false);
                    }
                });
                okButton.setRequestFocusEnabled(false);
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setRoom(null);
                        setVisible(false);
                    }
                });
            }
        }
        passwordPanel = new JPanel();
        passwordPanel.setEnabled(false);
        passwordPanel.setRequestFocusEnabled(false);

        lblRoomPassword = new JLabel("Room Password");

        passwordField = new JPasswordField();
        GroupLayout gl_passwordPanel = new GroupLayout(passwordPanel);
        gl_passwordPanel.setHorizontalGroup(
                gl_passwordPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_passwordPanel.createSequentialGroup()
                        .addGroup(gl_passwordPanel.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblRoomPassword)
                                .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(91, Short.MAX_VALUE))
                );
        gl_passwordPanel.setVerticalGroup(
                gl_passwordPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_passwordPanel.createSequentialGroup()
                        .addComponent(lblRoomPassword)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(12, Short.MAX_VALUE))
                );
        passwordPanel.setLayout(gl_passwordPanel);

        JLabel lblServer = new JLabel("Server");

        serverTextField = new JTextField();
        serverTextField.setText("cytu.be");
        serverTextField.setColumns(10);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                                .addComponent(buttonPane, GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                                .addComponent(passwordPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblServer)
                                .addComponent(serverTextField, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(contentPanel, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(passwordPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblServer)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(serverTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                        .addComponent(buttonPane, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                );
        getContentPane().setLayout(groupLayout);
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
