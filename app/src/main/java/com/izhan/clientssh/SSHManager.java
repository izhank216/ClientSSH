package com.izhan.clientssh;

import com.jcraft.jsch.*;
import com.google.common.base.Strings;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SSHManager {

    enum State { AWAIT_USERNAME, AWAIT_PASSWORD, CONNECTED }

    private Terminal terminal;
    private State state;
    private String username;
    private String password;
    private String host = "127.0.0.1";
    private int port = 22;
    private boolean allowSSH1 = false;

    private Session session;
    private ChannelShell channel;
    private OutputStream out;
    private InputStream in;

    public SSHManager(Terminal terminal) { this.terminal = terminal; }

    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void enableSSH1(boolean enable) { this.allowSSH1 = enable; }

    public void setState(State state) {
        this.state = state;
        if (state == State.AWAIT_PASSWORD) {
            terminal.setMaskInput(true);
            terminal.appendOutput("Password: ");
        }
    }

    public void handleInput(String input) {
        if (Strings.isNullOrEmpty(input)) return;
        switch(state) {
            case AWAIT_USERNAME:
                username = input;
                setState(State.AWAIT_PASSWORD);
                break;
            case AWAIT_PASSWORD:
                password = input;
                terminal.setMaskInput(false);
                terminal.appendOutput("\nConnecting...\n");
                connectSSH();
                break;
            case CONNECTED:
                sendCommand(input);
                break;
        }
    }

    private void connectSSH() {
        new Thread(() -> {
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(username, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                if (allowSSH1) session.setConfig("PreferredAuthentications","password,publickey,keyboard-interactive");
                session.connect(5000);

                channel = (ChannelShell) session.openChannel("shell");
                in = channel.getInputStream();
                out = channel.getOutputStream();
                channel.connect();
                state = State.CONNECTED;
                terminal.appendOutput("Connected to " + host + "\n");

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    String text = new String(buffer, 0, read, StandardCharsets.UTF_8);
                    terminal.appendOutput(text);
                }

            } catch (Exception e) {
                terminal.appendOutput("Error: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private void sendCommand(String command) {
        new Thread(() -> {
            try {
                String cmd = Strings.nullToEmpty(command).trim();
                if (!cmd.endsWith("\n")) cmd += "\n";
                IOUtils.write(cmd, out, "UTF-8");
                out.flush();
            } catch (Exception e) {
                terminal.appendOutput("Send command error: " + e.getMessage() + "\n");
            }
        }).start();
    }
}
