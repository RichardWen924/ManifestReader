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
          <i class="fas fa-building"></i>
        </div>
        <h1>加入提单导出系统</h1>
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
          <label for="companyAbbr">Airline Code (航司四字码)</label>
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
  password: ''
})

const handleRegister = async () => {
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
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(99, 102, 241, 0.2));
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
  background: linear-gradient(135deg, #10b981, #6366f1);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.4);
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
  border-color: #10b981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
}

.hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #10b981, #6366f1);
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
  margin-top: 24px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.4);
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

.success-msg {
  margin-top: 16px;
  padding: 12px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 8px;
  color: #34d399;
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
  color: #10b981;
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
