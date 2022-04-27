package helloWorld;

import peersim.edsim.*;
import peersim.core.*;
import peersim.config.*;

public class HelloWorld implements EDProtocol {

	//identifiant de la couche transport
	private int transportPid;

	//objet couche transport
	private HWTransport transport;

	//identifiant de la couche courante (la couche applicative)
	private int mypid;

	//le numero de noeud
	private int nodeId;
	
	//l'id du noeud distribue aleatoirement entre 0 et 10000
	private int uid;
	
	//dit si le noeud fait partie de l'anneau
	private boolean state;
	
	//permet de savoir si le noeud est deja en train d'effectuer une action
	private boolean action=false;
	
	//definition des voisins droit et gauche du noeud 
	private Node previous;
	private Node next;
	
	//prefixe de la couche (nom de la variable de protocole du fichier de config)
	private String prefix;
	

	public HelloWorld(String prefix) {
		this.prefix = prefix;
		//initialisation des identifiants a partir du fichier de configuration
		this.transportPid = Configuration.getPid(prefix + ".transport");
		this.mypid = Configuration.getPid(prefix + ".myself");
		this.transport = null;
		this.previous=this.next=null;
	}

	//methode appelee lorsqu'un message est recu par le protocole HelloWorld du noeud
	public void processEvent( Node node, int pid, Object event ) {
		this.receive((Message)event);
	}

	//methode necessaire pour la creation du reseau (qui se fait par clonage d'un prototype)
	public Object clone() {

		HelloWorld dolly = new HelloWorld(this.prefix);

		return dolly;
	}

	//liaison entre un objet de la couche applicative et un 
	//objet de la couche transport situes sur le meme noeud
	public void setTransportLayer(int nodeId) {
		this.nodeId = nodeId;
		this.transport = (HWTransport) Network.get(this.nodeId).getProtocol(this.transportPid);
	}

	//envoi d'un message (l'envoi se fait via la couche transport)
	public void send(Message msg, Node dest) {
		this.transport.send(getMyNode(), dest, msg, this.mypid);
	}

