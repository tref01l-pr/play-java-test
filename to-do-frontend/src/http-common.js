import axios from "axios";
import { useJwtStore } from "./stores/JwtStore.js";

const instance = axios.create({
    baseURL: "http://localhost:9000",
    headers: {
        "Content-type": "application/json",
    },
    withCredentials: true,
});

instance.interceptors.request.use((config) => {
    const token = useJwtStore().getJwt;
    if (token !== "") {
        config.headers.Authorization = `Bearer ${token}`;
    } else {
        delete instance.defaults.headers.common["Authorization"];
    }
    return config;
});

export default instance;