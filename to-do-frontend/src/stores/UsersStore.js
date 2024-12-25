import { defineStore } from "pinia";

export const useUsersStore = defineStore("UserStore", {

    state: () => {
        return {
            user: {
                id: "",
                username: "",
                userCreatedAt: ""
            }
        }
    },

    actions: {
        setUser(id, name, userCreatedAt) {
            this.user.id = id;
            this.user.username = name;
            this.user.userCreatedAt = userCreatedAt;
        },
        logOut() {
            this.user = {
                id: "",
                username: "",
                userCreatedAt: ""
            };
        }
    },

    getters: {
        getUser() {
            return this.user;
        },
        isUserLoggedIn() {
            return this.user.username !== "";
        }
    }
})
