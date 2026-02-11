<template>
  <div class="register-page">
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
        <h1>JOIN SHIPPING DOCFLOW</h1>
        <p>Start your premium document journey</p>
      </div>
      
      <form @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <label for="companyName">Company Name (公司名称)</label>
          <div class="input-wrapper">
            <i class="fas fa-industry"></i>
            <input 
              v-model="form.companyName" 
              type="text" 
              id="companyName" 
              placeholder="e.g. Global Logistics Co." 
              required
            >
          </div>
        </div>

        <div class="form-group">
          <label for="companyAbbr">Shipline Code (航司四字码)</label>
          <div class="input-wrapper">
            <i class="fas fa-plane"></i>
            <input 
              v-model="form.companyAbbr" 
              type="text" 
              id="companyAbbr" 
              placeholder="e.g. MSKU (4 Letters)" 
              maxlength="4"
              required
            >
          </div>
          <small class="hint">用于生成提单编号的前四个字母</small>
        </div>
        
        <div class="form-group">
          <label for="password">Password (密码)</label>
          <div class="input-wrapper">
            <i class="fas fa-lock"></i>
            <input 
              v-model="form.password" 
              type="password" 
              id="password" 
              placeholder="Enter your password" 
              required
            >
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm Password (重复密码)</label>
          <div class="input-wrapper">
            <i class="fas fa-check-double"></i>
            <input 
              v-model="form.confirmPassword" 
              type="password" 
              id="confirmPassword" 
              placeholder="Repeat your password" 
              required
            >
          </div>
        </div>
        
        <button type="submit" :disabled="loading" class="login-btn">
          <span v-if="!loading">Create Account</span>
          <span v-else class="loader"></span>
        </button>
      </form>
      
      <div v-if="successMsg" class="success-msg">
        <i class="fas fa-check-circle"></i>
        {{ successMsg }}
      </div>

      <div v-if="error" class="error-msg">
        <i class="fas fa-exclamation-circle"></i>
        {{ error }}
      </div>
      
      <div class="login-footer">
        <p>Already have an account? <router-link to="/login">Login Now</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/request'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const successMsg = ref('')

const form = ref({
  companyName: '',
  companyAbbr: '',
  password: '',
  confirmPassword: ''
})

const handleRegister = async () => {
  if (form.value.password !== form.value.confirmPassword) {
    error.value = 'Passwords do not match (两次输入的密码不一致)'
    return
  }

  loading.value = true
  error.value = ''
  successMsg.value = ''
  
  try {
    const res = await api.post('/client-api/register', form.value)
    if (res.code === 200 || res.code === 0) {
      successMsg.value = res.msg || 'Registration successful!'
      // Redirect to login after a short delay
      setTimeout(() => {
        router.push('/login')
      }, 3000)
    } else {
      error.value = res.msg || 'Registration failed'
    }
  } catch (err) {
    error.value = err.message || 'Network error'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
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
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(59, 130, 246, 0.1));
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
  max-width: 480px;
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
  margin-bottom: 20px;
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

.hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-dim);
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
  margin-top: 24px;
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

.success-msg {
  margin-top: 16px;
  padding: 12px;
  background: #ecfdf5;
  border: 1px solid #d1fae5;
  border-radius: 8px;
  color: #10b981;
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
