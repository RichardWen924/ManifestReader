<template>
  <div class="upgrade-page">
    <div class="background-blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="upgrade-container">
      <header class="upgrade-header animate-fade-in">
        <div class="logo">
          <i class="fas fa-crown"></i>
        </div>
        <h1>Elevate Your Workflow</h1>
        <p>Unlock unrestricted AI power and professional features</p>
      </header>

      <div class="plans-grid">
        <!-- Free Plan -->
        <div class="plan-card animate-slide-up" style="animation-delay: 0.1s">
          <div class="plan-badge">CURRENT</div>
          <h2 class="plan-name">Basic</h2>
          <div class="plan-price">$0<span>/mo</span></div>
          <ul class="plan-features">
            <li><i class="fas fa-check"></i> 4 Document Generations</li>
            <li><i class="fas fa-check"></i> 2 Standard Templates</li>
            <li><i class="fas fa-check"></i> AI Extraction Support</li>
            <li class="restricted"><i class="fas fa-times"></i> Priority API Access</li>
            <li class="restricted"><i class="fas fa-times"></i> Custom Branding</li>
          </ul>
          <button class="plan-btn disabled" disabled>Active Plan</button>
        </div>

        <!-- Pro Plan -->
        <div class="plan-card pro animate-slide-up" style="animation-delay: 0.2s">
          <div class="plan-badge featured">MOST POPULAR</div>
          <h2 class="plan-name">VIP Pro</h2>
          <div class="plan-price">$29<span>/mo</span></div>
          <ul class="plan-features">
            <li><i class="fas fa-check"></i> Unlimited Generations</li>
            <li><i class="fas fa-check"></i> All Professional Templates</li>
            <li><i class="fas fa-check"></i> Priority AI Processing</li>
            <li><i class="fas fa-check"></i> Advanced PDF Tools</li>
            <li><i class="fas fa-check"></i> 24/7 Dedicated Support</li>
          </ul>
          <button @click="handleUpgrade" :disabled="loading" class="plan-btn primary">
            <span v-if="!loading">Upgrade Now</span>
            <span v-else class="loader"></span>
          </button>
        </div>
      </div>

      <div class="back-link animate-fade-in">
        <router-link to="/"><i class="fas fa-arrow-left"></i> Back to Dashboard</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
// @author Richard
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/request'
import Swal from 'sweetalert2'

const loading = ref(false)
const router = useRouter()

const handleUpgrade = async () => {
  const result = await Swal.fire({
    title: 'Confirm Upgrade?',
    text: "You are about to upgrade to VIP Pro. This will unlock all features instantly.",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#3b82f6',
    cancelButtonColor: '#64748b',
    confirmButtonText: 'Yes, Upgrade!',
    background: '#1e293b',
    color: '#ffffff'
  })

  if (result.isConfirmed) {
    loading.value = true
    try {
      const res = await api.post('/client-api/upgrade')
      if (res.code === 200 || res.code === 0) {
        await Swal.fire({
          title: 'Success!',
          text: res.msg || 'Welcome to VIP Pro!',
          icon: 'success',
          background: '#1e293b',
          color: '#ffffff'
        })
        router.push('/')
      }
    } catch (err) {
      Swal.fire({
        title: 'Error',
        text: err.message || 'Upgrade failed',
        icon: 'error',
        background: '#1e293b',
        color: '#ffffff'
      })
    } finally {
      loading.value = false
    }
  }
}
</script>

<style scoped>
.upgrade-page {
  min-height: 100vh;
  background-color: #0f172a;
  color: white;
  padding: 80px 24px;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.background-blobs {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.blob {
  position: absolute;
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(147, 51, 234, 0.15));
  filter: blur(100px);
  border-radius: 50%;
  animation: float 20s infinite alternate;
}

.blob-1 { top: -200px; left: -100px; }
.blob-2 { bottom: -200px; right: -100px; animation-delay: -5s; }
.blob-3 { top: 40%; left: 30%; width: 400px; height: 400px; background: rgba(59, 130, 246, 0.1); }

@keyframes float {
  from { transform: translate(0, 0) rotate(0deg); }
  to { transform: translate(100px, 50px) rotate(10deg); }
}

.upgrade-container {
  width: 100%;
  max-width: 1000px;
  position: relative;
  z-index: 1;
}

.upgrade-header {
  text-align: center;
  margin-bottom: 60px;
}

.logo {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 10px 20px rgba(245, 158, 11, 0.3);
}

.logo i {
  font-size: 32px;
  color: white;
}

h1 {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
  background: linear-gradient(to right, #fff, #94a3b8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.upgrade-header p {
  font-size: 18px;
  color: #94a3b8;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 32px;
  margin-bottom: 40px;
}

.plan-card {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 32px;
  padding: 48px;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.plan-card:hover {
  transform: translateY(-10px);
  border-color: rgba(255, 255, 255, 0.2);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.plan-card.pro {
  background: rgba(30, 41, 59, 0.4);
  border: 2px solid #3b82f6;
  box-shadow: 0 0 40px rgba(59, 130, 246, 0.1);
}

.plan-badge {
  position: absolute;
  top: 24px;
  right: 24px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 100px;
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
}

.plan-badge.featured {
  background: #3b82f6;
  color: white;
}

.plan-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
}

.plan-price {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 32px;
}

.plan-price span {
  font-size: 16px;
  font-weight: 400;
  color: #94a3b8;
}

.plan-features {
  list-style: none;
  padding: 0;
  margin: 0 0 40px 0;
}

.plan-features li {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 15px;
  color: #cbd5e1;
}

.plan-features li i {
  color: #10b981;
}

.plan-features li.restricted {
  color: #64748b;
}

.plan-features li.restricted i {
  color: #ef4444;
}

.plan-btn {
  width: 100%;
  padding: 16px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.plan-btn.disabled {
  background: rgba(255, 255, 255, 0.05);
  color: #64748b;
  cursor: not-allowed;
}

.plan-btn.primary {
  background: #3b82f6;
  color: white;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.2);
}

.plan-btn.primary:hover {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.3);
}

.back-link {
  text-align: center;
}

.back-link a {
  color: #94a3b8;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.back-link a:hover {
  color: white;
}

/* Animations */
.animate-fade-in {
  animation: fadeIn 1s ease-out;
}

.animate-slide-up {
  opacity: 0;
  animation: slideUp 0.8s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

.loader {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