	//affichage a la reception 
	private void receive(Message msg) {
		switch (msg.getType()) {
		//message "normal" fait le tour des noeuds
		case 0:
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
			
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			System.out.println(this +", uid "+this.getUid()+" : Received " + msg.getContent() +" from "+ msg.getNode().getUid());
			
			if (this.getMyNode().getID()!= 0) {
				System.out.println(this + ": send "+ msg.getContent()+ " to "+this.getNextNode().getProtocol(this.mypid));
				msg.setNode(this);
				this.send(msg, this.getNextNode());		
			}
			
			this.setAction(false);
			break;
			
		//mesage join, le noeud demande a s'inserer au bon endroit
		case 1:
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
		
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			System.out.println(this +", uid "+this.getUid()+" : Received " + msg.getContent() + " from "+ msg.getNode().getUid());
			
			//cas ou il n'y a qu'un seul noeud dans l'anneau
			if (this.nodeId==this.getNextNode().getID()) {
				
				//defintion des nouveaux voisins
				msg.getNode().setPrevNode(this.getMyNode());
				msg.getNode().setNextNode(this.getMyNode());
				System.out.println("node"+msg.getNode().getMyNode().getID()+", uid: " +msg.getNode().getUid()+" prev node: "+msg.getNode().getPrevNode().getID()+" next node: "+msg.getNode().getNextNode().getID());
				this.setNextNode(msg.getNode().getMyNode());
				this.setPrevNode(msg.getNode().getMyNode());
				System.out.println(this+", uid: " +this.getUid()+" prev node: "+this.getPrevNode().getID()+" next node: "+this.getNextNode().getID());
				System.out.println("Node " + msg.getNode().getNodeId()+ ", uid "+ msg.getNode().getUid()+ " bien ins�r� entre " + this.uid + " et "+this.uid);
			}
			
			//cas generique (+de 1 noeud)
			else if (msg.getNode().getUid()>this.uid) {
				HelloWorld next=(HelloWorld)this.getNextNode().getProtocol(this.mypid);
			
				if (msg.getNode().getUid()<next.getUid() || this.getUid()>next.getUid()) {
				
					//on defini les voisins du noeud en insertion
					msg.getNode().setPrevNode(this.getMyNode());
					msg.getNode().setNextNode(this.getNextNode());
					msg.getNode().setState(true);
					System.out.println("node"+msg.getNode().getMyNode().getID()+", uid: " +msg.getNode().getUid()+" prev node: "+msg.getNode().getPrevNode().getID()+" next node: "+msg.getNode().getNextNode().getID());
					
					//on redefini les voisins des deux noeuds implique dans l'insertion du nouveau noeud
					next.setPrevNode(msg.getNode().getMyNode());
					System.out.println("node" +next.getMyNode().getID()+", uid: " +next.getUid()+" prev node: "+next.getNextNode().getID());
					this.setNextNode(msg.getNode().getMyNode());
					System.out.println(this+", uid: " +this.getUid()+" next node: "+this.getNextNode().getID());
					//System.out.println(this +", uid "+this.getUid()+" updated, new next node: "+this.getNextNode().getID());
					System.out.println("Node " + msg.getNode().getNodeId()+ ", uid "+ msg.getNode().getUid()+ " bien ins�r� entre " + this.uid + " et "+next.getUid());
				}
			
				else {
					this.send(msg, this.getNextNode());
				}
			}
			
			else if (msg.getNode().getUid()<this.uid) {
				HelloWorld prev=(HelloWorld)this.getPrevNode().getProtocol(this.mypid);
				
				if (msg.getNode().getUid()>prev.getUid() || this.getUid()<prev.getUid()) {
					
					//on defini les voisins du noeud en insertion
					msg.getNode().setPrevNode(this.getPrevNode());
					msg.getNode().setNextNode(this.getMyNode());
					msg.getNode().setState(true);
					System.out.println("node"+msg.getNode().getMyNode().getID()+", uid: " +msg.getNode().getUid()+" prev node: "+msg.getNode().getPrevNode().getID()+" next node: "+msg.getNode().getNextNode().getID());
					
					//on redefini les voisins des deux noeuds implique dans l'insertion du nouveau noeud
					prev.setNextNode(msg.getNode().getMyNode());
					System.out.println("node"+prev.getMyNode().getID()+", uid: " +prev.getUid()+" next node: "+prev.getNextNode().getID());
					this.setPrevNode(msg.getNode().getMyNode());
					System.out.println(this+", uid: " +this.getUid()+" prev node: "+this.getPrevNode().getID());
					//System.out.println(this +", uid "+this.getUid()+" updated, new prev node: "+this.getPrevNode().getID());
					System.out.println("Node " + msg.getNode().getNodeId()+ ", uid " +msg.getNode().getUid()+ " bien ins�r� entre " + prev.getUid() + " et "+this.uid);
				}
				
				else {
					this.send(msg, this.getPrevNode());
				}
			}
			
			this.setAction(false);
			break;
			
		//message leave, le noeud veut quitter l'anneau (recu par voisin next du noeud sortant)
		case 2: 
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
			
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			System.out.println("Node" + msg.getNode().getNodeId() +", uid "+msg.getNode().getUid()+" want to leave");
			Node prevNode=msg.getNode().getPrevNode();
			HelloWorld prev=(HelloWorld)prevNode.getProtocol(this.mypid);
			
			//on defini les voisins du noeud sortant a null
			msg.getNode().setPrevNode(null);
			msg.getNode().setNextNode(null);
			
			//mise a jour des voisins lies au noeud sortant
			prev.setNextNode(this.getMyNode());
			this.setPrevNode(prevNode);
			
			//System.out.println(this +", uid "+this.getUid()+" updated, new prev node: "+this.getPrevNode().getID());
			System.out.println("Node uid "+ msg.getNode().getUid()+ " has left ");
			this.setAction(false);
			break;
			
		}
	}
	

	//retourne le noeud courant
	public Node getMyNode() {
		return Network.get(this.nodeId);
	}

	public String toString() {
		return "Node "+ this.nodeId;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}
	
	public Node getPrevNode() {
		return previous;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setPrevNode(Node rightNode) {
		this.previous = rightNode;
	}

	public Node getNextNode() {
		return next;
	}

	public void setNextNode(Node leftNode) {
		this.next = leftNode;
	}

	public boolean isAction() {
		return action;
	}

	public void setAction(boolean action) {
		this.action = action;
	}

}