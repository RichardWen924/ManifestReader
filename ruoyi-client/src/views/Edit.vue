<template>
  <div class="edit-page">
    <nav class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">
          <i class="fas fa-ship"></i>
        </div>
        <div class="logo-text">
          <h1>提单导出系统</h1>
          <span>Shipping Document System</span>
        </div>
      </div>
      <ul class="nav-links">
        <li>
          <router-link to="/"><i class="fas fa-magic"></i> AI Analysis</router-link>
        </li>
        <li class="active">
          <router-link to="/history"><i class="fas fa-history"></i> My Records</router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="user-info">
          <i class="fas fa-user-circle"></i>
          <span>{{ currentUser }}</span>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <i class="fas fa-sign-out-alt"></i> Logout
        </button>
      </div>
    </nav>
    
    <main class="main-content">
      <header class="content-header">
        <div class="back-link" @click="$router.push('/history')">
          <i class="fas fa-arrow-left"></i> Back to History
        </div>
        <h1>Edit Record</h1>
        <p>Modify and export your document data</p>
      </header>
      
      <div v-if="loading" class="loading-state">
        <span class="loader"></span>
        <p>Loading record data...</p>
      </div>
      
      <section v-else class="edit-section card">
        <form @submit.prevent="handleSave" class="edit-form">
          <div class="form-grid">
            <div class="form-group">
              <label>Booking NO.</label>
              <input v-model="form.bookingNo" disabled class="disabled">
            </div>
            <div class="form-group">
              <label>B/L NO.</label>
              <input v-model="form.blNo">
            </div>
             <div class="form-group">
              <label>DOC NO.</label>
              <input v-model="form.docNo">
            </div>
            <div class="form-group">
               <label>Serial NO.</label>
               <input v-model="form.serialNo">
            </div>
            <div class="form-group">
               <label>Created By</label>
               <input v-model="form.createBy" disabled class="disabled">
            </div>
            
            <div class="form-group full-width">
              <label>Shipper</label>
              <textarea v-model="form.shipper" rows="4"></textarea>
            </div>
            
            <div class="form-group full-width">
              <label>Consignee</label>
              <textarea v-model="form.consignee" rows="4"></textarea>
            </div>
            
            <div class="form-group">
              <label>Vessel / Voyage</label>
              <input v-model="form.vesselVoyage">
            </div>
            <div class="form-group">
              <label>Port of Loading</label>
              <input v-model="form.portOfLoading">
            </div>
            <div class="form-group">
              <label>Port of Discharge</label>
              <input v-model="form.portOfDischarge">
            </div>
            <div class="form-group">
              <label>Package Quantity</label>
              <input v-model="form.packageQuantity" type="number">
            </div>
            <div class="form-group">
              <label>Package Unit</label>
              <input v-model="form.packageUnit">
            </div>
            <div class="form-group">
              <label>Gross Weight (KGS)</label>
              <input v-model="form.grossWeightKgs" type="number" step="0.01">
            </div>
            <div class="form-group">
              <label>Measurement (CBM)</label>
              <input v-model="form.measurementCbm" type="number" step="0.01">
            </div>
            <div class="form-group">
              <label>Place of Delivery</label>
              <input v-model="form.placeOfDelivery">
            </div>
          </div>
          
          <div class="form-actions">
             <button type="button" @click="handleExport" class="outline-btn">
               <i class="fas fa-file-export"></i> Export PDF
             </button>
             <button type="submit" :disabled="saving" class="primary-btn">
               <i class="fas fa-save"></i> {{ saving ? 'Saving...' : 'Save Changes' }}
             </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/request'

const route = useRoute()
const router = useRouter()
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
const loading = ref(true)
const saving = ref(false)
const form = ref({})

const handleLogout = async () => {
  try {
    await api.post('/client-api/logout')
    localStorage.removeItem('client_user')
    router.push('/login')
  } catch (err) {
    localStorage.removeItem('client_user')
    router.push('/login')
  }
}

const fetchRecord = async () => {
  loading.value = true
  try {
    const bookingNo = route.params.bookingNo
    const res = await api.get('/client-api/list', {
      params: { bookingNo }
    })
    if (res.rows && res.rows.length > 0) {
      form.value = res.rows[0]
    } else {
      alert('Record not found')
      router.push('/history')
    }
  } catch (err) {
    console.error('Fetch failed:', err)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await api.post('/client-api/update', form.value)
    alert('Record updated successfully')
  } catch (err) {
    alert('Update failed: ' + err.message)
  } finally {
    saving.value = false
  }
}

const handleExport = async () => {
  try {
    const dto = {
      businessData: form.value,
      uuid: ''
    }
    
    const response = await api.post('/client-api/export-pdf', dto, {
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `BL_${form.value.bookingNo}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    alert('Export failed: ' + err.message)
  }
}

onMounted(() => {
  fetchRecord()
})
</script>

<style scoped>
.edit-page {
  display: flex;
  min-height: 100vh;
}

/* Sidebar */
.sidebar {
  width: 260px;
  background: rgba(15, 23, 42, 0.95);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.sidebar-header i { font-size: 24px; color: #6366f1; }
.logo-text h1 { font-size: 20px; font-weight: 700; background: linear-gradient(to right, #f8fafc, #cbd5e1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.logo-text span { font-size: 10px; color: #64748b; text-transform: uppercase; letter-spacing: 1px; }
.logo-icon { width: 40px; height: 40px; background: rgba(99, 102, 241, 0.1); border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.logo-icon i { color: #6366f1; font-size: 20px; }

.nav-links { list-style: none; flex-grow: 1; }
.nav-links li { margin-bottom: 8px; }
.nav-links a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #94a3b8;
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.2s;
}

.nav-links li.active a { background: rgba(99, 102, 241, 0.1); color: white; }

.sidebar-footer {
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.user-info { display: flex; align-items: center; gap: 10px; color: #cbd5e1; margin-bottom: 16px; }

.logout-btn {
  width: 100%; padding: 10px; background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 8px; color: #f87171; cursor: pointer;
}

/* Main Content */
.main-content {
  flex-grow: 1; padding: 40px; background: #0f172a; overflow-y: auto;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  cursor: pointer;
  margin-bottom: 16px;
  font-size: 14px;
}

.back-link:hover { color: #6366f1; }

.edit-section {
  max-width: 900px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.full-width { grid-column: span 2; }

.form-group label {
  display: block; font-size: 14px; font-weight: 500; margin-bottom: 8px; color: #94a3b8;
}

input, textarea {
  width: 100%; padding: 12px; background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 10px; color: white; font-family: inherit; transition: all 0.3s;
}

input:focus, textarea:focus {
  outline: none; border-color: #6366f1; box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

input.disabled { opacity: 0.5; cursor: not-allowed; }

.form-actions {
  margin-top: 40px; display: flex; justify-content: flex-end; gap: 16px; padding-top: 24px; border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.primary-btn {
  padding: 12px 32px; background: #6366f1; color: white; border: none; border-radius: 10px; font-weight: 600; cursor: pointer;
}

.outline-btn {
  padding: 12px 32px; background: none; border: 1px solid rgba(255, 255, 255, 0.2); color: white; border-radius: 10px; text-decoration: none; display: flex; align-items: center; gap: 8px; cursor: pointer;
}

.loading-state {
  text-align: center; padding: 100px; color: #94a3b8;
}

.loader {
  width: 48px; height: 48px; border: 4px solid rgba(255, 255, 255, 0.1); border-radius: 50%; border-top-color: #6366f1; animation: spin 1s linear infinite; display: inline-block; margin-bottom: 20px;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
