package com.example.jgroupschat;

import org.jgroups.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@SpringBootApplication
public class JgroupsChatApplication implements Receiver {

	JChannel channel;
	String user_name = System.getProperty("user.name","n/a");

	private void start() throws Exception {
		channel = new JChannel().setReceiver(this).connect("ChatCluster");
		eventLoop();
		channel.close();
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(JgroupsChatApplication.class, args);
		new JgroupsChatApplication().start();
	}

	private void eventLoop() {
		BufferedReader in= new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			try {
				System.out.print("> ");
				System.out.flush();
				String line= in.readLine().toLowerCase();
				if(line.startsWith("quit") || line.startsWith("exit"))
					break;
				line="[" + user_name + "] " + line;
				Message msg = new ObjectMessage(null, line);
				channel.send(msg);
			}
			catch(Exception e) {
			}
		}
	}

	public void viewAccepted (View new_view) {
		System.out.println("** view: " + new_view);
	}

	public void receive(Message msg) {
		System.out.println(msg.getSrc() + ": " + msg.getObject());
	}
}
