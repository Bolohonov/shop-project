<script setup lang="ts">
import { useToast } from '@/composables/useToast'
const { toasts } = useToast()
</script>

<template>
  <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2">
    <transition-group name="toast">
      <div v-for="toast in toasts" :key="toast.id"
        class="px-4 py-3 rounded-lg shadow-lg text-sm font-medium max-w-sm"
        :class="{
          'bg-green-600 text-white': toast.type === 'success',
          'bg-red-600 text-white': toast.type === 'error',
          'bg-indigo-600 text-white': toast.type === 'info'
        }">
        {{ toast.message }}
      </div>
    </transition-group>
  </div>
</template>

<style scoped>
.toast-enter-active { transition: all .3s ease-out; }
.toast-leave-active { transition: all .3s ease-in; }
.toast-enter-from { opacity: 0; transform: translateX(30px); }
.toast-leave-to { opacity: 0; transform: translateX(30px); }
</style>
