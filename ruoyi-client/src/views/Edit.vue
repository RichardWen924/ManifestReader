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
        <li :class="{ active: $route.path === '/' }">
          <router-link to="/"><i class="fas fa-magic"></i> AI Analysis</router-link>
        </li>
        <li :class="{ active: $route.path === '/history' }">
          <router-link to="/history"><i class="fas fa-history"></i> My Records</router-link>
        </li>
        <li :class="{ active: $route.path === '/lab' }">
          <router-link to="/lab"><i class="fas fa-flask"></i> Template Lab</router-link>
        </li>
        <li :class="{ active: $route.path === '/templates' }">
          <router-link to="/templates"><i class="fas fa-layer-group"></i> Template Management</router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="user-info" @click="openProfileModal" title="Click to edit profile">
          <i class="fas fa-user-circle"></i>
          <span class="user-abbr">{{ userAbbr }}</span>
          <span class="user-code">{{ currentUser }}</span>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <i class="fas fa-sign-out-alt"></i> Logout
        </button>
      </div>
    </nav>
    
    <!-- Profile Edit Modal -->
    <div v-if="isProfileModalOpen" class="modal-overlay" @click.self="closeProfileModal">
      <div class="modal-content profile-modal card">
        <header class="modal-header">
          <div class="header-left">
            <h2>Edit Profile (修改个人信息)</h2>
            <p>Update your company name and password</p>
          </div>
          <button @click="closeProfileModal" class="close-btn"><i class="fas fa-times"></i></button>
        </header>

        <div class="modal-body-p">
          <form @submit.prevent="handleUpdateProfile" class="profile-form">
            <div class="form-group-c">
              <label>Company Name (公司名称)</label>
              <input v-model="profileForm.companyName" placeholder="Enter new company name">
            </div>

            <div class="form-group-c">
              <label>Shipline Code (航司四字码)</label>
              <input v-model="profileForm.companyAbbr" placeholder="e.g. MSKU" maxlength="4">
              <small class="hint">Must be 4 uppercase letters</small>
            </div>
            
            <div class="form-group-c">
              <label>New Password (新密码)</label>
              <input v-model="profileForm.password" type="password" placeholder="Leave blank to keep current password">
            </div>

            <div class="form-group-c">
              <label>Confirm Password (确认密码)</label>
              <input v-model="profileForm.confirmPassword" type="password" placeholder="Repeat new password">
            </div>
            
            <div v-if="profileError" class="error-msg-p">{{ profileError }}</div>
            <div v-if="profileSuccess" class="success-msg-p">{{ profileSuccess }}</div>
          </form>
        </div>

        <footer class="modal-footer-p">
          <button @click="closeProfileModal" class="btn-cancel">Cancel</button>
          <button @click="handleUpdateProfile" :disabled="profileLoading" class="btn-save">
            <span v-if="!profileLoading">Save Changes</span>
            <span v-else class="loader-p"></span>
          </button>
        </footer>
      </div>
    </div>
    
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
              <label>B/L NO.</label>
              <input v-model="form.blNo">
            </div>
            <div class="form-group">
              <label>Booking NO.</label>
              <input v-model="form.bookingNo">
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
            <div class="form-group">
              <label>Container No</label>
              <input v-model="form.containerNo">
            </div>
            <div class="form-group">
              <label>Seal No</label>
              <input v-model="form.sealNo">
            </div>
            <div class="form-group">
              <label>Container Weight</label>
              <input v-model="form.containerWeight" type="number" step="0.01">
            </div>
            <div class="form-group">
              <label>VGM Weight</label>
              <input v-model="form.vgmWeight" type="number" step="0.01">
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
const userAbbr = ref('Loading...')
const loading = ref(true)
const saving = ref(false)
const form = ref({})

// Profile Modal States
const isProfileModalOpen = ref(false)
const profileLoading = ref(false)
const profileError = ref('')
const profileSuccess = ref('')
const profileForm = ref({
  companyName: '',
  companyAbbr: '',
  password: '',
  confirmPassword: ''
})

const fetchUserData = async () => {
  try {
    const res = await api.get('/client-api/current-user')
    if (res.code === 200 || res.code === 0) {
      userAbbr.value = res.data.companyAbbr
      profileForm.value.companyName = res.data.companyName
      profileForm.value.companyAbbr = res.data.companyAbbr
    }
  } catch (err) {
    console.error('Failed to fetch user data:', err)
  }
}

