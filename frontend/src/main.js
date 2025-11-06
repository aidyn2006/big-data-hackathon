import { createApp } from 'vue'
import App from './App.vue'
import './theme.css'
import 'leaflet/dist/leaflet.css'
import axios from 'axios'

// Enable sending cookies with requests
axios.defaults.withCredentials = true

createApp(App).mount('#app')


