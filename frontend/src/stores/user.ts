import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { User } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)

  function setUser(u: User | null) {
    user.value = u
  }

  return { user, setUser }
})
