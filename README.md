# INFO833_DHT

Les méthodes d'envoie de message à tous les noeuds, join, leave et modification des voisins précédent et suivant fonctionnent. 
La mise à jour des voisins d'un noeud suite à l'arrivée ou au départ d'un autre ne sont plus fait avec des set mais avec l'utilisation de messages de changements des voisins.
On observe alors des problèmes d'actions concurrentes lorsque plusieurs messages se succèdent (par exemple deux join).
