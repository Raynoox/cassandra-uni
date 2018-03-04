package cassdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cassdemo.backend.BackendException;
import cassdemo.backend.BackendSession;
import cassdemo.backend.Room;

public class Main {

	private static final String PROPERTIES_FILENAME = "config.properties";

	public static void main(String[] args) throws IOException, BackendException, InterruptedException {
		String contactPoint = null;
		String keyspace = null;
		String username = args[0];

		Properties properties = new Properties();
		try {
			properties.load(Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));
			contactPoint = properties.getProperty("contact_point");
			keyspace = properties.getProperty("keyspace");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		BackendSession session = new BackendSession(contactPoint, keyspace);

		User user1 = new User(session, username);
		user1.loop();

		System.exit(0);
	}
}