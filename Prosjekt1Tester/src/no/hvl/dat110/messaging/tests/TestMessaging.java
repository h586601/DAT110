package no.hvl.dat110.messaging.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import no.hvl.dat110.messaging.Connection;
import no.hvl.dat110.messaging.Message;
import no.hvl.dat110.messaging.MessageConfig;
import no.hvl.dat110.messaging.MessagingClient;
import no.hvl.dat110.messaging.MessagingServer;

public class TestMessaging {

	private boolean failure;

	@Test
	public void test() {

		byte[] clientsent = { 1, 2, 3, 4, 5 };

		Thread server = new Thread() {

			public void run() {

				try {
					System.out.println("Messaging server - start");

					MessagingServer server = new MessagingServer(MessageConfig.MESSAGINGPORT);

					Connection connection = server.accept(); //Error

					Message request = connection.receive();

					byte[] serverreceived = request.getData();

					Message reply = new Message(serverreceived);

					connection.send(reply);

					connection.close();

					server.stop();

					System.out.println("Messaging server - stop");

					assertTrue(Arrays.equals(clientsent, serverreceived));

				} catch (Exception e) {
					e.printStackTrace();
					failure = true;
				}

			}
		};

		Thread client = new Thread() {

			public void run() {

				try {

					System.out.println("Messaging client - start");

					MessagingClient client = new MessagingClient(MessageConfig.MESSAGINGHOST,
							MessageConfig.MESSAGINGPORT);

					Connection connection = client.connect(); //Error

					Message message1 = new Message(clientsent);

					connection.send(message1);

					Message message2 = connection.receive();

					byte[] clientreceived = message2.getData();

					connection.close();

					System.out.println("Messaging client - stop");

					assertTrue(Arrays.equals(clientsent, clientreceived));
				} catch (Exception e) {
					e.printStackTrace();
					failure = true;
				}
			}

		};

		try {
			server.start();
			client.start();

			server.join();
			client.join();

		} catch (Exception e) {
			e.printStackTrace();
			failure = true;
		} finally {
			if (failure) {
				fail();
			}
		}

	}
}
