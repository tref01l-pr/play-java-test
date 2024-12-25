<template>
  <form @submit.prevent="performAction">
    <div class="mb-4 grid gap-4 sm:grid-cols-2">
      <TodoInput
        label="Title"
        id="title"
        v-model="todo.title"
        placeholder="Type task title"
        required
      />
      <TodoTextBox
        label="Description"
        id="description"
        v-model="todo.description"
        placeholder="Type task description"
        required
      />
      <TodoInput
          label="Tags"
          id="tags"
          v-model="todo.tags"
          placeholder="Enter tags separated by commas"
      />
    </div>
    <button
      type="submit"
      class="inline-flex items-center rounded-lg bg-green-700 px-5 py-2.5 text-center text-sm font-medium text-white hover:bg-green-800 focus:outline-none focus:ring-4 focus:ring-green-300 dark:bg-green-600 dark:hover:bg-green-700 dark:focus:ring-green-800"
    >
      {{ props.todoId ? 'Update' : 'Add' }} Task
    </button>
  </form>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useToDosStore } from '../../stores/ToDosStore.js'
import TodoInput from './Form/TodoInput.vue'
import TodoTextBox from './Form/TodoTextBox.vue'
import TodoDropdown from './Form/TodoDropdown.vue'

const emit = defineEmits(['close-modal'])

const props = defineProps({
  todoId: {
    type: String,
    default: null
  }
})
const tasksStore = useToDosStore()
const todo = ref({
  title: '',
  description: '',
  status: 'todo',
  tags: ''
})

const statusList = [
  { text: 'Todo', value: 'todo' },
  { text: 'In Progress', value: 'in-progress' },
  { text: 'Done', value: 'done' }
]

const performAction = () => {
  if (props.todoId) {
    updateTask()
  } else {
    addTask()
  }
}

const addTask = () => {
  const preparedData = prepareToDoData(todo.value)
  tasksStore.createToDo(preparedData)
  resetTodo()
  emit('close-modal')
}

const updateTask = () => {
  const preparedData = prepareToDoData(todo.value)
  tasksStore.updateToDoById(preparedData)
  resetTodo()
  emit('close-modal')
}

const prepareToDoData = (data) => {
  return {
    ...data,
    tags: data.tags ? data.tags.split(',').map(tag => tag.trim()) : [],
    ...(props.todoId ? { id: props.todoId } : {})
  }
}

const resetTodo = () => {
  todo.value = {}
}

onMounted(() => {
  if (props.todoId) {
    const fetchedTodo = tasksStore.getTodoById(props.todoId)
    todo.value = { ...fetchedTodo }
  }
})
</script>

<style lang="scss" scoped></style>
