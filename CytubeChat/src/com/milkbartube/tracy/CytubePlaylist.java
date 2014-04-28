package com.milkbartube.tracy;

import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;

import org.json.JSONException;

public class CytubePlaylist extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField newVideoTextField;
    private JScrollPane playlistScrollPane;
    private JTextPane playlistTextPane;
    private StyledDocument playlistStyledDocument;
    private JCheckBox chckbxTemp;

    private CytubeRoom room;

    private LinkedList<CytubeVideo> playlist = new LinkedList<CytubeVideo>();


    public CytubePlaylist(LinkedList<CytubeVideo> playlist, CytubeRoom room) {
	setRoom(room);
	buildCytubePlaylist();
	setPlaylist(playlist);

    }

    /**
     * Create the frame.
     */
    public void buildCytubePlaylist() {
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	setBounds(100, 100, 507, 404);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);
	setTitle(room.getRoom() + " Playlist");

	playlistScrollPane = new JScrollPane();

	JButton btnAddVideoNext = new JButton("Add Next");
	btnAddVideoNext.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		parseAddVideo(newVideoTextField.getText(), "next");
		newVideoTextField.setText("");
	    }
	});

	newVideoTextField = new JTextField();
	newVideoTextField.setColumns(10);

	JButton btnAddEnd = new JButton("Add End");
	btnAddEnd.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		parseAddVideo(newVideoTextField.getText(), "end");
		newVideoTextField.setText("");
	    }
	});

	chckbxTemp = new JCheckBox("Temp");
	GroupLayout gl_contentPane = new GroupLayout(contentPane);
	gl_contentPane.setHorizontalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
		.addComponent(playlistScrollPane, GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
		.addGroup(gl_contentPane.createSequentialGroup()
			.addContainerGap()
			.addComponent(newVideoTextField, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(btnAddVideoNext)
			.addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(btnAddEnd)
			.addGap(9)
			.addComponent(chckbxTemp)
			.addContainerGap())
		);
	gl_contentPane.setVerticalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
		.addGroup(gl_contentPane.createSequentialGroup()
			.addContainerGap()
			.addComponent(playlistScrollPane, GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
			.addGap(12)
			.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
				.addComponent(newVideoTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(btnAddVideoNext)
				.addComponent(btnAddEnd)
				.addComponent(chckbxTemp)))
		);

	playlistTextPane = new JTextPane();
	playlistTextPane.setEditable(false);
	playlistScrollPane.setViewportView(playlistTextPane);
	setPlaylistStyledDocument(playlistTextPane.getStyledDocument());
	contentPane.setLayout(gl_contentPane);
    }

    protected void clearPlaylist() {
	playlistTextPane.setText("");
    }

    protected void drawPlaylist() throws BadLocationException {
	playlistTextPane.setText("");
	for (int i = 0; i < playlist.size(); i++) {
	    getPlaylistStyledDocument().insertString(
		    getPlaylistStyledDocument().getLength(), i + 1 + " " 
			    + playlist.get(i).getTitle() + "\n", null);
	}
    }

    protected void parseAddVideo(String url, String pos) {
	String[] parsedURL = CytubeUtils.parseVideoUrl(url);
	try {
	    if (parsedURL != null)
		getRoom().sendVideo(parsedURL[0], parsedURL[1], pos, chckbxTemp.isSelected());
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public LinkedList<CytubeVideo> getPlaylist() {
	return playlist;
    }

    public void setPlaylist(LinkedList<CytubeVideo> playlist) {
	this.playlist = playlist;
    }

    public StyledDocument getPlaylistStyledDocument() {
	return playlistStyledDocument;
    }

    public void setPlaylistStyledDocument(StyledDocument playlistStyledDocument) {
	this.playlistStyledDocument = playlistStyledDocument;
    }

    public CytubeRoom getRoom() {
	return room;
    }

    public void setRoom(CytubeRoom room) {
	this.room = room;
    }
}
