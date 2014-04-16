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

public class RoomDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTextField roomTextField;
    private JPanel buttonPane;
    private JTextField passwordTextField;
    private JPanel passwordPanel;
    private JLabel lblRoomPassword;

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
	setBounds(100, 100, 328, 222);
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
			setVisible(false);
		    }
		});
	    }
	}
	passwordPanel = new JPanel();
	passwordPanel.setEnabled(false);
	passwordPanel.setRequestFocusEnabled(false);

	lblRoomPassword = new JLabel("Room Password");

	passwordTextField = new JTextField();
	passwordTextField.setColumns(10);
	GroupLayout gl_passwordPanel = new GroupLayout(passwordPanel);
	gl_passwordPanel.setHorizontalGroup(
		gl_passwordPanel.createParallelGroup(Alignment.LEADING)
		.addGroup(gl_passwordPanel.createSequentialGroup()
			.addGroup(gl_passwordPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(passwordTextField, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
				.addComponent(lblRoomPassword))
				.addContainerGap(100, Short.MAX_VALUE))
		);
	gl_passwordPanel.setVerticalGroup(
		gl_passwordPanel.createParallelGroup(Alignment.LEADING)
		.addGroup(gl_passwordPanel.createSequentialGroup()
			.addComponent(lblRoomPassword)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(passwordTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addContainerGap(12, Short.MAX_VALUE))
		);
	passwordPanel.setLayout(gl_passwordPanel);
	GroupLayout groupLayout = new GroupLayout(getContentPane());
	groupLayout.setHorizontalGroup(
		groupLayout.createParallelGroup(Alignment.LEADING)
		.addGroup(groupLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(contentPanel, GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
				.addComponent(buttonPane, GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
				.addComponent(passwordPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap())
		);
	groupLayout.setVerticalGroup(
		groupLayout.createParallelGroup(Alignment.LEADING)
		.addGroup(groupLayout.createSequentialGroup()
			.addComponent(contentPanel, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
			.addGap(23)
			.addComponent(passwordPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(buttonPane, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
			.addContainerGap(84, Short.MAX_VALUE))
		);
	getContentPane().setLayout(groupLayout);
    }

    public String getRoom() {
	return roomTextField.getText();
    }

    public String getPassword() {
	return passwordTextField.getText();
    }
}
