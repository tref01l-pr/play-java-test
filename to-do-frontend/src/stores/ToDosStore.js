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
                for (let i = 0; i < response.data.length; i++) {
                    this.todos.push({
                        id: response.data[i].id,
                        title: response.data[i].title,
                        description: response.data[i].description,
                        createdAt: new Date(response.data[i].createdAt).toISOString().slice(0, 19).replace("T", " "),
                        tags: response.data[i].tags || [],
                        filesMetadata: response.data[i].filesMetadata || []
                    });
                }
            } catch (error) {
                console.log("Error: " + error);
            }
        },

        async createToDo(data) {
            try {
                const user = useUsersStore().getUser;
                const formData = new FormData();

                const dataWithUserId = {
                    userId: user.id,
                    title: data.title,
                    description: data.description,
                    tags: data.tags
                };

                formData.append("data", JSON.stringify(dataWithUserId));

                if (data.files && data.files.length > 0) {
                    data.files.forEach((file, index) => {
                        formData.append(`files`, file.file);
                    });
                }

                console.log("formData", formData.get("data"));
                console.log("formData", formData.get("files"));

                const response = await usersService.createToDo(formData);

                this.todos.push({
                    id: response.data.id,
                    title: response.data.title,
                    description: response.data.description,
                    createdAt: new Date(response.data.createdAt).toISOString().slice(0, 19).replace("T", " "),
                    tags: response.data.tags || [],
                    filesMetadata: response.data.filesMetadata || []
                });
                console.log("ToDo created:", response.data);
            } catch (error) {
                console.error("Error creating todo:", error);
            }
        },

        async updateToDoById(data) {
            try {
                const requestData = {
                    id: data.id,
                    title: data.title,
                    description: data.description,
                    tags: data.tags,
                    filesMetadata: data.filesMetadata
                }

                const formData = new FormData();
                formData.append("data", JSON.stringify(requestData));

                if (data.files && data.files.length > 0) {
                    data.files.forEach((file, index) => {
                        formData.append(`files`, file.file);
                    });
                }

                const response = await usersService.updateToDoById(formData);
                const index = this.todos.findIndex(todo => todo.id === data.id);
                if (index !== -1) {
                    this.todos[index] = {
                        id: response.data.id,
                        title: response.data.title,
                        description: response.data.description,
                        createdAt: new Date(response.data.createdAt).toISOString().slice(0, 19).replace("T", " "),
                        tags: response.data.tags || [],
                        filesMetadata: response.data.filesMetadata || []
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
        async exportToDoById(id) {
            try {
                const response = await usersService.exportToDoById(id);
                if (response && response.data) {
                    const contentDisposition = response.headers['content-disposition'];
                    const fileName = contentDisposition
                        ? contentDisposition.split('filename=')[1].replace(/"/g, '')
                        : 'todo-files.zip';

                    const fileBlob = response.data;

                    const url = URL.createObjectURL(fileBlob);
                    const link = document.createElement('a');
                    link.href = url;
                    link.download = fileName;

                    link.click();

                    URL.revokeObjectURL(url);
                }
            } catch (error) {
                console.error("Error exporting todo:", error);
            }
        }
    },
    getters: {
        getTodoList: (state) => {
            console.log(state.todos);
            return state.todos.map(todo => {
                return {
                    ...todo,
                    tags: todo.tags && todo.tags.length > 0 ? todo.tags.join(', ') : '-',
                    files: todo.filesMetadata && todo.filesMetadata.length > 0 ? todo.filesMetadata.length : '-'
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
                },
                {
                    key: 'files',
                    label: 'Files'
                }
            ]
        }
    },
});