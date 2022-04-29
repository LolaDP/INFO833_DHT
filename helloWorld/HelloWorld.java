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
		//Message(0,"texte",noeud emetteur)
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
		//Message(1, "join", noeud emetteur)
		case 1:
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
		
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			System.out.println(this +", uid "+this.getUid()+" : Received " + msg.getContent() + " from "+ msg.getNode().getUid());
			
			//cas ou il n'y a qu'un seul noeud dans l'anneau
			if (this.nodeId==this.getNextNode().getID()) {
				//definition des messages pour mettre a jours les voisins du noeud emeteur
				Message changePNmsg=new Message(3,"change prev node",this);
				Message changeNNmsg=new Message(4,"change next node",this);
				
				//defintion des nouveaux voisins
				this.send(changePNmsg, msg.getNode().getMyNode());
				this.send(changeNNmsg, msg.getNode().getMyNode());
				this.setNextNode(msg.getNode().getMyNode());
				this.setPrevNode(msg.getNode().getMyNode());
				System.out.println("Node " + msg.getNode().getNodeId()+ ", uid "+ msg.getNode().getUid()+ " bien inséré entre " + this.uid + " et "+this.uid);
			}
			
			//cas generique (+de 1 noeud)
			//l'uid du noeud voulant rejoindre est superieur a celui du noeud recpeteur
			else if (msg.getNode().getUid()>this.uid) {
				
				HelloWorld next=(HelloWorld)this.getNextNode().getProtocol(this.mypid);
				
				//si l'uid du noeud voulant rejoindre est inferieur a celui du voisin netx du noeud recpeteur ou que le noeud recpteur est le plus grand de l'anneau 
				//on s'insere
				if (msg.getNode().getUid()<next.getUid() || this.getUid()>next.getUid()) {
					//definition des messages pour mettre a jours les voisins du noeud emeteur
					Message changePNmsg=new Message(3,"change prev node",this);
					Message changeNNmsg=new Message(4,"change next node",next);
				
					//on defini les voisins du noeud en insertion
					this.send(changePNmsg, msg.getNode().getMyNode());
					this.send(changeNNmsg, msg.getNode().getMyNode());
					
					//on redefini les voisins des deux noeuds implique dans l'insertion du nouveau noeud
					Message changePNnext=new Message(3,"change prev node", msg.getNode());
					msg.getNode().send(changePNnext, next.getMyNode());
					this.setNextNode(msg.getNode().getMyNode());
					System.out.println(this +", uid "+this.getUid()+" updated, new next node: "+this.getNextNode().getID());
					System.out.println("Node " + msg.getNode().getNodeId()+ ", uid "+ msg.getNode().getUid()+ " bien inséré entre " + this.uid + " et "+next.getUid());
				}
				
				//sinon ce n'est pas le bon endroit pour s'inserer, on transfere le msg au noeud suivant
				else {
					this.send(msg, this.getNextNode());
				}
			}
			
			//l'uid du noeud voulant rejoindre est inferieur a celui du noeud recpeteur
			else if (msg.getNode().getUid()<this.uid) {
				HelloWorld prev=(HelloWorld)this.getPrevNode().getProtocol(this.mypid);
				
				//si l'uid du noeud voulant rejoindre est superieur a celui du voisin prev du noeud recpeteur ou que le noeud recpteur est le plus petit de l'anneau 
				//on s'insere
				if (msg.getNode().getUid()>prev.getUid() || this.getUid()<prev.getUid()) {
					//definition des messages pour mettre a jours les voisins du noeud emeteur
					Message changePNmsg=new Message(3,"change prev node",prev);
					Message changeNNmsg=new Message(4,"change next node",this);
					
					//on defini les voisins du noeud en insertion
					this.send(changePNmsg, msg.getNode().getMyNode());
					this.send(changeNNmsg, msg.getNode().getMyNode());
					
					//on redefini les voisins des deux noeuds implique dans l'insertion du nouveau noeud
					Message changePNnext=new Message(4,"change next node", msg.getNode());
					msg.getNode().send(changePNnext, prev.getMyNode());
					this.setPrevNode(msg.getNode().getMyNode());
					System.out.println(this +", uid "+this.getUid()+" updated, new prev node: "+this.getPrevNode().getID());
					System.out.println("Node " + msg.getNode().getNodeId()+ ", uid " +msg.getNode().getUid()+ " bien inséré entre " + prev.getUid() + " et "+this.uid);
				}
				
				//sinon ce n'est pas le bon endroit pour s'inserer, on transfere le msg au noeud precedent
				else {
					this.send(msg, this.getPrevNode());
				}
			}
			
			this.setAction(false);
			break;
			
		//message leave, le noeud veut quitter l'anneau (recu par voisin next du noeud sortant)
		//Message(2, "leave", noeud emetteur)
		case 2: 
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
			
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			System.out.println("Node" + msg.getNode().getNodeId() +", uid "+msg.getNode().getUid()+" want to leave");
			Node prevNode=msg.getNode().getPrevNode();

			this.setPrevNode(null);
			
			//definition des messages pour mettre a jours les voisins du noeud sortant
			Message changePNmsg=new Message(3,"change prev node",null);
			Message changeNNmsg=new Message(4,"change next node",null);
			Message changeNNprev=new Message(4,"change next node",this);
			
			//on defini les voisins du noeud sortant a null
			this.send(changePNmsg, msg.getNode().getMyNode());
			this.send(changeNNmsg, msg.getNode().getMyNode());
			
			//mise a jour des voisins lies au noeud sortant
			this.send(changeNNprev, prevNode);
			this.setPrevNode(prevNode);
			
			System.out.println(this +", uid "+this.getUid()+" updated, new prev node: "+this.getPrevNode().getID());
			System.out.println("Node uid "+ msg.getNode().getUid()+ " has left ");
			
			this.setAction(false);
			break;
			
		//message de modification du voisin prev
		//Message(3, "change prev neighboor", noeud nouveau voisin)
		case 3:
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			System.out.println(this +", uid "+this.getUid()+" : Received " + msg.getContent());
			
			if (msg.getNode()==null) {
				this.setPrevNode(null);
				System.out.println(this +", uid "+this.getUid()+" updated, new prev node: "+this.getPrevNode());
				
			}
			
			else {
				this.setPrevNode(msg.getNode().getMyNode());
				System.out.println(this +", uid "+this.getUid()+" updated, new prev node: "+this.getPrevNode().getID());
				//confirmation de l'action
				Message okMsg3=new Message(6, "action done", this);
				this.send(okMsg3, msg.getNode().getMyNode());
			}
			
			this.setAction(false);
			break;
			
		//message de modification du voisin next
		//Message(4, "change next neighboor", noeud nouveau voisin)
		case 4:
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			System.out.println(this +", uid "+this.getUid()+" : Received " + msg.getContent());
			
			if (msg.getNode()==null) {		
				this.setNextNode(null);
				System.out.println(this +", uid "+this.getUid()+" updated, new next node: "+this.getNextNode());
			}
			
			else {
				this.setNextNode(msg.getNode().getMyNode());
				System.out.println(this +", uid "+this.getUid()+" updated, new next node: "+this.getNextNode().getID());
				//confirmation de l'action
				Message okMsg4=new Message(6, "action done", this);
				this.send(okMsg4, msg.getNode().getMyNode());
			}
			
			this.setAction(false);
			break;
		
		//mesage d'un noeud A pour un noeud B specifique
		//Message(5, "identifiant du noeud destinataire", noeud emetteur)
		case 5:
			//verifie si le noeud n'est pas deja occupe
			while(this.isAction()) {
				
			}
			//on bloque les messages qui pourraient arriver pendant l'action  
			this.setAction(true);
			
			System.out.println(this +", uid "+this.getUid()+" : received a message from "+  msg.getNode().getUid()+ " send to node uid:" + msg.getContent());
			
			if(Integer.parseInt(msg.getContent())==this.getUid()) {
				System.out.println(this +", uid: "+this.getUid()+" received its message from "+ msg.getNode().getUid());
			}
			else if (Integer.parseInt(msg.getContent())>this.uid) {
				this.send(msg, this.getNextNode());
				System.out.println("the message isn't for me, transfered to: "+this.getNextNode().getID());
			}
			else if (Integer.parseInt(msg.getContent())<this.uid) {
				this.send(msg, this.getPrevNode());
				System.out.println("the message isn't for me, transfered to: "+this.getPrevNode().getID());
			}
			
		//message pour confirmer une action
		//Message(6, "action done", noeud emetteur)
		case 6:
			System.out.println(this+ ", uid:"+this.getUid()+" received "+msg.getContent()+" from "+ msg.getNode().getUid());
			this.setAction(false);
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