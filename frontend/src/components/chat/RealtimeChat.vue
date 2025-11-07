<template>
  <div class="realtime-chat-overlay">
    <div class="realtime-chat-container">
      <button class="close-button" @click="handleClose">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>

      <div class="orb-container">
        <div ref="ringRef" class="ring" :class="{ listening: isRecording, playing: isPlaying }"></div>
        <div
          ref="orbRef"
          class="orb"
          :class="{ listening: isRecording, playing: isPlaying }"
          @click="handleOrbClick"
          :title="
            isPlaying
              ? 'Нажмите чтобы прервать и говорить'
              : isRecording
              ? 'Нажмите чтобы остановить'
              : 'Нажмите чтобы говорить с AI'
          "
        ></div>
      </div>

      <div class="status-text">
        {{ statusText }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const emit = defineEmits(['close'])

const isRecording = ref(false)
const isPlaying = ref(false)
const statusText = ref('Нажмите чтобы говорить')

const orbRef = ref(null)
const ringRef = ref(null)
const beepRef = ref(null)
const audioRef = ref(null)
const mediaRecorderRef = ref(null)
const audioChunksRef = ref([])

const createBeep = () => {
  const audioContext = new (window.AudioContext || window.webkitAudioContext)()
  const oscillator = audioContext.createOscillator()
  const gainNode = audioContext.createGain()

  oscillator.connect(gainNode)
  gainNode.connect(audioContext.destination)

  oscillator.frequency.value = 800
  oscillator.type = 'sine'

  gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
  gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.1)

  oscillator.start(audioContext.currentTime)
  oscillator.stop(audioContext.currentTime + 0.1)
}

const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

    if (beepRef.value) {
      try {
        beepRef.value.play()
      } catch (e) {
        console.log('Beep play failed:', e)
      }
    }

    audioChunksRef.value = []
    const mediaRecorder = new MediaRecorder(stream)
    mediaRecorderRef.value = mediaRecorder

    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunksRef.value.push(event.data)
      }
    }

    mediaRecorder.onstart = () => {
      isRecording.value = true
      statusText.value = 'Запись...'
    }

    mediaRecorder.onstop = async () => {
      isRecording.value = false
      stream.getTracks().forEach((track) => track.stop())

      if (audioChunksRef.value.length > 0) {
        await processAudio()
      } else {
        statusText.value = 'Нажмите чтобы говорить'
      }
    }

    mediaRecorder.start()
  } catch (err) {
    console.error('Error accessing microphone:', err)
    statusText.value = 'Ошибка доступа к микрофону'
  }
}

const stopRecording = () => {
  if (mediaRecorderRef.value && mediaRecorderRef.value.state === 'recording') {
    mediaRecorderRef.value.stop()
  }
}

const processAudio = async () => {
  statusText.value = 'Обрабатываю...'

  try {
    const audioBlob = new Blob(audioChunksRef.value, { type: 'audio/webm' })

    const formData = new FormData()
    formData.append('data', audioBlob, 'audio.webm')

    const response = await fetch('http://localhost:5678/webhook/process-audio', {
      method: 'POST',
      body: formData
    })

    if (!response.ok) {
      throw new Error(`Server response error: ${response.status}`)
    }

    // Получаем blob напрямую (MP3 файл)
    const blob = await response.blob()
    console.log('Received blob:', blob.type, blob.size, 'bytes')
    
    // Проигрываем MP3
    playAudioFromBlob(blob)

  } catch (err) {
    console.error('Error sending or processing response:', err)
    statusText.value = 'Ошибка связи с AI'
  }
}

const playAudioFromBlob = async (blob) => {
  const audioURL = URL.createObjectURL(blob)
  const audio = new Audio(audioURL)
  audioRef.value = audio

  audio.onplay = () => {
    isPlaying.value = true
    statusText.value = 'Отвечаю...'
  }

  audio.onended = () => {
    isPlaying.value = false
    statusText.value = 'Нажмите чтобы говорить'
    URL.revokeObjectURL(audioURL)
    audioRef.value = null
  }

  audio.onerror = (e) => {
    console.error('Audio playback error:', e)
    isPlaying.value = false
    statusText.value = 'Ошибка воспроизведения'
    URL.revokeObjectURL(audioURL)
    audioRef.value = null
  }

  try {
    await audio.play()
  } catch (e) {
    console.error('Audio play error:', e)
    statusText.value = 'Ошибка воспроизведения аудио'
  }
}

const stopAudio = () => {
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value.currentTime = 0
    audioRef.value = null
  }
  isPlaying.value = false
}

const handleClose = () => {
  // Останавливаем запись если она идёт
  if (mediaRecorderRef.value && mediaRecorderRef.value.state === 'recording') {
    mediaRecorderRef.value.stop()
    // Останавливаем все треки
    const stream = mediaRecorderRef.value.stream
    if (stream) {
      stream.getTracks().forEach((track) => track.stop())
    }
  }

  // Останавливаем воспроизведение
  stopAudio()

  // Сбрасываем состояния
  isRecording.value = false
  isPlaying.value = false
  statusText.value = 'Нажмите чтобы говорить'

  // Закрываем окно
  emit('close')
}

const handleOrbClick = () => {
  if (isPlaying.value) {
    stopAudio()
    statusText.value = 'Нажмите чтобы говорить'
    startRecording()
    return
  }

  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

onMounted(() => {
  beepRef.value = { play: createBeep }
})

onUnmounted(() => {
  if (mediaRecorderRef.value && mediaRecorderRef.value.state === 'recording') {
    mediaRecorderRef.value.stop()
  }
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value = null
  }
})
</script>

<style scoped>
.realtime-chat-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  backdrop-filter: blur(8px);
}

.realtime-chat-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  padding: 60px 40px 40px;
  background: linear-gradient(145deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  min-width: 400px;
}

.close-button {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  transition: all 0.3s ease;
}

.close-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

.orb-container {
  position: relative;
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring {
  position: absolute;
  width: 220px;
  height: 220px;
  border: 3px solid rgba(102, 126, 234, 0.3);
  border-radius: 50%;
  transition: all 0.3s ease;
}

.ring.listening {
  border-color: rgba(255, 107, 107, 0.6);
  animation: pulse-ring 1.5s ease-in-out infinite;
}

.ring.playing {
  border-color: rgba(46, 213, 115, 0.6);
  animation: pulse-ring 1.2s ease-in-out infinite;
}

@keyframes pulse-ring {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.7;
  }
}

.orb {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: linear-gradient(145deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
}

.orb::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transform: rotate(45deg);
  transition: all 0.6s ease;
}

.orb:hover {
  transform: scale(1.05);
  box-shadow: 0 15px 50px rgba(102, 126, 234, 0.6);
}

.orb:hover::before {
  left: 100%;
}

.orb.listening {
  background: linear-gradient(145deg, #ff6b6b 0%, #ee5a6f 100%);
  animation: pulse-orb 1.5s ease-in-out infinite;
  box-shadow: 0 10px 40px rgba(255, 107, 107, 0.5);
}

.orb.playing {
  background: linear-gradient(145deg, #2ed573 0%, #26de81 100%);
  animation: pulse-orb 1.2s ease-in-out infinite;
  box-shadow: 0 10px 40px rgba(46, 213, 115, 0.5);
}

@keyframes pulse-orb {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
}

.status-text {
  font-size: 18px;
  color: #fff;
  text-align: center;
  font-weight: 500;
  letter-spacing: 0.5px;
  min-height: 28px;
}
</style>
