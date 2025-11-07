<template>
  <div class="resident-home">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-icon">🏙️</div>
        <h1 class="hero-title">Қош келдіңіз!</h1>
        <p class="hero-subtitle">Қоғамдық көлік туралы жалобаларды жіберіңіз</p>
        <p class="hero-description">
          AI агентіміз сіздің жалобаңызды талдап, маршрут, уақыт, орын және басымдықты анықтайды
        </p>
        
        <div class="hero-actions">
          <button @click="openChat" class="primary-action">
            <span class="action-icon">🤖</span>
            <div class="action-content">
              <div class="action-title">AI Чат</div>
              <div class="action-subtitle">Жалоба жіберу</div>
            </div>
          </button>
          
          <button @click="scrollToComplaints" class="secondary-action">
            <span class="action-icon">📋</span>
            <div class="action-content">
              <div class="action-title">Менің жалобаларым</div>
              <div class="action-subtitle">Статусты көру</div>
            </div>
          </button>
        </div>
      </div>
      
      <div class="hero-illustration">
        <div class="illustration-card">
          <div class="illustration-icon">🚌</div>
          <div class="illustration-text">Автобус</div>
        </div>
        <div class="illustration-card">
          <div class="illustration-icon">🤖</div>
          <div class="illustration-text">AI Агент</div>
        </div>
        <div class="illustration-card">
          <div class="illustration-icon">✅</div>
          <div class="illustration-text">Шешім</div>
        </div>
      </div>
    </div>
    
    <!-- Features Section -->
    <div class="features-section">
      <h2 class="section-title">Қалай жұмыс істейді?</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">💬</div>
          <h3 class="feature-title">1. Жазыңыз</h3>
          <p class="feature-text">Проблеманы қазақ немесе орыс тілінде жазыңыз</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">🧠</div>
          <h3 class="feature-title">2. AI талдайды</h3>
          <p class="feature-text">Маршрут, орын, уақыт және басымдықты анықтайды</p>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">📊</div>
          <h3 class="feature-title">3. Қадағалаңыз</h3>
          <p class="feature-text">Жалобаның статусын нақты уақытта көріңіз</p>
        </div>
      </div>
    </div>
    
    <!-- My Complaints Section -->
    <div ref="complaintsSection" class="complaints-section">
      <MyComplaints />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import MyComplaints from './MyComplaints.vue'

const emit = defineEmits(['openChat'])
const complaintsSection = ref(null)

function openChat() {
  emit('openChat')
}

function scrollToComplaints() {
  complaintsSection.value?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.resident-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.hero-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  padding: 60px 0;
  min-height: 70vh;
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-icon {
  font-size: 80px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.hero-title {
  font-size: 56px;
  font-weight: 900;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 24px;
  font-weight: 600;
  color: var(--text);
  margin: 0;
}

.hero-description {
  font-size: 16px;
  color: var(--muted);
  line-height: 1.6;
  margin: 0;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.primary-action, .secondary-action {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 16px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  font-family: inherit;
  text-align: left;
}

.primary-action {
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  color: white;
  box-shadow: 0 8px 24px rgba(167, 139, 250, 0.3);
}

.primary-action:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(167, 139, 250, 0.4);
}

.secondary-action {
  background: white;
  color: var(--text);
  border: 2px solid #E2E8F0;
}

.secondary-action:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.action-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.action-title {
  font-size: 18px;
  font-weight: 700;
}

.action-subtitle {
  font-size: 13px;
  opacity: 0.8;
}

.hero-illustration {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.illustration-card {
  background: white;
  border-radius: 20px;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  animation: slideIn 0.6s ease forwards;
  opacity: 0;
}

.illustration-card:nth-child(1) { animation-delay: 0.1s; }
.illustration-card:nth-child(2) { animation-delay: 0.2s; }
.illustration-card:nth-child(3) { animation-delay: 0.3s; }

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.illustration-card:hover {
  transform: translateX(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.illustration-icon {
  font-size: 48px;
}

.illustration-text {
  font-size: 20px;
  font-weight: 600;
  color: var(--text);
}

.features-section {
  padding: 80px 0;
  background: linear-gradient(180deg, transparent, #F8FAFC, transparent);
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.section-title {
  text-align: center;
  font-size: 36px;
  font-weight: 800;
  color: var(--text);
  margin-bottom: 48px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  max-width: 1000px;
  margin: 0 auto;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.feature-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 12px;
}

.feature-text {
  font-size: 14px;
  color: var(--muted);
  line-height: 1.6;
  margin: 0;
}

.complaints-section {
  padding: 40px 0;
}

@media (max-width: 968px) {
  .hero-section {
    grid-template-columns: 1fr;
    gap: 40px;
    padding: 40px 0;
  }
  
  .hero-title {
    font-size: 40px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>


