package helloWorld;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import peersim.config.*;

/*
  Module d'initialisation de helloWorld: 
  Fonctionnement:
    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
 */
public class Initializer implements peersim.core.Control {

	private int helloWorldPid;

	public Initializer(String prefix) {
		//recuperation du pid de la couche applicative
		this.helloWorldPid = Configuration.getPid(prefix + ".helloWorldProtocolPid");
	}

	public boolean execute() {
		int nodeNb, nbLie;
		HelloWorld emitter, current, current2;
		Node dest, noeud, joinNode;
		Message helloMsg, joinMsg;

		//recuperation de la taille du reseau
		nodeNb = Network.size();
		//que 1 noeuds lies au depart
		nbLie =1;
		
		if (nodeNb < 1) {
			System.err.println("Network size is not positive");
			System.exit(1);
		}
		
		//recuperation de la couche applicative de l'emetteur (le noeud 0)
		emitter = (HelloWorld)Network.get(0).getProtocol(this.helloWorldPid);
		emitter.setTransportLayer(0);

		//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport et on lui attribut un uid aleatoirement
		for (int i = 0; i < nbLie; i++) {
			dest = Network.get(i);
			current = (HelloWorld)dest.getProtocol(this.helloWorldPid);
			current.setTransportLayer(i);
			//uid a attribuer alleatoirement en 0 et 10000 
			// on suppose que le nombre de uid a attribuer et suffisement faible devant 10000 pour que la proba
			current.setUid((10000-0+1)==1?0:0 + CommonState.r.nextInt((10000-0+1)));
			current.setState(true);
		}
		
		//attributions des voisins 
	    List<Integer> uidList=new ArrayList<>();
		int next=0;
		for ( int n=0 ; n<nbLie; n++) {
			noeud = Network.get(n);
			current2 = (HelloWorld)noeud.getProtocol(this.helloWorldPid);
			uidList.add(current2.getUid());
		}
		Collections.sort(uidList);
		for ( int n=0 ; n<nbLie; n++) {
			noeud = Network.get(n);
			current2 = (HelloWorld)noeud.getProtocol(this.helloWorldPid);
			for (int i=0; i<uidList.size(); i++) {
				if (current2.getUid()==uidList.get(i) && i==uidList.size()-1) {
					next=uidList.get(0);
				}
				else if (current2.getUid()==uidList.get(i)) {
					next=uidList.get(i+1);
				}
			}
			for (int m=0; m<nbLie; m++) {
				//voisin
				dest = Network.get(m);
				current = (HelloWorld)dest.getProtocol(this.helloWorldPid);
				if (current.getUid()==next) {
					
					current2.setNextNode(dest);
					current.setPrevNode(noeud);
				}
	
			}
		}
		
		//test liaisons de l'anneaux sont correctes, les noeuds ont les bons voisins
		for ( int n=0 ; n<nbLie; n++) {
			dest=Network.get(n);
			current2 = (HelloWorld)dest.getProtocol(this.helloWorldPid);
			System.out.println(current2.getUid()+"  id:"+current2.getMyNode().getID());
			System.out.println(current2.getPrevNode().getID() );
		System.out.println(current2.getNextNode().getID() );
		}
	
		//le noeud 3 envoie un join
		joinNode=Network.get(3);
		current = (HelloWorld)joinNode.getProtocol(this.helloWorldPid);
		current.setTransportLayer(3);
		current.setUid((10000-0+1)==1?0:0 + CommonState.r.nextInt((10000-0+1)));
		joinMsg = new Message(1,"Join",current);
		current.send(joinMsg, Network.get(0));
		
		//le noeud 1 envoie un join
		joinNode=Network.get(1);
		current = (HelloWorld)joinNode.getProtocol(this.helloWorldPid);
		current.setTransportLayer(1);
		current.setUid((10000-0+1)==1?0:0 + CommonState.r.nextInt((10000-0+1)));
		joinMsg = new Message(1,"Join",current);
		current.send(joinMsg, Network.get(0));
		
		//le noeud 2 envoie un join
		joinNode=Network.get(2);
		current = (HelloWorld)joinNode.getProtocol(this.helloWorldPid);
		current.setTransportLayer(2);
		current.setUid((10000-0+1)==1?0:0 + CommonState.r.nextInt((10000-0+1)));
		joinMsg = new Message(1,"Join",current);
		current.send(joinMsg, Network.get(0));
		
		//le noeud 4 envoie un join
		joinNode=Network.get(4);
		current = (HelloWorld)joinNode.getProtocol(this.helloWorldPid);
		current.setTransportLayer(4);
		current.setUid((10000-0+1)==1?0:0 + CommonState.r.nextInt((10000-0+1)));
		joinMsg = new Message(1,"Join",current);
		current.send(joinMsg, Network.get(0));
		
		//le noeud 0 envoie a son voisin next
		//creation du message
		helloMsg = new Message(Message.HELLOWORLD,"Hello!!",emitter);
		dest= emitter.getNextNode();
		emitter.send(helloMsg, dest);
//	
//		//le noeud 1 envoie un message a son next pour quitter l'anneau 
//		Node leaveNode=Network.get(1);
//		current = (HelloWorld)leaveNode.getProtocol(this.helloWorldPid);
//		current.setTransportLayer(1);
//		Message leaveMsg=new Message(2,"Leave",current);
//		current.send(leaveMsg, current.getNextNode());
		
		System.out.println("Initialization completed");
		return false;
	}
}
