package com.milkbartube.tracy;

import java.awt.HeadlessException;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.text.BadLocationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public final class CytubeSocket {

    private Socket socket;
    private CytubeRoom room;

    public CytubeSocket(String server, CytubeRoom room) throws URISyntaxException {
        this.room = room;
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        this.socket = IO.socket(server, opts);

        addHandlers();
    }

    private void addHandlers() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Connected.");
                join(room.getRoom());
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                room.setUsername(null);
            }
        });

        socket.on("chatMsg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.chatMsg(obj);
                } catch (JSONException | BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("addUser", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    boolean afk = (boolean) obj.getJSONObject("meta").get("afk");
                    String username = obj.getString("name") ;
                    int rank = (int) obj.get("rank");
                    CytubeUser user = new CytubeUser(afk, username, rank, room, false);
                    room.addUser(user, true);
                    room.updateUserList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("userLeave", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.removeUser(obj.getString("name"));
                    room.updateUserList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("changeMedia", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.setCurrentMedia(obj.getString("title"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (room.getFrameParent().getTabbedPane().getSelectedComponent().equals(this))
                    room.getFrameParent().setTitle(room.getCurrentMedia());
            }
        });

        socket.on("pm", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.onPrivateMessage(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("login", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    if ((boolean) obj.get("success")) {
                        System.out.println("Logged in");
                        room.setUsername(obj.getString("name"));
                        room.getUser().setUsername(obj.getString("name"));
                    } else {
                        JOptionPane.showMessageDialog(null, obj.get("error"));
                        room.setUsername(null);
                    }
                } catch (HeadlessException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("setUserMeta", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.handleUserMeta(obj);
                    room.updateUserList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("queue", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.addVideo(obj, false, 0, null);
                } catch (JSONException | BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("delete", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.deleteVideo(obj.getInt("uid"), false);
                } catch (BadLocationException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("moveVideo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.moveVideo(obj);
                } catch (JSONException | BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("userlist", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray users = (JSONArray) args[0];
                room.getUserList().clear();
                try {
                    for (int i=0; i<users.length();i++) {
                        boolean afk = (boolean) users.getJSONObject(i).getJSONObject("meta")
                                .get("afk");
                        String username = (String) users.getJSONObject(i).get("name");
                        int rank = (int) users.getJSONObject(i).get("rank");
                        CytubeUser user = new CytubeUser(afk, username, rank, room, false);
                        try {
                            room.addUser(user, false);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                    room.updateUserList();
                } catch (BadLocationException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on("playlist", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray videoArray =  (JSONArray) args[0];

                if (videoArray.length() == 0 && room.getPlaylistFrame() != null) {
                    room.getPlaylistFrame().clearPlaylist();
                    room.getPlaylist().clear();
                }

                for (int i = 0; i < videoArray.length(); i++) {
                    try {
                        room.getPlaylist().add(new CytubeVideo(videoArray.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        socket.on("needPassword", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (!room.getRoomPassword().equals("")) {
                    sendRoomPassword(room.getRoomPassword());
                    room.setRoomPassword("");
                } else {
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel("Enter Room Password:");
                    JPasswordField pass = new JPasswordField(10);
                    panel.add(label);
                    panel.add(pass);
                    String[] options = new String[]{"OK", "Cancel"};
                    int option = JOptionPane.showOptionDialog(null, panel, "Room Password",
                            JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                            null, options, options[0]);
                    if (option == 0) {
                        char[] password = pass.getPassword();
                        sendRoomPassword(new String(password));
                    } else {
                        room.getFrameParent().getTabbedPane().remove(room);
                        socket.disconnect();
                        return;
                    }
                }
            }
        });

        socket.on("setAFK", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    room.setAfk(obj.getString("name"), (boolean) obj.get("afk"));
                    room.updateUserList();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void connect() {
        System.out.println("Connecting.");
        socket.connect();
    }

    public void disconnect() {
        socket.disconnect();
    }

    public void sendMessage(String message) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("msg", message);
            socket.emit("chatMsg", json);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public void login(String username, String password) {
        try {
            JSONObject json1 = new JSONObject();
            json1.putOpt("name", username);
            json1.putOpt("pw", password);
            socket.emit("login", json1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void join(String room) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt("name", room);
            socket.emit("initChannelCallbacks");
            socket.emit("joinChannel", json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void privateMessage(JSONObject json) {
        socket.emit("pm", json);
    }

    protected void sendRoomPassword(String password) {
        socket.emit("channelPassword", password);
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
