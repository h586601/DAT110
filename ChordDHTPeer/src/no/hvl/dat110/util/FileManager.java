package no.hvl.dat110.util;

/**
 * @author tdoy
 * dat110 - DSLab 2
 */

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.hvl.dat110.rpc.interfaces.NodeInterface;

public class FileManager {

	/**
	 * Given a filename, makes replicas and distributes them to all active peers
	 * such that pred < replica <= peer
	 * 
	 * @throws RemoteException
	 */
	public void distributeReplicastoPeers(String filename) throws RemoteException {

		// create replicas of the filename
		BigInteger[] replicas = Util.createReplicaFiles(filename, Util.numReplicas);

		// collect the 5 processes from the Util class
		Map<String, Integer> processes = Util.getProcesses();

		// iterate over the processes(peers) and the replicas
		// for each replica, add the replica to the peer if the condition: pred <
		// replica <= peer is satisfied
		for (Map.Entry<String, Integer> process : processes.entrySet()) {
			String name = process.getKey();
			int port = processes.get(name);
			NodeInterface node = Util.getProcessStub(name, port);
			BigInteger nodeID = node.getNodeID();
			BigInteger predID = node.getPredecessor().getNodeID();
			for (BigInteger rep : replicas) {
				boolean isPeer = Util.computeLogic(rep, predID, nodeID);
				if (isPeer) {
					node.addKey(rep);
				}
			}
		}
	}

	/**
	 * Given a filename, finds all the peers that hold a copy of that file
	 * 
	 * @param filename
	 * @return list of active nodes having the replicas of this file
	 * @throws RemoteException
	 */
	public Set<NodeInterface> requestActiveNodesForFile(String filename) throws RemoteException {

		Set<NodeInterface> peers = new HashSet<>();

		BigInteger[] replicas = Util.createReplicaFiles(filename, Util.numReplicas);

		Map<String, Integer> processes = Util.getProcesses();

		for (Map.Entry<String, Integer> process : processes.entrySet()) {
			String name = process.getKey();
			int port = processes.get(name);
			NodeInterface node = Util.getProcessStub(name, port);
			BigInteger nodeID = node.getNodeID();
			BigInteger predID = node.getPredecessor().getNodeID();

			for (BigInteger rep : replicas) {
				boolean isPeer = Util.computeLogic(rep, predID, nodeID);
				if (isPeer) {
					peers.add(node);
				}
			}
		}

		return peers;
	}

}
