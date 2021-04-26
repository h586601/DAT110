package no.hvl.dat110.ac.restservice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;

public class AccessLog {

	// atomic integer used to obtain identifiers for each access entry
	private AtomicInteger cid;
	protected ConcurrentHashMap<Integer, AccessEntry> log;

	public AccessLog () {
		this.log = new ConcurrentHashMap<Integer,AccessEntry>();
		cid = new AtomicInteger(0);
	}

	// TODO: add an access entry to the log for the provided message and return assigned id
	public int add(String message) {

		int id = 0;

		id = cid.incrementAndGet();

		AccessEntry accessEntry = new AccessEntry(id, message);
		log.put(id, accessEntry);

		return id;
	}

	// TODO: retrieve a specific access entry from the log
	public AccessEntry get(int id) {

		return log.get(id);

	}

	// TODO: clear the access entry log
	public void clear() {
		log.clear();
		cid.set(0);
	}

	// TODO: return JSON representation of the access log
	public String toJson () {

		Gson gson = new Gson();

		String json = gson.toJson(log.values());

    	return json;
    }
}
