package org.aurinkopg.os;

public class ShellCommands {
    // Get the real name of the logged in user:
    // finger -l | head -n 1 | sed 's/^.*Name: //g' | tr -d '\n'

    // Get the username of the logged in user:
    // finger -l | head -n 1 | sed 's/^Login: //g' | sed 's/[[:space:]]*Name:.*$//g' | tr -d '\n'
}
