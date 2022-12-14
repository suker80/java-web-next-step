package db;

import model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataBase {
    private static final Map<String, User> users = new HashMap<>();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }

    public DataBase() {
        User suker80 = new User("suker80", "123", "123", "123@123.com");
        users.put("suker80", suker80);

    }
}
