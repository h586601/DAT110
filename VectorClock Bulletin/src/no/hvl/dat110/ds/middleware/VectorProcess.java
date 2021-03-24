package no.hvl.dat110.ds.middleware;

/**
 * @author tdoy
 * Based on Section 6.2: Distributed Systems - van Steen and Tanenbaum (2017)
 * For demo/teaching purpose in dat110 class
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.hvl.dat110.ds.middleware.iface.OperationType;
import no.hvl.dat110.ds.middleware.iface.ProcessInterface;
import no.hvl.dat110.ds.util.ProcessConfig;
import no.hvl.dat110.ds.util.Util;

public class VectorProcess extends UnicastRemoteObject implements ProcessInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private VectorClock vectorclock;
	private int processID;
	private String processName;
	private List<ProcessConfig> replicas;
	private Message msg;
	private boolean newevent = false;
	private boolean started = false;
	private Message receivedMessage;
	private ExecutorService backgroundExec = Executors.newCachedThreadPool();

	private List<Message> queue; // Warning this queue is not thread safe. It is your job to make it thread safe

	protected VectorProcess(String procName, int procid) throws RemoteException {
		super();
		processID = procid;
		processName = procName;
		replicas = Util.getProcessReplicas();
		int procIndex = indexOf(procName); // return the position of this process in the process list
		vectorclock = new VectorClock(replicas.size(), procIndex);
		queue = new ArrayList<>();
	}

	private int indexOf(String procName) {

		for (int i = 0; i < replicas.size(); i++) {
			ProcessConfig pc = replicas.get(i);
			if (pc.getProcessName().equals(procName))
				return i;
		}

		return -1;
	}

	@Override
	public void buildMessage(OperationType optype) throws RemoteException {
		msg = new Message(vectorclock, processName);
		msg.setOptype(optype);
	}

	/**
	 * Used to simulate local event within this process
	 */
	@Override
	public void localEvent() {
		vectorclock.updateClockRule1(); // increment local clock entry
	}

	/**
	 * Example of how to send a message to remote processes
	 */
	@Override
	public void sendMessage(String procName, int port) {

		vectorclock.updateClockRule1();
		try {
			ProcessInterface p = Util.getProcessStub(procName, port);
			p.onReceivedMessage(msg);

		} catch (RemoteException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void multicastMessage() throws RemoteException {

		vectorclock.updateClockRule1();

		VectorClock vc = new VectorClock(vectorclock.getVectorclock(), vectorclock.getIndex());

		Message msg_c = new Message(vc, processName);
		msg_c.setOptype(msg.getOptype());

		for (ProcessConfig rep : replicas) {
			if (rep.getProcessName() != processName) {
				ProcessInterface pi = Util.getProcessStub(rep.getProcessName(), rep.getPort());
				pi.onReceivedMessage(msg_c);
			}
		}

	}

	/**
	 * Variant of multicastMessage() but uses a delay between sending (e.g.
	 * Thread.sleep())
	 */
	@Override
	public void multicastMessage(long delay) throws RemoteException {

		vectorclock.updateClockRule1();

		VectorClock vc = new VectorClock(replicas.size(), indexOf(processName));

		Message msg_c = new Message(vc, processName);
		msg_c.setOptype(msg.getOptype());

		// Multicast message: warning! - only multicast to other processes
		for (ProcessConfig rep : replicas) {
			if (rep.getProcessName() != processName) {
				ProcessInterface pi = Util.getProcessStub(rep.getProcessName(), rep.getPort());
				pi.onReceivedMessage(msg_c);
			}

			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onReceivedMessage(Message message) throws RemoteException {

		/**
		 * check that messages preceding this message have been delivered. if true,
		 * deliver message otherwise, queue the message
		 **/
		if (vectorclock.deliverMessage(message.getVectorClock())) {
			deliverMessage(message);
		} else {
			queue.add(message);
			if (!started) {
				started = true;
				checkQueue();
			}
		}

//		checkQueue();
	}

	private void deliverMessage(Message message) throws RemoteException {
		newevent = true;

		receivedMessage = message;
		vectorclock.updateClockRule2(message.getVectorClock()); // apply clock update rules: get the max for each local
																// entry and increment the local clock

		// printing to the console
		System.out.println(processName + " delivered " + message.getOptype().name() + " message to application");
		vectorclock.printClock();
	}

	/**
	 * Start a thread to check the queue periodically
	 */
	private void checkQueue() {

		backgroundExec.execute(new Runnable() {

			@Override
			public void run() {
				while (started) {
					try {
						Thread.sleep(1000);
						processMessage();
						if (queue.isEmpty())
							started = false;
					} catch (InterruptedException e) {
						//
					}
				}

			}
		});
	}

	private void processMessage() {
		System.out.println(this.processName + ": Size of queue: " + queue.size());
		List<Message> dup = new ArrayList<>(queue);
		dup.forEach(m -> {
			if (vectorclock.deliverMessage(m.getVectorClock())) {
				try {
					deliverMessage(m);
					queue.remove(index(m));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private int index(Message m) {
		for (int i = 0; i < queue.size(); i++) {
			if (queue.get(i).getProcessID() == m.getProcessID())
				return i;
		}
		return -1;
	}

	@Override
	public int getProcessID() throws RemoteException {
		return processID;
	}

	@Override
	public Vector<Integer> getVectorclock() throws RemoteException {
		return vectorclock.getVectorclock();
	}

	@Override
	public boolean isNewevent() throws RemoteException {
		return newevent;
	}

	@Override
	public void setNewevent(boolean newevent) throws RemoteException {
		this.newevent = newevent;
	}

	@Override
	public Message getReceivedMessage() throws RemoteException {
		return receivedMessage;
	}

	public String getProcessName() throws RemoteException {
		return processName;
	}

}
