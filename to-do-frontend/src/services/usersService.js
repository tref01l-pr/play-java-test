import http from '../http-common';

class userService {

    userRegistration(data) {
        return http.post("/register", data);
    }

    login(data) {
        console.log(" login(data) " + data.email + " " + data.password);
        return http.post("/login", data);
    }

    authorization() {
        return http.post("/refreshAccessToken");
    }

    logOut() {
        return http.post("/logout");
    }

    getToDosByUsername(username) {
        return http.get("/todos/user/" + username);
    }

    createToDo(data) {
        return http.post("/todos", data);
    }

    updateToDoById(data) {
        return http.put("/todos", data);
    }

    deleteToDoById(id) {
        return http.delete("/todos/" + id);
    }
}

export default new userService();