const openProfileModal = () => {
  isProfileModalOpen.value = true
  profileError.value = ''
  profileSuccess.value = ''
  profileForm.value.password = ''
  profileForm.value.confirmPassword = ''
  fetchUserData()
}

const closeProfileModal = () => {
  isProfileModalOpen.value = false
}

const handleUpdateProfile = async () => {
  if (profileForm.value.password && profileForm.value.password !== profileForm.value.confirmPassword) {
    profileError.value = 'Passwords do not match'
    return
  }
  if (profileForm.value.companyAbbr && !/^[A-Z]{4}$/.test(profileForm.value.companyAbbr.toUpperCase())) {
    profileError.value = 'Shipline Code must be 4 uppercase letters'
    return
  }

  profileLoading.value = true
  profileError.value = ''
  profileSuccess.value = ''

  try {
    const res = await api.post('/client-api/update-profile', {
      companyName: profileForm.value.companyName,
      companyAbbr: profileForm.value.companyAbbr,
      password: profileForm.value.password
    })
    if (res.code === 200 || res.code === 0) {
      profileSuccess.value = 'Profile updated successfully!'
      fetchUserData()
      setTimeout(() => closeProfileModal(), 1500)
    } else {
      profileError.value = res.msg || 'Update failed'
    }
  } catch (err) {
    profileError.value = err.message || 'Network error'
  } finally {
    profileLoading.value = false
  }
}

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
    const blNo = route.params.blNo
    const res = await api.get('/client-api/list', {
      params: { blNo }
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
    router.push('/history')
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
    link.setAttribute('download', `BL_${form.value.blNo}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    alert('Export failed: ' + err.message)
  }
}

onMounted(async () => {
  fetchRecord()
  await fetchUserData()
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
  backdrop-filter: blur(10px);
  border-right: 1px solid var(--glass-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar-header {
  padding: 30px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.logo-text h1 {
  font-size: 18px;
  color: white;
  margin: 0;
}

.logo-text span {
  font-size: 11px;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-links {
  list-style: none;
  padding: 20px 15px;
  flex: 1;
}

.nav-links li {
  margin-bottom: 8px;
}

.nav-links a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: var(--text-dim);
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-links li.active a, .nav-links a:hover {
  background: var(--glass-bg);
  color: white;
  box-shadow: inset 0 0 0 1px var(--glass-border);
}

.nav-links li.active i, .nav-links a:hover i {
  color: white;
}

.nav-links i { width: 20px; text-align: center; }

.sidebar-footer {
  margin-top: auto;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 102, 241, 0.3);
}

.user-abbr {
  font-size: 16px;
  font-weight: 700;
  color: #10b981;
  letter-spacing: 1px;
}

.user-code {
  font-size: 12px;
  color: #64748b;
  font-family: monospace;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  color: #f87171;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-family: inherit;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 40px;
  background: #0f172a;
  min-height: 100vh;
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
  padding: 12px 32px; background: none; border: 1px solid rgba(255, 255, 255, 0.2); color: white; border-radius: 10px; text-decoration: none;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40px;
}

.modal-content.profile-modal {
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  background: #0f172a;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border-radius: 20px;
  padding: 0;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 { font-size: 20px; font-weight: 700; margin: 0; color: white; }
.modal-header p { color: #94a3b8; margin: 4px 0 0 0; font-size: 13px; }

.close-btn {
  background: none;
  border: none;
  color: #64748b;
  font-size: 20px;
  cursor: pointer;
}

.modal-body-p {
  padding: 24px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group-c {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group-c label {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
}

.form-group-c input {
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: white;
  width: 100%;
}

.hint { font-size: 11px; color: #64748b; }
.error-msg-p { color: #f87171; font-size: 13px; margin-top: 5px; }
.success-msg-p { color: #10b981; font-size: 13px; margin-top: 5px; }

.modal-footer-p {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-save {
  padding: 10px 20px;
  background: var(--primary-color);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.btn-cancel {
  padding: 10px 20px;
  background: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 8px;
  cursor: pointer;
}

.loader-p {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

.loading-state {
  text-align: center; padding: 100px; color: #94a3b8;
}

.loader {
  width: 48px; height: 48px; border: 4px solid rgba(255, 255, 255, 0.1); border-radius: 50%; border-top-color: #6366f1; animation: spin 1s linear infinite; display: inline-block; margin-bottom: 20px;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
