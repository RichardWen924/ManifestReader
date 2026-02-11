<template>
  <div class="dashboard-page">
    <nav class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">
          <i class="fas fa-ship"></i>
        </div>
        <div class="logo-text">
          <h1>SHIPPING DOCFLOW</h1>
          <span>Shipping Document System</span>
        </div>
      </div>
      <ul class="nav-links">
        <li :class="{ active: $route.path === '/' }">
          <router-link to="/"><i class="fas fa-file-invoice"></i> 文档生成</router-link>
        </li>
        <li :class="{ active: $route.path === '/history' }">
          <router-link to="/history"><i class="fas fa-history"></i> 历史提单</router-link>
        </li>
        <li :class="{ active: $route.path === '/lab' }">
          <router-link to="/lab"><i class="fas fa-flask"></i> 模版生成</router-link>
        </li>
        <li :class="{ active: $route.path === '/templates' }">
          <router-link to="/templates"><i class="fas fa-layer-group"></i> 模版管理</router-link>
        </li>
        <li :class="{ active: $route.path === '/guide' }">
          <router-link to="/guide"><i class="fas fa-question-circle"></i> 使用教程</router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="user-profile" @click="openProfileModal">
          <div class="user-avatar">{{ userAbbr }}</div>
          <div class="user-info">
            <span class="name">{{ currentUser }}</span>
            <span class="role">Shipper</span>
          </div>
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
              <label>Old Password (原密码)</label>
              <input v-model="profileForm.oldPassword" type="password" placeholder="Required if changing password">
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
            <div class="footer-left">
              <div class="template-export-group">
                <select v-model="selectedTemplateId" class="template-select">
                  <option value="">-- Select Template --</option>
                  <option v-for="t in templateOptions" :key="t.templateId" :value="t.templateId">
                    {{ t.templateName }}
                  </option>
                </select>
                <button @click="handleExportWithTemplate" :disabled="!selectedTemplateId || exporting" class="export-btn">
                  <i class="fas fa-file-export"></i>
                  <span v-if="!exporting">Export Doc</span>
                  <span v-else class="loader"></span>
                </button>
              </div>
            </div>
            <div class="footer-right">
              <button @click="closeModal" class="outline-btn">Cancel</button>
              <button @click="saveFromModal" class="primary-btn"><i class="fas fa-check"></i> Save Record</button>
            </div>
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
import { listTemplate, exportWithTemplate } from '../api/template'

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
  oldPassword: '',
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
  profileForm.value.oldPassword = ''
  profileForm.value.password = ''
  profileForm.value.confirmPassword = ''
  // Re-fetch to ensure we have latest data
  fetchUserData()
}

const closeProfileModal = () => {
  isProfileModalOpen.value = false
}

const handleUpdateProfile = async () => {
  if (profileForm.value.password) {
      if (!profileForm.value.oldPassword) {
          alert('请输入原密码')
          return
      }
      if (profileForm.value.password !== profileForm.value.confirmPassword) {
        alert('两次输入的密码不一致')
        return
      }
  }

  if (profileForm.value.companyAbbr && !/^[A-Z]{4}$/.test(profileForm.value.companyAbbr.toUpperCase())) {
    alert('Shipline Code must be 4 uppercase letters')
    return
  }

  profileLoading.value = true
  profileError.value = ''
  profileSuccess.value = ''

  try {
    const res = await api.post('/client-api/update-profile', {
      companyName: profileForm.value.companyName,
      companyAbbr: profileForm.value.companyAbbr,
      oldPassword: profileForm.value.oldPassword,
      password: profileForm.value.password
    })
    if (res.code === 200 || res.code === 0) {
      profileSuccess.value = 'Profile updated successfully!'
      fetchUserData() // Update sidebar
      setTimeout(() => {
        closeProfileModal()
      }, 1500)
    } else {
      alert(res.msg || 'Update failed')
    }
  } catch (err) {
    alert(err.message || 'Network error')
  } finally {
    profileLoading.value = false
  }
}

// Modal States
const isModalOpen = ref(false)
const editingResult = ref(null)
const editingIndex = ref(-1)
const selectedTemplateId = ref('')
const exporting = ref(false)
const templateOptions = ref([])

const fetchTemplateOptions = async () => {
  try {
    const res = await listTemplate({})
    if (res.code === 200 || res.code === 0) {
      templateOptions.value = res.data
    }
  } catch (err) {
    console.error('Failed to fetch templates:', err)
  }
}

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

