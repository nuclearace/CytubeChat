package com.milkbartube.tracy;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JPasswordField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTextField usernameTextField;
    private JPasswordField passwordField;

    public LoginDialog() {
	buildLoginDialog();
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	setLocationRelativeTo(null);
    }

    /**
     * Create the dialog.
     */
    public void buildLoginDialog() {
	setBounds(100, 100, 450, 208);
	getContentPane().setLayout(new BorderLayout());
	contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
	getContentPane().add(contentPanel, BorderLayout.CENTER);

	JLabel lblUsername = new JLabel("Username");

	usernameTextField = new JTextField();
	usernameTextField.setColumns(10);

	JLabel lblPassword = new JLabel("Password");

	passwordField = new JPasswordField();
	GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
	gl_contentPanel.setHorizontalGroup(
		gl_contentPanel.createParallelGroup(Alignment.LEADING)
		.addGroup(gl_contentPanel.createSequentialGroup()
			.addContainerGap()
			.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
				.addComponent(usernameTextField, GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
				.addComponent(lblUsername)
				.addComponent(lblPassword)
				.addComponent(passwordField))
				.addContainerGap(231, Short.MAX_VALUE))
		);
	gl_contentPanel.setVerticalGroup(
		gl_contentPanel.createParallelGroup(Alignment.LEADING)
		.addGroup(gl_contentPanel.createSequentialGroup()
			.addContainerGap()
			.addComponent(lblUsername)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(18)
			.addComponent(lblPassword)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addContainerGap(13, Short.MAX_VALUE))
		);
	contentPanel.setLayout(gl_contentPanel);
	{
	    JPanel buttonPane = new JPanel();
	    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    getContentPane().add(buttonPane, BorderLayout.SOUTH);
	    {
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			setVisible(false);
		    }
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	    }
	    {
		JButton guestLoginButton = new JButton("Guest Login");
		guestLoginButton.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
			setVisible(false);
		    }
		});
		guestLoginButton.setActionCommand("Cancel");
		buttonPane.add(guestLoginButton);
	    }
	}
    }

    public String getUsername() {
	return usernameTextField.getText();
    }

    public String getPassword() {
	char[] passwordCharArray = passwordField.getPassword();
	return new String(passwordCharArray);
    }
}
