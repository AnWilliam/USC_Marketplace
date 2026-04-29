package service;

import java.sql.SQLException;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.ValidationUtil;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAO());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(String email, String password, String name) throws SQLException {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        if (!ValidationUtil.isUSCEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Only @usc.edu emails are allowed.");
        }
        if (ValidationUtil.isBlank(password) || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (ValidationUtil.isBlank(name)) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (userDAO.findByEmail(normalizedEmail) != null) {
            throw new IllegalArgumentException("This email is already registered.");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setName(name.trim());
        int userID = userDAO.create(user);
        return userDAO.findById(userID);
    }

    public User login(String email, String password) throws SQLException {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        if (ValidationUtil.isBlank(normalizedEmail) || ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userDAO.findByEmail(normalizedEmail);
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    public User getProfile(int userID) throws SQLException {
        User user = userDAO.findById(userID);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user;
    }

    public User updateProfile(int userID, String bio, String profilePicture) throws SQLException {
        boolean updated = userDAO.updateProfile(userID, bio, profilePicture);
        if (!updated) {
            throw new IllegalArgumentException("User not found.");
        }
        return userDAO.findById(userID);
    }
}