const handleExportWithTemplate = async () => {
  if (!selectedTemplateId.value) return
  exporting.value = true
  try {
    const blob = await exportWithTemplate(selectedTemplateId.value, editingResult.value.data)
    // trigger download
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    const bookingNo = editingResult.value.data.bookingNo || 'export'
    link.setAttribute('download', `BL_${bookingNo}.docx`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    alert('Export failed: ' + (err.message || 'Unknown error'))
  } finally {
    exporting.value = false
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
  fetchTemplateOptions()
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
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
.footer-left { display: flex; align-items: center; }
.footer-right { display: flex; gap: 12px; }

.template-export-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.template-select {
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  color: white;
  font-size: 13px;
  min-width: 180px;
  cursor: pointer;
}
.template-select option { background: #1e293b; color: white; }
.export-btn {
  padding: 8px 16px;
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 8px;
  color: #34d399;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}
.export-btn:hover:not(:disabled) {
  background: rgba(16, 185, 129, 0.25);
  border-color: #34d399;
}
.export-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
}

.sidebar-header {
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--primary-gradient);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
}

.logo-text h1 {
  font-size: 14px;
  color: var(--text-main);
  font-weight: 800;
  margin: 0;
  letter-spacing: 0.5px;
}

.logo-text span {
  font-size: 10px;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: block;
  margin-top: 2px;
}

.nav-links {
  list-style: none;
  padding: 8px 12px;
  flex: 1;
}

.nav-links li {
  margin-bottom: 4px;
}

.nav-links a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  color: var(--text-dim);
  text-decoration: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-links a i {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.nav-links li.active a {
  background: #eff6ff;
  color: var(--primary-color);
  font-weight: 600;
  box-shadow: none;
}

.nav-links li.active i {
  color: var(--primary-color);
}

.nav-links a:hover:not(.active a) {
  background: #f8fafc;
  color: var(--text-main);
}

.sidebar-footer {
  margin-top: auto;
  padding: 24px 16px;
  border-top: 1px solid var(--border-color);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
}

.user-profile:hover {
  background: #f1f5f9;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: #e2e8f0;
  color: var(--text-main);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.user-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.user-info .name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info .role {
  font-size: 11px;
  color: var(--text-dim);
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  color: #ef4444;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-btn:hover {
  background: #fee2e2;
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
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: var(--primary-color);
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
/* Main Content Area */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 48px 56px;
  background: var(--bg-light);
  min-height: 100vh;
}

.content-header {
  margin-bottom: 40px;
}

.content-header h1 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.content-header p {
  color: var(--text-dim);
  font-size: 16px;
}

/* Glass Card */
.card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  box-shadow: var(--shadow-md);
}

/* Upload Section */
.upload-section {
  padding: 32px;
  margin-bottom: 40px;
}

.drop-zone {
  border: 2px dashed var(--border-color);
  border-radius: 16px;
  padding: 48px;
  text-align: center;
  transition: all 0.2s;
  background: #f8fafc;
  cursor: pointer;
}

.drop-zone:hover, .drop-zone.dragging {
  border-color: var(--primary-color);
  background: #eff6ff;
}

.upload-icon {
  font-size: 48px;
  color: var(--primary-color);
  margin-bottom: 16px;
}

.upload-text h3 {
  font-size: 18px;
  color: var(--text-main);
  margin-bottom: 8px;
}

.upload-text p {
  color: var(--text-dim);
  font-size: 14px;
}

/* Results Section */
.results-section {
  margin-top: 48px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main);
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.result-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.result-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
}

.badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  text-transform: uppercase;
}

.badge.analyzing { background: #fef3c7; color: #d97706; }
.badge.completed { background: #dcfce7; color: #16a34a; }

.data-preview {
  display: grid;
  gap: 12px;
  margin-bottom: 24px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.data-item .label {
  color: var(--text-dim);
}

.data-item .value {
  color: var(--text-main);
  font-weight: 600;
}

.result-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.result-actions button {
  flex: 1;
}

/* Buttons */
.primary-btn {
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.primary-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.outline-btn {
  background: white;
  color: var(--text-main);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.outline-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

/* Modal Styling */
.modal-overlay {
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  border: none;
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
}

.modal-header {
  border-bottom: 1px solid var(--border-color);
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  color: var(--text-main);
  font-size: 20px;
  font-weight: 800;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: var(--text-dim);
  cursor: pointer;
}

.modal-body {
  padding: 32px;
}

.form-group-custom {
  margin-bottom: 20px;
}

.form-group-custom label {
  display: block;
  color: var(--text-dim);
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
}

.form-group-custom input {
  width: 100%;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  color: var(--text-main);
  border-radius: 10px;
  padding: 12px;
  font-size: 14px;
}

.form-group-custom input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.modal-footer {
  border-top: 1px solid var(--border-color);
  padding: 20px 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
