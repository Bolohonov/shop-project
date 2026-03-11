import { ref, onUnmounted } from 'vue'

export function useSse() {
  const connected = ref(false)
  let eventSource: EventSource | null = null

  function connect(onStatusChanged: (data: any) => void) {
    const token = localStorage.getItem('shop_accessToken')
    if (!token) return

    const url = '/shop/api/v1/events/subscribe?token=' + token
    eventSource = new EventSource(url)

    eventSource.addEventListener('heartbeat', () => { connected.value = true })

    eventSource.addEventListener('order.status_changed', (e) => {
      try { onStatusChanged(JSON.parse(e.data)) } catch {}
    })

    eventSource.onerror = () => {
      connected.value = false
      eventSource?.close()
      setTimeout(() => connect(onStatusChanged), 5000)
    }
  }

  function disconnect() {
    eventSource?.close()
    eventSource = null
    connected.value = false
  }

  onUnmounted(disconnect)
  return { connected, connect, disconnect }
}
