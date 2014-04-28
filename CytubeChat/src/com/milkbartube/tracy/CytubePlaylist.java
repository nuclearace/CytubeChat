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

public class CytubePlaylist extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField newVideoTextField;
    private JScrollPane playlistScrollPane;
    private JTextPane playlistTextPane;
    private StyledDocument playlistStyledDocument;
    
    private LinkedList<CytubeVideo> playlist = new LinkedList<CytubeVideo>();

    
    public CytubePlaylist(LinkedList<CytubeVideo> playlist) {
	buildCytubePlaylist();
	this.setPlaylist(playlist);
    }

    /**
     * Create the frame.
     */
    public void buildCytubePlaylist() {
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	setBounds(100, 100, 450, 300);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);
	
	playlistScrollPane = new JScrollPane();
	
	JButton btnAddVideo = new JButton("Add Video");
	btnAddVideo.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    
		}
	});
	
	newVideoTextField = new JTextField();
	newVideoTextField.setColumns(10);
	GroupLayout gl_contentPane = new GroupLayout(contentPane);
	gl_contentPane.setHorizontalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
			.addComponent(playlistScrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
			.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
				.addContainerGap()
				.addComponent(newVideoTextField, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(btnAddVideo))
	);
	gl_contentPane.setVerticalGroup(
		gl_contentPane.createParallelGroup(Alignment.LEADING)
			.addGroup(gl_contentPane.createSequentialGroup()
				.addContainerGap()
				.addComponent(playlistScrollPane, GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
				.addGap(12)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
					.addComponent(newVideoTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(btnAddVideo)))
	);
	
	playlistTextPane = new JTextPane();
	playlistTextPane.setEditable(false);
	playlistScrollPane.setViewportView(playlistTextPane);
	setPlaylistStyledDocument(playlistTextPane.getStyledDocument());
	contentPane.setLayout(gl_contentPane);
    }
    
    protected void drawPlaylist() throws BadLocationException {
	playlistTextPane.setText("");
	for (int i = 0; i < playlist.size(); i++) {
	    getPlaylistStyledDocument().insertString(
		    getPlaylistStyledDocument().getLength(), playlist.get(i).getTitle() + "    "
		    + playlist.get(i).getDuration() + "\n", null);
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
}
