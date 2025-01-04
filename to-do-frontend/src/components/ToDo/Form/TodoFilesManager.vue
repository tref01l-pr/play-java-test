<script setup>
import { ref } from 'vue';

const props = defineProps({
  label: String,
  id: String,
  modelValue: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['update:modelValue']);

const errorMessage = ref('');
const handleFileUpload = (event) => {
  const files = Array.from(event.target.files);
  const validFiles = files.filter((file) => file.type === 'application/pdf');
  const invalidFiles = files.filter((file) => file.type !== 'application/pdf');

  if (invalidFiles.length > 0) {
    errorMessage.value = `Incorrect files: ${invalidFiles.map((f) => f.name).join(', ')}`;
  } else {
    errorMessage.value = '';
  }

  const updatedArray = [
    ...props.modelValue,
    ...validFiles.map((file) => ({
      file: file,
      fileName: file.name,
      fileType: file.type,
      fileSize: (file.size / 1024).toFixed(2) + ' KB'
    })),
  ];

  emit('update:modelValue', updatedArray);
};

const removeItem = (index) => {
  const updatedArray = [...props.modelValue];
  updatedArray.splice(index, 1);
  emit('update:modelValue', updatedArray);
};
</script>

<template>
  <div>
    <label
        :for="id"
        class="mb-2 block text-sm font-medium text-gray-900 dark:text-white"
    >
      {{ label }}
    </label>

    <input
        :id="id"
        type="file"
        accept="application/pdf"
        multiple
        class="mb-4 block w-full rounded-lg border p-2 text-sm text-gray-900"
        @change="handleFileUpload"
    />
    <p v-if="errorMessage" class="mb-4 text-sm text-red-600">
      {{ errorMessage }}
    </p>

    <div v-if="modelValue.length > 0">
      <div
          v-for="(item, index) in modelValue"
          :key="index"
          class="mb-4 border-b pb-2"
      >
        <div class="flex items-center">
          <span class="flex-1 text-sm text-gray-900">
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
          Type: {{ item.fileType }} | Size: {{ item.fileSize }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>