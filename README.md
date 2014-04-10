CytubeChat
==========

Simple chat client for Cytu.be using https://github.com/Gottox/socket.io-java-client for socket.io

Download
--------

[Here](https://www.dropbox.com/s/u4w9bg6xliik6gp/cytubechat.jar) is a .jar if you just want to run it. 


Features
--------

- Tab-completion of usernames
- Private Messesaging via `/pm username` (very simple in-line messaging)

Commands
--------

- `/black`, `/white`, `/grey` Change color of UI
- `/pm username` Starts a private message
- `/clearchat` Clears the chat buffer
- `/disconnect` Disconnects from the server
- `/reconnect` Disconnects and reconnects from the server (Useful for joining another room)
- `/sound` Mutes the boop sound on incoming messages (Still boops if message contains your name)
- `/login` Logs into a room
- `/chatbuffer` limit the number of message displayed to 100
