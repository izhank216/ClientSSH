package com.izhan.clientssh;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.common.base.Strings;

public class MainActivity extends AppCompatActivity {

    EditText hostInput;
    EditText portInput;
    Button connectButton;
    LinearLayout layout;
    Terminal terminal;
    SSHManager sshManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        hostInput = new EditText(this);
        hostInput.setHint("Host/IP");
        layout.addView(hostInput);

        portInput = new EditText(this);
        portInput.setHint("Port (default 22)");
        layout.addView(portInput);

        connectButton = new Button(this);
        connectButton.setText("Connect");
        layout.addView(connectButton);

        setContentView(layout);

        connectButton.setOnClickListener(v -> {

            String host = Strings.nullToEmpty(hostInput.getText().toString()).trim();
            String portText = Strings.nullToEmpty(portInput.getText().toString()).trim();
            int port = 22;
            try {
                if (!portText.isEmpty()) port = Integer.parseInt(portText);
            } catch (Exception ignored) { }

            terminal = new Terminal(this);
            sshManager = new SSHManager(terminal);
            sshManager.setHost(host);
            sshManager.setPort(port);
            sshManager.enableSSH1(true);

            setContentView(terminal.getLayout());
            terminal.setInputListener(input -> sshManager.handleInput(input));
            terminal.appendOutput("Connected to " + host + "\nUsername: ");
            sshManager.setState(SSHManager.State.AWAIT_USERNAME);

        });
    }
}
