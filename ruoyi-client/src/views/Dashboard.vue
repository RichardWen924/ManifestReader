<template>
  <div class="dashboard-page">
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

        <div class="modal-body">
          <form @submit.prevent="handleUpdateProfile" class="profile-form">
            <div class="form-group-custom">
              <label>Company Name (公司名称)</label>
              <input v-model="profileForm.companyName" placeholder="Enter new company name">
            </div>

            <div class="form-group-custom">
              <label>Shipline Code (航司四字码)</label>
              <input v-model="profileForm.companyAbbr" placeholder="e.g. MSKU" maxlength="4">
              <small class="hint">Must be 4 uppercase letters</small>
            </div>
            
            <div class="form-group-custom">
              <label>New Password (新密码)</label>
              <input v-model="profileForm.password" type="password" placeholder="Leave blank to keep current password">
            </div>

            <div class="form-group-custom">
              <label>Confirm Password (确认密码)</label>
              <input v-model="profileForm.confirmPassword" type="password" placeholder="Repeat new password">
            </div>
            
            <div v-if="profileError" class="error-msg">{{ profileError }}</div>
            <div v-if="profileSuccess" class="success-msg">{{ profileSuccess }}</div>
          </form>
        </div>

        <footer class="modal-footer">
          <button @click="closeProfileModal" class="outline-btn">Cancel</button>
          <button @click="handleUpdateProfile" :disabled="profileLoading" class="primary-btn">
            <span v-if="!profileLoading">Save Changes</span>
            <span v-else class="loader"></span>
          </button>
        </footer>
      </div>
    </div>
    
    <main class="main-content">
      <header class="content-header">
        <h1>AI Intelligent Analysis</h1>
        <p>Upload your document files for instant data extraction</p>
      </header>
      
      <section class="upload-section card">
        <div 
          class="drop-zone" 
          :class="{ dragging: isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
          @click="$refs.fileInput.click()"
        >
          <input 
            type="file" 
            ref="fileInput" 
            multiple 
            @change="handleFileChange" 
            hidden
          >
          <i class="fas fa-cloud-upload-alt upload-icon"></i>
          <h3>Click or drag files to upload</h3>
          <p>Support for PDF, Images, Word documents</p>
        </div>
        
        <div v-if="files.length > 0" class="file-list">
          <div v-for="(file, index) in files" :key="index" class="file-item">
            <i class="fas fa-file-alt"></i>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-status" :class="file.status">
              {{ 
                file.status === 'pending' ? 'Pending' : 
                (file.status === 'analyzing' ? 'Analyzing...' : 
                (file.status === 'error' ? 'Error' : 'Success')) 
              }}
            </span>
            <button @click.stop="removeFile(index)" class="remove-btn">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="actions">
            <button 
              @click="startAnalysis" 
              :disabled="isAnalyzing || files.every(f => f.status === 'success')"
              class="primary-btn"
            >
              <i class="fas fa-brain"></i> Start AI Analysis
            </button>
          </div>
        </div>
      </section>
      
      <section v-if="results.length > 0" class="results-section">
        <div class="section-title">
          <h2>Analysis Results</h2>
          <div class="actions">
             <button @click="saveAll" class="success-btn">
               <i class="fas fa-save"></i> Save All to Records
             </button>
          </div>
        </div>
        
        <div class="results-grid">
          <div v-for="(result, index) in results" :key="index" class="result-card card">
            <div class="result-header">
              <h3>{{ result.fileName }}</h3>
              <span class="badge">Analyzed</span>
            </div>
            <div class="result-preview-info">
              <div class="preview-item">
                <span class="label">B/L No:</span>
                <span class="value">{{ result.data.blNo || '-' }}</span>
              </div>
              <div class="preview-item">
                <span class="label">Booking No:</span>
                <span class="value">{{ result.data.bookingNo || '-' }}</span>
              </div>
              <div class="preview-item">
                <span class="label">Vessel/Voyage:</span>
                <span class="value">{{ result.data.vesselVoyage || '-' }}</span>
              </div>
            </div>
            <div class="result-actions">
              <button @click="openEditModal(result, index)" class="outline-btn"><i class="fas fa-edit"></i> Review & Edit</button>
              <button @click="saveSingle(result, index)" class="primary-btn"><i class="fas fa-save"></i> Save</button>
            </div>
          </div>
        </div>
      </section>

      <!-- Edit Modal -->
      <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
        <div class="modal-content card">
          <header class="modal-header">
            <div class="header-left">
              <h2>Review Extraction Result</h2>
              <p>{{ editingResult?.fileName }}</p>
            </div>
            <button @click="closeModal" class="close-btn"><i class="fas fa-times"></i></button>
          </header>

          <div class="modal-body custom-scrollbar">
            <form @submit.prevent="saveFromModal" class="modal-form">
              <!-- Basic Info Group -->
              <div class="form-section">
                <h3><i class="fas fa-info-circle"></i> Basic Information</h3>
                <div class="grid-3">
                  <div class="form-group-custom">
                    <label>B/L NO.</label>
                    <input v-model="editingResult.data.blNo" placeholder="B/L NO.">
                  </div>
                  <div class="form-group-custom">
                    <label>Booking NO.</label>
                    <input v-model="editingResult.data.bookingNo" placeholder="Booking NO.">
                  </div>
                  <div class="form-group-custom">
                    <label>Serial NO.</label>
                    <input v-model="editingResult.data.serialNo" placeholder="Serial NO.">
                  </div>
                </div>
              </div>

              <!-- Parties Group -->
              <div class="form-section">
                <h3><i class="fas fa-users"></i> Parties</h3>
                <div class="grid-2">
                  <div class="form-group-custom">
                    <label>Shipper</label>
                    <textarea v-model="editingResult.data.shipper" rows="3" placeholder="Shipper info"></textarea>
                  </div>
                  <div class="form-group-custom">
                    <label>Consignee</label>
                    <textarea v-model="editingResult.data.consignee" rows="3" placeholder="Consignee info"></textarea>
                  </div>
                  <div class="form-group-custom">
                    <label>Notify Party</label>
                    <textarea v-model="editingResult.data.notifyParty" rows="3" placeholder="Notify party info"></textarea>
                  </div>
                  <div class="form-group-custom">
                    <label>Carrier Agent</label>
                    <textarea v-model="editingResult.data.carrierAgent" rows="3" placeholder="Carrier agent info"></textarea>
                  </div>
                  <div class="form-group-custom full-width">
                    <label>Delivery Agent</label>
                    <textarea v-model="editingResult.data.deliveryAgent" rows="2" placeholder="Delivery agent info"></textarea>
                  </div>
                </div>
              </div>

              <!-- Route Group -->
              <div class="form-section">
                <h3><i class="fas fa-route"></i> Route Information</h3>
                <div class="grid-3">
                  <div class="form-group-custom">
                    <label>Vessel/Voyage</label>
                    <input v-model="editingResult.data.vesselVoyage" placeholder="Vessel/Voyage">
                  </div>
                  <div class="form-group-custom">
                    <label>Vessel Name</label>
                    <input v-model="editingResult.data.vesselName" placeholder="Vessel Name">
                  </div>
                  <div class="form-group-custom">
                    <label>Voyage No</label>
                    <input v-model="editingResult.data.voyageNo" placeholder="Voyage No">
                  </div>
                  <div class="form-group-custom">
                    <label>Port of Loading</label>
                    <input v-model="editingResult.data.portOfLoading" placeholder="POL">
                  </div>
                  <div class="form-group-custom">
                    <label>Port of Discharge</label>
                    <input v-model="editingResult.data.portOfDischarge" placeholder="POD">
                  </div>
                  <div class="form-group-custom">
                    <label>Place of Receipt</label>
                    <input v-model="editingResult.data.placeOfReceipt" placeholder="Place of Receipt">
                  </div>
                  <div class="form-group-custom">
                    <label>Place of Delivery</label>
                    <input v-model="editingResult.data.placeOfDelivery" placeholder="Place of Delivery">
                  </div>
                </div>
              </div>

              <!-- Goods Group -->
              <div class="form-section">
                <h3><i class="fas fa-box"></i> Goods & Packages</h3>
                <div class="grid-2">
                  <div class="form-group-custom">
                    <label>Package Quantity</label>
                    <input v-model="editingResult.data.packageQuantity" type="text" placeholder="Quantity">
                  </div>
                  <div class="form-group-custom">
                    <label>Package Unit</label>
                    <input v-model="editingResult.data.packageUnit" placeholder="Unit">
                  </div>
                  <div class="form-group-custom full-width">
                    <label>Goods Description</label>
                    <textarea v-model="editingResult.data.goodsDescription" rows="4" placeholder="Goods description"></textarea>
                  </div>
                  <div class="form-group-custom full-width">
                    <label>Marks</label>
                    <textarea v-model="editingResult.data.marks" rows="2" placeholder="Marks"></textarea>
                  </div>
                </div>
              </div>

              <!-- Weight & Container Group -->
              <div class="form-section">
                <h3><i class="fas fa-weight-hanging"></i> Weight & Container</h3>
                <div class="grid-3">
                  <div class="form-group-custom">
                    <label>Gross Weight (KGS)</label>
                    <input v-model="editingResult.data.grossWeightKgs" type="number" step="0.01">
                  </div>
                  <div class="form-group-custom">
                    <label>Measurement (CBM)</label>
                    <input v-model="editingResult.data.measurementCbm" type="number" step="0.01">
                  </div>
                  <div class="form-group-custom">
                    <label>Container Weight</label>
                    <input v-model="editingResult.data.containerWeight" type="number" step="0.01">
                  </div>
                  <div class="form-group-custom">
                    <label>VGM Weight</label>
                    <input v-model="editingResult.data.vgmWeight" type="number" step="0.01">
                  </div>
                  <div class="form-group-custom">
                    <label>Container No</label>
                    <input v-model="editingResult.data.containerNo" placeholder="Container No">
                  </div>
                  <div class="form-group-custom">
                    <label>Seal No</label>
                    <input v-model="editingResult.data.sealNo" placeholder="Seal No">
                  </div>
                  <div class="form-group-custom full-width">
                    <label>Container/Seal Info</label>
                    <textarea v-model="editingResult.data.containerSealInfo" rows="2" placeholder="Container/Seal details"></textarea>
                  </div>
                </div>
              </div>

              <!-- Freight & Other Group -->
              <div class="form-section">
                <h3><i class="fas fa-file-invoice-dollar"></i> Freight & Issuance</h3>
                <div class="grid-3">
                  <div class="form-group-custom">
                    <label>Service Type</label>
                    <input v-model="editingResult.data.serviceType" placeholder="Service Type">
                  </div>
                  <div class="form-group-custom">
                    <label>Service Mode</label>
                    <input v-model="editingResult.data.serviceMode" placeholder="Service Mode">
                  </div>
                  <div class="form-group-custom">
                    <label>Revenue Tons</label>
                    <input v-model="editingResult.data.revenueTons" placeholder="Revenue Tons">
                  </div>
                  <div class="form-group-custom">
                    <label>Freight Term</label>
                    <input v-model="editingResult.data.freightTerm" placeholder="Freight Term">
                  </div>
                  <div class="form-group-custom">
                    <label>Freight Rate</label>
                    <input v-model="editingResult.data.freightRate" placeholder="Freight Rate">
                  </div>
                  <div class="form-group-custom">
                    <label>Prepaid Amount</label>
                    <input v-model="editingResult.data.prepaidAmount" placeholder="Prepaid">
                  </div>
                  <div class="form-group-custom">
                    <label>Collect Amount</label>
                    <input v-model="editingResult.data.collectAmount" placeholder="Collect">
                  </div>
                  <div class="form-group-custom">
                    <label>Payable At</label>
                    <input v-model="editingResult.data.payableAt" placeholder="Payable At">
                  </div>
                  <div class="form-group-custom">
                    <label>Original B/L Count</label>
                    <input v-model="editingResult.data.originalBlCount" placeholder="Count">
                  </div>
                  <div class="form-group-custom">
                    <label>Issue Place</label>
                    <input v-model="editingResult.data.issuePlace" placeholder="Issue Place">
                  </div>
                  <div class="form-group-custom full-width">
                    <label>Laden On Board</label>
                    <input v-model="editingResult.data.ladenOnBoard" placeholder="Laden On Board info">
                  </div>
                </div>
              </div>
            </form>
          </div>

          <footer class="modal-footer">
            <button @click="closeModal" class="outline-btn">Cancel</button>
            <button @click="saveFromModal" class="primary-btn"><i class="fas fa-check"></i> Save Record</button>
          </footer>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/request'

