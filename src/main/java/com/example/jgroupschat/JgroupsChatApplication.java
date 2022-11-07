package com.example.jgroupschat;

import org.jgroups.*;
import org.jgroups.util.Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
public class JgroupsChatApplication implements Receiver {

	JChannel channel;
	String user_name = System.getProperty("user.name","n/a");
	final List<String> state=new LinkedList<>();

	private void start() throws Exception {
		channel = new JChannel().setReceiver(this).connect("ChatCluster").getState(null, 1000);
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
		String line=msg.getSrc() + ": " + msg.getObject();
		System.out.println(line);
		synchronized(state) {
			state.add(line);
		}
	}

	public void getState(OutputStream output) throws Exception {
		synchronized(state) {
			Util.objectToStream(state, new DataOutputStream(output));
		}
	}

	public void setState(InputStream input) throws Exception {
		List<String> list = Util.objectFromStream(new DataInputStream(input));
		synchronized(state) {
			state.clear();
			state.addAll(list);
		}
		System.out.println("received state (" + list.size() + " messages in chat history):");
		list.forEach(System.out::println);
	}
}
