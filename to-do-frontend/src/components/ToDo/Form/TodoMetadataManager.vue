<script setup>

const props = defineProps({
  label: String,
  id: String,
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:modelValue']);

const removeItem = (index) => {
  const updatedArray = [...props.modelValue];
  updatedArray.splice(index, 1);
  emit('update:modelValue', updatedArray);
};
</script>

<template>
  <div v-if="modelValue.length > 0">
    <label
        :for="id"
        class="mb-2 block text-sm font-medium text-gray-900 dark:text-white"
    >
      {{ label }}
    </label>
    <div v-for="(item, index) in modelValue" :key="index" class="mb-4 border-b pb-2">
      <div class="flex items-center">
        <span
            :id="`${id}-${index}`"
            class="flex-1 text-sm text-gray-900"
        >
        {{ item.fileName }}
      </span>
        <button
            @click="removeItem(index)"
            class="ml-2 text-red-500 hover:text-red-700"
        >
          Remove
        </button>
      </div>
      <div class="mt-2 text-sm text-gray-500">
        Type: {{ item.fileType }} | Pages: {{ item.pageCount }}
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>