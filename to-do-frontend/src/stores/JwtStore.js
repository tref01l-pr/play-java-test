import { defineStore } from "pinia";

export const useJwtStore = defineStore("JwtStore", {
    state: () => {
        return {
            token: ""
        }
    },
    actions: {
        async setJwtToken(token) {
            this.token = token;
        },
        async clearJwtToken() {
            this.token = "";
        },
        async setJwtLocalStorage(token) {
            localStorage.setItem("jwt-token", this.token);
        },
        logOut() {
            this.token = "";
            localStorage.removeItem("jwt-token");
        }
    },
    getters: {
        getJwt() {
            return this.token;
        }
    }
});