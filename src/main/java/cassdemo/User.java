package cassdemo;

import cassdemo.backend.BackendException;
import cassdemo.backend.BackendSession;
import cassdemo.backend.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    private BackendSession session;
    private String username; // przez konstruktor gradle run args

    public User(BackendSession session, String username) {
        this.session = session;
        this.username = username;
    }

    public void loop() throws BackendException, InterruptedException {
        if (username == null) {
            logger.error("username is not set!");
        }
        Map<Integer, Integer> map = new HashMap<>();

        if(username.equals("conflict")) {
            map.put(3, 3);
            map.put(4, 5);
        } else
        if(username.equals("single")) {
            map.put(1, 1);
        } else
        if(username.equals("multi") || username.equals("multi2")) {
            map.put(4,3);
            map.put(2,2);
        } else {
            map.put(1, 1);
            map.put(4, 2);
        }

        while (true) {

            boolean canReserve=true;
            List<Room> toReserve = new ArrayList<>();
            for (Integer key : map.keySet()) {
                logger.info("get available rooms");
                List<Room> emptyRooms = session.getAvaliableRooms(key);
                logger.info("check size, is "+emptyRooms.size());

                if (emptyRooms.size() < map.get(key)) {
                    logger.warn("NOT ENOUGH ROOMS");
                    canReserve = false;
                    continue;
                }
                logger.info("try to reserve rooms");

                List<Room> reserved = reserveRooms(emptyRooms, map.get(key));
                if (reserved == null) {
                    logger.warn("NOT ENOUGH ROOMS");
                    session.deleteReservations(username);
                    canReserve = false;
                    continue;
                }
                toReserve.addAll(reserved);
            }
            logger.info("can reserve? " +canReserve);
            if(canReserve) {
                logger.info("actual reserving");

                for (Room room : toReserve) {
                    session.updateRoom(username, room);
                }
            }
            logger.info("going to sleep");

            Thread.sleep(5000);
            logger.info("cleaning up");

            session.deleteReservations(username);
            if(canReserve) {
                deleteRoomReservations(toReserve);
            }
            logger.info("end loop");
            Thread.sleep(10000);


        }
    }
    private void deleteRoomReservations(List<Room> reserved) {
        reserved.forEach(room -> {
            try {
                session.updateRoom(null, room);
            } catch (BackendException e) {
                e.printStackTrace();
            }
        });
    }

    private List<Room> reserveRooms(List<Room> rooms, Integer howMany) {
        List<Room> reserved = new ArrayList<>();
        boolean result;
        for (Room room : rooms) {
            result = false;
            try {
                result = session.trytoreserveroom(username, room);
            } catch (BackendException e) {
                e.printStackTrace();
            }
            if (result == true) {
                reserved.add(room);
                howMany--;
                if (howMany == 0) {
                    return reserved;
                }
            }
        }
        return null;
    }
}