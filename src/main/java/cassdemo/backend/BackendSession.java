package cassdemo.backend;

import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/*
 * For error handling done right see:
 * https://www.datastax.com/dev/blog/cassandra-error-handling-done-right
 *
 * Performing stress tests often results in numerous WriteTimeoutExceptions,
 * ReadTimeoutExceptions (thrown by Cassandra replicas) and
 * OpetationTimedOutExceptions (thrown by the client). Remember to retry
 * failed operations until success (it can be done through the RetryPolicy mechanism:
 * https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy )
 */

public class BackendSession {

	private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

	private Session session;

	public BackendSession(String contactPoint, String keyspace) throws BackendException {

		Cluster cluster = Cluster.builder().addContactPoint(contactPoint).build();
		try {
			session = cluster.connect(keyspace);
		} catch (Exception e) {
			throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
		}
		prepareStatements();
	}

	private static PreparedStatement UPDATE_ROOM_USER;
	private static PreparedStatement DELETE_USER_RESERVATIONS;

	private static PreparedStatement SELECT_ROOM;
	private static PreparedStatement INSERT_RESERVATION;


	private void prepareStatements() throws BackendException {
		try {

			UPDATE_ROOM_USER = session.prepare("UPDATE Room SET user = ? where size = ? and roomId = ?");
			DELETE_USER_RESERVATIONS = session.prepare("DELETE from Reservation where user = ?");
			SELECT_ROOM = session.prepare("SELECT * from Room where size = ?");
			INSERT_RESERVATION = session.prepare("INSERT INTO Reservation (roomId, user) VALUES (?,?) IF NOT EXISTS;");
		} catch (Exception e) {
			throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
		}

		logger.info("Statements prepared");
	}

	public void updateRoom(String user, Room room) throws BackendException {
		BoundStatement bs = new BoundStatement(UPDATE_ROOM_USER);
		bs.bind(user, room.getSize(), room.getRoomId());
		try {
			session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform a room update. " + e.getMessage() + ".", e);
		}
	}

	public void deleteReservations(String user) throws BackendException{
		BoundStatement bs = new BoundStatement(DELETE_USER_RESERVATIONS);
		bs.bind(user);
		try {
			session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform an delete. " + e.getMessage() + ".", e);
		}
	}
	public List<Room> getAvaliableRooms(Integer size) throws BackendException {
		List<Room> filteredRooms = new ArrayList<>();

		BoundStatement bs = new BoundStatement(SELECT_ROOM);
		bs.bind(size);
		ResultSet rs = null;
		try {
			rs = session.execute(bs);
		} catch (Exception e) {
			throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
		}
		for (Row row : rs) {
			int roomId = row.getInt("roomId");
			String user = row.getString("user");
			int roomSize = row.getInt("size");
			if (user == null) {
				filteredRooms.add(new Room(roomId, user, roomSize));
			}
		}
		return filteredRooms;
	}

	public boolean trytoreserveroom(String user, Room room) throws BackendException {
		BoundStatement bs = new BoundStatement(INSERT_RESERVATION);
		bs.bind(room.getRoomId(), user);
		ResultSet rs = null;
		boolean result;
		try {
			rs = session.execute(bs);
			result = rs.one().getBool("[applied]");
		} catch (Exception e) {
			throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
		}
		return result;
	}

	protected void finalize() {
		try {
			if (session != null) {
				session.getCluster().close();
			}
		} catch (Exception e) {
			logger.error("Could not close existing cluster", e);
		}
	}

	public void deleteRoomReservation(Room room) {
	}
}