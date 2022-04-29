package helloWorld;

import peersim.edsim.*;

public class Message {

	//public final static int HELLOWORLD = 0;

	private int type;
	private String content;
	private HelloWorld node;

	Message(int type, String content, HelloWorld n) {
		this.type = type;
		this.content = content;
		this.node=n;
	}

	public String getContent() {
		return this.content;
	}

	public int getType() {
		return this.type;
	}

	public HelloWorld getNode() {
		return this.node;
	}

	public void setNode(HelloWorld node) {
		this.node = node;
	}

}