import { createWebHistory, createRouter } from "vue-router";
import {useUsersStore} from "./stores/UsersStore.js";
import {useAuthStore} from "./stores/AuthStore.js";

const routes =  [
    {
        path: "/test",
        component: () => import("./views/Test.vue")
    },
    {
        path: "/login",
        alias: ["/log-in", "/sign-in"],
        name: "login",
        component: () => import("./views/LogIn.vue"),
    },
    {
        path: "/registration",
        name: "registration",
        component: () => import("./views/SignUp.vue"),
    },
    {
        path: "/profile",
        name: "profile",
        component: () => import("./views/Profile.vue")
    },
    {
        path: "/",
        alias: ["/todos"],
        name: "todos",
        component: () => import("./views/ToDo.vue")
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach(async (to, from, next) => {
    if (!useAuthStore().hasInitialization) {
        await useAuthStore().authorization();
    }

    if (to.name !== "login" &&
        to.name !== "registration" &&
        to.name !== "notFound" &&
        !useUsersStore().isUserLoggedIn) {
        next({name: "login"});
    } else if ((to.name === "login" || to.name === "registration") && useUsersStore().isUserLoggedIn) {
        next({name: "profile"});
    } else next();
});

export default router;