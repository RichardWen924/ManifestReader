<template>
  <div class="login-page">
    <div class="background-blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
    
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <i class="fas fa-ship"></i>
        </div>
        <h1>SHIPPING DOCFLOW</h1>
        <p>Premium Document Management</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Username</label>
          <div class="input-wrapper">
            <i class="fas fa-user"></i>
            <input 
              v-model="username" 
              type="text" 
              id="username" 
              placeholder="Enter your username" 
              required
            >
          </div>
        </div>
        
        <div class="form-group">
          <label for="password">Password</label>
          <div class="input-wrapper">
            <i class="fas fa-lock"></i>
            <input 
              v-model="password" 
              type="password" 
              id="password" 
              placeholder="Enter your password" 
              required
            >
          </div>
        </div>
        
        <div class="forgot-password">
          <a href="#">Forgot password?</a>
        </div>
        
        <button type="submit" :disabled="loading" class="login-btn">
          <span v-if="!loading">Login</span>
          <span v-else class="loader"></span>
        </button>
      </form>
      
      <div v-if="error" class="error-msg">
        <i class="fas fa-exclamation-circle"></i>
        {{ error }}
      </div>
      
      <div class="login-footer">
        <p>Don't have an account? <router-link to="/register">Register Now</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/request'

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const router = useRouter()

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await api.post('/client-api/login', {
      username: username.value,
      password: password.value
    })
    if (res.code === 200 || res.code === 0) {
      localStorage.setItem('client_user', res.data)
      router.push('/')
    }
  } catch (err) {
    error.value = err.message || 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--bg-light);
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
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(99, 102, 241, 0.1));
  filter: blur(80px);
  border-radius: 50%;
  animation: move 20s infinite alternate;
}

.blob-1 { top: -100px; left: -100px; }
.blob-2 { bottom: -150px; right: -100px; animation-duration: 25s; animation-direction: reverse; }
.blob-3 { top: 20%; right: 10%; width: 300px; height: 300px; animation-duration: 15s; }

@keyframes move {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(100px, 50px) scale(1.1); }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 48px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 56px;
  height: 56px;
  background: var(--primary-gradient);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.logo i {
  font-size: 28px;
  color: white;
}

h1 {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--text-main);
}

.login-header p {
  color: var(--text-dim);
  font-size: 15px;
}

.login-form .form-group {
  margin-bottom: 24px;
}

label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--text-dim);
  text-transform: uppercase;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrapper i {
  position: absolute;
  left: 14px;
  color: var(--text-dim);
  font-size: 14px;
}

input {
  width: 100%;
  padding: 12px 14px 12px 42px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-main);
  font-family: inherit;
  font-size: 15px;
  transition: all 0.2s;
}

input:focus {
  outline: none;
  background: white;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.forgot-password {
  text-align: right;
  margin-bottom: 28px;
}

.forgot-password a {
  font-size: 13px;
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: var(--primary-gradient);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none !important;
}

.error-msg {
  margin-top: 16px;
  padding: 12px;
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  color: #ef4444;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 14px;
  color: var(--text-dim);
}

.login-footer a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 700;
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
