package Contracts.Requests;

public class RegisterUserRequest {
    private String username;
    private String password;
    private String passwordConfirmation;

    public RegisterUserRequest(String username, String password, String passwordConfirmation) {
        this.username = username;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }
}
