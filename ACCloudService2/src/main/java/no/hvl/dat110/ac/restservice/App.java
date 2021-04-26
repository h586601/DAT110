package no.hvl.dat110.ac.restservice;

import static spark.Spark.after;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

import com.google.gson.Gson;

/**
 * Hello world!
 *
 */
public class App {

	static AccessLog accesslog = null;
	static AccessCode accesscode = null;

	public static void main(String[] args) {

		if (args.length > 0) {
			port(Integer.parseInt(args[0]));
		} else {
			port(8080);
		}

		// objects for data stored in the service

		accesslog = new AccessLog();
		accesscode = new AccessCode();

		after((req, res) -> {
			res.type("application/json");
		});

		// for basic testing purposes
		get("/accessdevice/hello", (req, res) -> {

			Gson gson = new Gson();

			return gson.toJson("IoT Access Control Device");
		});

		// TODO: implement the routes required for the access control service
		// as per the HTTP/REST operations describined in the project description
		post("/accessdevice/log", (req, res) -> {

			Gson gson = new Gson();

			AccessMessage message = gson.fromJson(req.body(), AccessMessage.class);

			int id = accesslog.add(message.getMessage());

			return gson.toJson(accesslog.get(id));

		});

		get("/accessdevice/log", (req, res) -> accesslog.toJson());

		get("/accessdevice/log/:id", (req, res) -> {

			Gson gson = new Gson();
			int id = Integer.parseInt(req.params(":id"));
			AccessEntry entry = accesslog.get(id);

			return gson.toJson(entry);
		});

		put("/accessdevice/code", (req, res) -> {

			Gson gson = new Gson();

			AccessCode code = gson.fromJson(req.body(), AccessCode.class);
			accesscode.setAccesscode(code.getAccesscode());

			return gson.toJson(code);
		});

		get("/accessdevice/code", (req, res) -> {
			Gson gson = new Gson();

			return gson.toJson(accesscode);
		});

		delete("/accessdevice/log", (req, res) -> {
			accesslog.clear();
			return accesslog.toJson();
		});
	}

}
