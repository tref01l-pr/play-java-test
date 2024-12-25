import { defineStore } from "pinia";
import { useRoute, useRouter } from "vue-router";
import usersService from "../services/usersService.js";
import {useUsersStore} from "./UsersStore.js";

export const useToDosStore = defineStore("ToDosStore", {
    state: () => {
        return {
            todos: [],
        };
    },
    actions: {
        async loadTodos() {
            try {
                const user = useUsersStore().getUser;
                const response = await usersService.getToDosByUsername(user.username);
                this.todos = [];
                console.log("asd");
                console.log(response.data);
                for (let i = 0; i < response.data.length; i++) {
                    this.todos.push({
                        id: response.data[i].id,
                        title: response.data[i].title,
                        description: response.data[i].description,
                        createdAt: new Date(response.data[i].createdAt).toISOString().slice(0, 19).replace("T", " "),
                        tags: response.data[i].tags || []
                    });
                }
                console.log("Todos fetched:", this.todos);
            } catch (error) {
                console.log("Error: " + error);
            }
        },
        async getToDosByUsername(username) {
            try {
                const response = await usersService.getToDosByUsername(username);
                this.todos = response.data.map(todo => ({
                    id: todo.id,
                    title: todo.title,
                    description: todo.description,
                    createdAt: new Date(todo.createdAt).toISOString().slice(0, 19).replace("T", " "),
                    tags: todo.tags || []
                }));
                console.log("Todos fetched:", this.todos);
            } catch (error) {
                console.error("Error fetching todos:", error);
            }
        },

        async createToDo(data) {
            try {
                const user = useUsersStore().getUser;
                const dataWithUserId = {
                    ...data,
                    userId: user.id,
                };

                const response = await usersService.createToDo(dataWithUserId);

                this.todos.push({
                    id: response.data.id,
                    title: response.data.title,
                    description: response.data.description,
                    createdAt: new Date(response.data.createdAt).toISOString().slice(0, 19).replace("T", " "),
                    tags: response.data.tags || []
                });
                console.log("ToDo created:", response.data);
            } catch (error) {
                console.error("Error creating todo:", error);
            }
        },

        async updateToDoById(data) {
            try {
                const response = await usersService.updateToDoById(data);
                const index = this.todos.findIndex(todo => todo.id === data.id);
                if (index !== -1) {
                    this.todos[index] = {
                        id: response.data.id,
                        title: response.data.title,
                        description: response.data.description,
                        createdAt: new Date(response.data.createdAt).toISOString().slice(0, 19).replace("T", " "),
                        tags: response.data.tags || []
                    };
                }
                console.log("ToDo updated:", response.data);
            } catch (error) {
                console.error("Error updating todo:", error);
            }
        },

        async deleteToDoById(id) {
            try {
                await usersService.deleteToDoById(id);
                    this.todos = this.todos.filter(todo => todo.id !== id);
                console.log(`ToDo with id ${id} deleted.`);
            } catch (error) {
                console.error("Error deleting todo:", error);
            }
        },
    },
    getters: {
        getTodoList: (state) => {
            return state.todos.map(todo => {
                return {
                    ...todo,
                    tags: todo.tags.join(', ')
                }
            });
        },
        getTodoById: (state) => (id) => {
            return state.todos.find((todo) => todo.id === id)
        },
        getTodoColumns: () => {
            return [
                {
                    key: 'id',
                    label: 'Id'
                },
                {
                    key: 'title',
                    label: 'Title'
                },
                {
                    key: 'description',
                    label: 'Description'
                },
                {
                    key: 'createdAt',
                    label: 'Created Date'
                },
                {
                    key: 'tags',
                    label: 'Tags'
                }
            ]
        }
    },
});