const router = useRouter()
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
const userAbbr = ref('Loading...')
const isDragging = ref(false)
const fileInput = ref(null)
const files = ref([])
const isAnalyzing = ref(false)
const results = ref([])

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
  // Re-fetch to ensure we have latest data
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
      fetchUserData() // Update sidebar
      setTimeout(() => {
        closeProfileModal()
      }, 1500)
    } else {
      profileError.value = res.msg || 'Update failed'
    }
  } catch (err) {
    profileError.value = err.message || 'Network error'
  } finally {
    profileLoading.value = false
  }
}

// Modal States
const isModalOpen = ref(false)
const editingResult = ref(null)
const editingIndex = ref(-1)

const openEditModal = (result, index) => {
  editingResult.value = JSON.parse(JSON.stringify(result)) // Deep copy
  editingIndex.value = index
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  editingResult.value = null
  editingIndex.value = -1
}

const saveFromModal = async () => {
  try {
    await api.post('/client-api/save', {
      filePath: editingResult.value.filePath,
      uuid: editingResult.value.uuid,
      editedData: editingResult.value.data
    })
    results.value.splice(editingIndex.value, 1)
    closeModal()
    alert('Record saved successfully!')
  } catch (err) {
    alert('Save failed: ' + err.message)
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

const handleFileChange = (e) => {
  const selectedFiles = Array.from(e.target.files)
  addFiles(selectedFiles)
}

const handleDrop = (e) => {
  isDragging.value = false
  const droppedFiles = Array.from(e.dataTransfer.files)
  addFiles(droppedFiles)
}

const addFiles = (newFiles) => {
  newFiles.forEach(file => {
    files.value.push({
      file,
      name: file.name,
      status: 'pending',
      path: ''
    })
  })
}

const removeFile = (index) => {
  files.value.splice(index, 1)
}

const startAnalysis = async () => {
  isAnalyzing.value = true
  
  for (const fileObj of files.value) {
    if (fileObj.status !== 'pending') continue
    
    fileObj.status = 'analyzing'
    try {
      // 1. 上传文件获取路径
      const formData = new FormData()
      formData.append('file', fileObj.file)
      
      const uploadRes = await api.post('/common/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      
      const filePath = uploadRes.fileName
      fileObj.path = filePath
      
      // 2. 调用 AI 分析
      const analyzeRes = await api.post(`/client-api/analyze?filePath=${encodeURIComponent(filePath)}`, null, {
        timeout: 300000 // AI 分析可能需要更长时间，设置为 5 分钟
      })
      
      fileObj.status = 'success'
      
      const newResult = {
        fileName: fileObj.name,
        filePath: filePath,
        data: analyzeRes.data.businessData,
        uuid: analyzeRes.data.uuid
      }
      results.value.push(newResult)
      
      // Auto open modal for the new result
      openEditModal(newResult, results.value.length - 1)
    } catch (err) {
      console.error(err)
      fileObj.status = 'error'
      alert(`Analysis failed for ${fileObj.name}: ${err.message}`)
    }
  }
  
  isAnalyzing.value = false
}

const saveSingle = async (result, index) => {
  try {
    await api.post('/client-api/save', {
      filePath: result.filePath,
      uuid: result.uuid,
      editedData: result.data
    })
    results.value.splice(index, 1)
    alert('Record saved successfully!')
    // 如果文件列表里也有这个文件，标记为已保存或删除
  } catch (err) {
    alert('Save failed: ' + err.message)
  }
}

const saveAll = async () => {
  const promises = results.value.map(r => api.post('/client-api/save', {
    filePath: r.filePath,
    uuid: r.uuid,
    editedData: r.data
  }))
  
  try {
    await Promise.all(promises)
    results.value = []
    alert('All records saved successfully!')
  } catch (err) {
    alert('Some records failed to save')
  }
}

const editResult = (result) => {
  // 暂时在本地编辑，或者跳转到编辑页？
  // 这里简化演示：我们在本地编辑
  const newBookingNo = prompt('Edit Booking No:', result.data.bookingNo)
  if (newBookingNo) result.data.bookingNo = newBookingNo
}

onMounted(async () => {
  // 检查 Session
  try {
    await api.get('/client-api/check-auth')
    await fetchUserData()
  } catch (err) {
    router.push('/login')
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-dark);
}

/* Results */
.results-section {
  margin-top: 40px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-body.editable-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
  width: 100%;
}

.form-group-custom {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group-custom.full-width {
  width: 100%;
}

.form-group-custom label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-group-custom input,
.form-group-custom textarea {
  width: 100%;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 10px 12px;
  color: white;
  font-size: 14px;
  transition: all 0.3s ease;
}

.form-group-custom input:focus,
.form-group-custom textarea:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.08);
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.creator-info .value {
  font-size: 13px;
  font-weight: 600;
  color: var(--secondary-color);
}

/* Preview Card Enhancements */
.result-preview-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: rgba(255, 255, 255, 0.02);
  padding: 12px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.preview-item .label { color: #94a3b8; }
.preview-item .value { color: #f8fafc; font-weight: 500; }

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

.modal-content {
  width: 100%;
  max-width: 1200px;
  max-height: 90vh;
  background: #0f172a;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 { font-size: 24px; font-weight: 700; margin: 0; }
.header-left p { color: #94a3b8; margin: 4px 0 0 0; font-size: 14px; }

.close-btn {
  background: none;
  border: none;
  color: #64748b;
  font-size: 20px;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover { color: white; }

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

.modal-footer {
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

/* Form Groups & Grid */
.form-section {
  margin-bottom: 40px;
}

.form-section h3 {
  font-size: 14px;
  font-weight: 600;
  color: #6366f1;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.02);
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
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

.profile-modal {
  max-width: 500px !important;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loader {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
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

.content-header {
  margin-bottom: 32px;
}

h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.content-header p {
  color: #94a3b8;
}

.card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 24px;
}

.drop-zone {
  border: 2px dashed rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 48px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.drop-zone.dragging, .drop-zone:hover {
  border-color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
}

.upload-icon {
  font-size: 48px;
  color: #6366f1;
  margin-bottom: 16px;
}

.file-list {
  margin-top: 24px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 8px;
}

.file-name {
  flex-grow: 1;
  margin-left: 12px;
  font-size: 14px;
}

.file-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  margin-right: 12px;
}

.file-status.pending { background: #334155; color: #94a3b8; }
.file-status.analyzing { background: #3b82f633; color: #3b82f6; }
.file-status.success { background: #10b98133; color: #10b981; }
.file-status.error { background: #ef444433; color: #ef4444; }

.remove-btn {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  transition: color 0.2s;
}

.remove-btn:hover { color: #f87171; }

.actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary-btn, .success-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.primary-btn { background: #6366f1; }
.primary-btn:hover { background: #4f46e5; transform: translateY(-1px); }

.success-btn { background: #10b981; }
.success-btn:hover { background: #059669; transform: translateY(-1px); }

.outline-btn {
  padding: 8px 16px;
  background: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 8px;
  cursor: pointer;
}

.outline-btn:hover { background: rgba(255, 255, 255, 0.05); }

/* Results */
.results-section {
  margin-top: 40px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.result-card .result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-card h3 {
  font-size: 16px;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge {
  font-size: 11px;
  padding: 2px 8px;
  background: #6366f133;
  color: #6366f1;
  border-radius: 100px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.data-item .label { color: #94a3b8; }
.data-item .value { font-weight: 500; }

.result-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.result-actions button {
  flex: 1;
}
</style>
