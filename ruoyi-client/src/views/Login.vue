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
          <i class="fas fa-file-pdf"></i>
        </div>
        <h1>提单导出系统</h1>
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
        <p>Don't have an account? <a href="#">Request Access</a></p>
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
  background: #0f172a;
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
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3));
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
  padding: 40px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
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
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 10px 15px -3px rgba(99, 102, 241, 0.4);
}

.logo i {
  font-size: 32px;
  color: white;
}

h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(to right, #f8fafc, #cbd5e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-header p {
  color: #94a3b8;
  font-size: 14px;
}

.login-form .form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #cbd5e1;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrapper i {
  position: absolute;
  left: 16px;
  color: #64748b;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: white;
  font-family: inherit;
  font-size: 15px;
  transition: all 0.3s ease;
}

input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.08);
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.forgot-password {
  text-align: right;
  margin-bottom: 24px;
}

.forgot-password a {
  font-size: 13px;
  color: #6366f1;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-password a:hover {
  color: #8b5cf6;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(99, 102, 241, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.error-msg {
  margin-top: 16px;
  padding: 12px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  color: #f87171;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 14px;
  color: #94a3b8;
}

.login-footer a {
  color: #6366f1;
  text-decoration: none;
  font-weight: 500;
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
