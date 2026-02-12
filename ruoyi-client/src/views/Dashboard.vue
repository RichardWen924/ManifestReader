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
          <router-link to="/"><i class="fas fa-file-invoice"></i> {{ $t('sidebar.docGen') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/history' }">
          <router-link to="/history"><i class="fas fa-history"></i> {{ $t('sidebar.history') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/lab' }">
          <router-link to="/lab"><i class="fas fa-flask"></i> {{ $t('sidebar.lab') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/templates' }">
          <router-link to="/templates"><i class="fas fa-layer-group"></i> {{ $t('sidebar.templates') }}</router-link>
        </li>
        <li :class="{ active: $route.path === '/guide' }">
          <router-link to="/guide"><i class="fas fa-question-circle"></i> {{ $t('sidebar.guide') }}</router-link>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="upgrade-link" @click="router.push('/upgrade')" :title="$t('sidebar.upgrade')">
          <i class="fas fa-shopping-cart"></i>
          <span>{{ $t('sidebar.upgrade') }}</span>
        </div>
        <div class="user-profile" @click="router.push('/profile')">
          <div class="user-avatar">{{ userAbbr }}</div>
          <div class="user-info">
            <div class="name-row">
              <span class="name">{{ currentUser }}</span>
              <span v-if="isVip" class="vip-badge">VIP</span>
            </div>
            <span class="role">{{ isVip ? $t('profile.premium') : $t('profile.shipper') }}</span>
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <i class="fas fa-sign-out-alt"></i> {{ $t('sidebar.logout') }}
        </button>
      </div>
    </nav>
    
    <!-- Profile Edit Modal Removed (Moved to Profile page) -->
    
    <main class="main-content">
      <header class="content-header">
        <div class="header-text">
          <h1>{{ $t('dashboard.title') }}</h1>
          <p>{{ $t('dashboard.subtitle') }}</p>
        </div>
        <LanguageSwitcher />
      </header>
      
      <section class="upload-section card">
        <div class="upload-container">
          <div 
            class="drop-zone" 
            :class="{ dragging: isDragging, compact: files.length > 0 }"
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
            <h3>{{ $t('dashboard.uploadTitle') }}</h3>
            <p>{{ $t('dashboard.uploadDesc') }}</p>
          </div>
          
          <div v-if="files.length > 0" class="file-list-container">
            <div class="file-list">
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
            </div>
            <div class="actions">
              <button 
                @click="startAnalysis" 
                :disabled="isAnalyzing || files.every(f => f.status === 'success')"
                class="primary-btn"
              >
                <i class="fas fa-brain"></i> {{ $t('dashboard.startAnalysis') }}
              </button>
            </div>
          </div>
        </div>
      </section>
      
      <section v-if="results.length > 0" class="results-section">
        <div class="section-title">
          <h2>{{ $t('dashboard.analysisResults') }}</h2>
          <div class="actions">
             <button @click="saveAll" class="success-btn">
               <i class="fas fa-save"></i> {{ $t('dashboard.saveAll') }}
             </button>
          </div>
        </div>
        
        <div class="results-grid">
          <div v-for="(result, index) in results" :key="index" class="result-card card">
            <div class="result-header">
              <h3>{{ result.fileName }}</h3>
              <span class="badge">{{ $t('dashboard.analyzed') }}</span>
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
              <button @click="openEditModal(result, index)" class="outline-btn"><i class="fas fa-edit"></i> {{ $t('dashboard.reviewEdit') }}</button>
              <button @click="saveSingle(result, index)" class="primary-btn"><i class="fas fa-save"></i> {{ $t('dashboard.save') }}</button>
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


          <div class="modal-body-split">
            <!-- Left Pane: Edit Form -->
            <div class="left-pane custom-scrollbar">
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

            <!-- Right Pane: Live Preview -->
            <div class="right-pane preview-section">
              <div class="preview-header">
                <h3><i class="fas fa-eye"></i> Document Preview</h3>
                <button @click="handlePreview" :disabled="previewLoading" class="refresh-btn">
                  <i class="fas" :class="previewLoading ? 'fa-spinner fa-spin' : 'fa-sync-alt'"></i>
                  Refresh Preview
                </button>
              </div>
              <div class="preview-container">
                <div v-if="!previewUrl" class="preview-placeholder">
                  <i class="fas fa-file-pdf"></i>
                  <p>Click "Refresh Preview" to generate a preview</p>
                </div>
                <iframe v-else :src="previewUrl" class="preview-frame"></iframe>
              </div>
            </div>
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
      <!-- Upgrade Modal (Custom Quota Interception) -->
      <div v-if="isUpgradeModalOpen" class="modal-overlay" @click.self="isUpgradeModalOpen = false">
        <div class="modal-content upgrade-modal card">
          <div class="upgrade-header">
            <div class="premium-icon">
              <i class="fas fa-crown"></i>
            </div>
            <h2>额度已达上限</h2>
            <p>Quota Limit Reached</p>
          </div>
          <div class="upgrade-body">
            <div class="limit-status">
              <div class="status-item">
                <span class="label">当前使用</span>
                <span class="value">{{ recordsCount }}</span>
              </div>
              <div class="status-separator">/</div>
              <div class="status-item">
                <span class="label">免费额度</span>
                <span class="value">{{ QUOTA_LIMIT }}</span>
              </div>
            </div>
            <p class="upgrade-msg">您的非会员额度已用完。升级为 <strong>Premium Member</strong> 即可解锁无限生成额度，并使用所有高级模版。</p>
            <div class="premium-features">
              <div class="feature-item"><i class="fas fa-check-circle"></i> 无限次 AI 智能分析</div>
              <div class="feature-item"><i class="fas fa-check-circle"></i> 解锁所有专业贸易模版</div>
              <div class="feature-item"><i class="fas fa-check-circle"></i> 优先技术支持</div>
            </div>
          </div>
          <footer class="modal-footer upgrade-footer">
            <button @click="isUpgradeModalOpen = false" class="outline-btn">稍后再说</button>
            <button @click="router.push('/upgrade')" class="primary-btn upgrade-cta-btn">
              <i class="fas fa-rocket"></i> 立即升级
            </button>
          </footer>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
// @author Richard
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/request'
import { listTemplate, exportWithTemplate } from '../api/template'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'
import Swal from 'sweetalert2'

const router = useRouter()
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
const userAbbr = ref('Loading...')
const isVip = ref(false)
const isDragging = ref(false)
const fileInput = ref(null)
const files = ref([])
const isAnalyzing = ref(false)
const results = ref([])
const recordsCount = ref(0) // 记录用户已生成的提单数量
const QUOTA_LIMIT = 4 // 非会员限额
const isUpgradeModalOpen = ref(false) // 会员升级弹窗状态

const fetchUserData = async () => {
  try {
    const res = await api.get('/client-api/current-user')
    if (res.code === 200 || res.code === 0) {
      userAbbr.value = res.data.companyAbbr
      isVip.value = res.data.vipStatus === '1'
      recordsCount.value = res.data.dataCount || 0
    }
  } catch (err) {
    console.error('Failed to fetch user data:', err)
  }
}

// Modal States
const isModalOpen = ref(false)
const editingResult = ref(null)
const editingIndex = ref(-1)
const selectedTemplateId = ref('')
const exporting = ref(false)
const templateOptions = ref([])
const previewLoading = ref(false)
const previewUrl = ref('')

const fetchTemplateOptions = async () => {
  try {
    const res = await listTemplate({})
    if (res.code === 200 || res.code === 0) {
      let rows = res.rows || res.data || []
      if (!isVip.value && rows.length > 2) {
        templateOptions.value = rows.slice(0, 2)
      } else {
        templateOptions.value = rows
      }
    }
  } catch (err) {
    console.error('Failed to fetch templates:', err)
  }
}

const openEditModal = (result, index) => {
  editingResult.value = JSON.parse(JSON.stringify(result)) // Deep copy
  editingIndex.value = index
  isModalOpen.value = true
  previewUrl.value = ''
  // Optionally trigger initial preview
  setTimeout(() => handlePreview(), 500)
}

const closeModal = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
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
    fetchUserData() // 更新额度
    fetchUserData() // 更新额度
    Swal.fire({
      title: 'Success',
      text: 'Record saved successfully!',
      icon: 'success',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6'
    })
  } catch (err) {
    Swal.fire({
      title: 'Error',
      text: 'Save failed: ' + err.message,
      icon: 'error',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6'
    })
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
    
    // Success Notification
    Swal.fire({
      title: 'Export Successful',
      text: 'Your document is downloading...',
      icon: 'success',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6',
      timer: 3000,
      timerProgressBar: true
    })
  } catch (err) {
    Swal.fire({
      title: 'Export Failed',
      text: err.message || 'Unknown error',
      icon: 'error',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6'
    })
  } finally {
    exporting.value = false
  }
}

const handlePreview = async () => {
  previewLoading.value = true
  try {
    const dto = {
      businessData: editingResult.value.data,
      uuid: editingResult.value.uuid || ''
    }
    const response = await api.post('/client-api/export-pdf', dto, {
      responseType: 'blob'
    })
    
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
    }
    previewUrl.value = window.URL.createObjectURL(new Blob([response], { type: 'application/pdf' }))
  } catch (err) {
    console.error('Preview failed:', err)
  } finally {
    previewLoading.value = false
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

onMounted(async () => {
  await fetchUserData()
  await fetchTemplateOptions()
})

const checkQuota = () => {
  if (isVip.value) return true
  if (recordsCount.value >= QUOTA_LIMIT) {
    isUpgradeModalOpen.value = true
    return false
  }
  return true
}

const addFiles = (newFiles) => {
  if (!checkQuota()) return
  
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
  if (!checkQuota()) return
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
      Swal.fire({
        title: 'Analysis Failed',
        text: `Analysis failed for ${fileObj.name}: ${err.message}`,
        icon: 'error',
        background: '#1e293b',
        color: '#ffffff',
        confirmButtonColor: '#3b82f6'
      })
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
    fetchUserData() // 更新额度
    fetchUserData() // 更新额度
    Swal.fire({
      title: 'Success',
      text: 'Record saved successfully!',
      icon: 'success',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6'
    })
    // 如果文件列表里也有这个文件，标记为已保存或删除
  } catch (err) {
    Swal.fire({
      title: 'Error',
      text: 'Save failed: ' + err.message,
      icon: 'error',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6'
    })
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
    fetchUserData() // 更新额度
    fetchUserData() // 更新额度
    Swal.fire({
      title: 'Success',
      text: 'All records saved successfully!',
      icon: 'success',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6'
    })
  } catch (err) {
    Swal.fire({
      title: 'Error',
      text: 'Some records failed to save',
      icon: 'error',
      background: '#1e293b',
      color: '#ffffff',
      confirmButtonColor: '#3b82f6'
    })
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
  background: var(--bg-light);
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
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upgrade-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(37, 99, 235, 0.05));
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  color: #fbbf24;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.upgrade-link:hover {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.upgrade-link i {
  font-size: 16px;
  color: #f59e0b;
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

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info .name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.vip-badge {
  padding: 2px 6px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border-radius: 4px;
  color: white;
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 0.5px;
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

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 40px 48px;
  background: var(--bg-light);
  min-height: 100vh;
}

.content-header {
  margin-bottom: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text h1 {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 6px;
  letter-spacing: -0.5px;
}

.content-header p {
  color: var(--text-dim);
  font-size: 15px;
}

/* Card */
.card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  box-shadow: var(--shadow-sm);
  padding: 24px;
}

/* Upload Section */
.upload-section {
  max-width: 1000px;
  margin-bottom: 32px;
}

.upload-container {
  display: flex;
  gap: 24px;
}

.drop-zone {
  flex: 1;
  border: 2px dashed var(--border-color);
  border-radius: 16px;
  padding: 40px 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.drop-zone.compact {
  padding: 24px;
  min-height: 160px;
}

.drop-zone:hover, .drop-zone.dragging {
  border-color: var(--primary-color);
  background: #eff6ff;
}

.upload-icon {
  font-size: 40px;
  color: var(--primary-color);
  margin-bottom: 12px;
}

.drop-zone h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 6px;
}

.drop-zone p {
  color: var(--text-dim);
  font-size: 13px;
}

.file-list-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 4px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  font-size: 13px;
}

.file-name {
  flex: 1;
  font-weight: 500;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}

.file-status.pending { background: #f1f5f9; color: #64748b; }
.file-status.analyzing { background: #eff6ff; color: #3b82f6; }
.file-status.success { background: #ecfdf5; color: #10b981; }
.file-status.error { background: #fef2f2; color: #ef4444; }

.remove-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  cursor: pointer;
  padding: 4px;
  transition: all 0.2s;
}

.remove-btn:hover { color: #ef4444; }

.actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* Results Section */
.results-section {
  margin-top: 40px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title h2 {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-main);
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.result-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 8px;
  background: #ecfdf5;
  color: #10b981;
  border-radius: 6px;
}

.result-preview-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-item {
  display: flex;
  gap: 8px;
  font-size: 13px;
}

.preview-item .label {
  color: var(--text-dim);
  font-weight: 500;
  width: 100px;
}

.preview-item .value {
  color: var(--text-main);
  font-weight: 600;
}

.result-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.result-actions button {
  flex: 1;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}

.modal-content {
  width: 100%;
  max-width: 1100px;
  max-height: 90vh;
  background: white;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.profile-modal {
  max-width: 500px !important;
}

.upgrade-modal {
  max-width: 440px !important;
  text-align: center;
  padding: 40px !important;
  border: 1px solid rgba(251, 191, 36, 0.3) !important;
  background: linear-gradient(to bottom, #ffffff, #fffdfa) !important;
}

.upgrade-header {
  margin-bottom: 24px;
}

.premium-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  margin: 0 auto 16px;
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.2);
}

.upgrade-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}

.upgrade-header p {
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  margin: 4px 0 0;
  letter-spacing: 1px;
}

.limit-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
}

.status-item {
  display: flex;
  flex-direction: column;
}

.status-item .label {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 4px;
}

.status-item .value {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
}

.status-separator {
  font-size: 24px;
  font-weight: 300;
  color: #cbd5e1;
}

.upgrade-msg {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin-bottom: 24px;
}

.premium-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
  text-align: left;
  border-top: 1px solid #f1f5f9;
  padding-top: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

.feature-item i {
  color: #10b981;
}

.upgrade-footer {
  display: flex;
  gap: 12px;
  padding: 0 !important;
  border: none !important;
}

.upgrade-footer button {
  flex: 1;
}

.upgrade-cta-btn {
  background: linear-gradient(135deg, #fbbf24, #f59e0b) !important;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3) !important;
}

.upgrade-cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.4) !important;
}

.modal-header {
  padding: 24px 32px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-main);
  margin: 0;
}

.header-left p {
  color: var(--text-dim);
  margin: 4px 0 0 0;
  font-size: 14px;
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 20px;
  cursor: pointer;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.modal-footer {
  padding: 20px 32px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.footer-right {
  display: flex;
  gap: 12px;
}

/* Modal Customization */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  width: 90vw;
  max-width: 1300px;
  height: 85vh;
  display: flex;
  flex-direction: column;
  background: #1e293b;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  color: white;
}

.close-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  color: white;
  transform: rotate(90deg);
}

.modal-body-split {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
  overflow: hidden; /* Important for scrollbars */
}

.left-pane {
  flex: 1;
  overflow-y: auto;
  min-width: 500px;
}

.right-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 400px;
}

.preview-section {
  background: #0f172a;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.preview-header {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  color: white;
  margin: 0;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.preview-container {
  flex: 1;
  background: #0f172a;
  position: relative;
  display: flex;
  height: 100%;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #64748b;
  gap: 16px;
}

.preview-placeholder i {
  font-size: 48px;
  opacity: 0.5;
}

.preview-frame {
  width: 100%;
  height: 100%;
  border: none;
}

/* Modal Customization End */

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.form-section h3 {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.full-width {
  grid-column: span 1 / -1;
}

.form-group-custom label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-dim);
  margin-bottom: 8px;
}

.form-group-custom input, .form-group-custom textarea {
  width: 100%;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  color: var(--text-main);
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s;
}

.form-group-custom input:focus, .form-group-custom textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  background: white;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

/* Export Group */
.template-export-group {
  display: flex;
  gap: 8px;
}

.template-select {
  padding: 10px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  font-size: 14px;
  color: var(--text-main);
  background: white;
}

.export-btn {
  padding: 10px 20px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.export-btn:hover:not(:disabled) {
  background: #059669;
}

.export-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Buttons */
.primary-btn {
  padding: 10px 20px;
  background: var(--primary-color);
  border: none;
  border-radius: 10px;
  color: white;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.primary-btn:hover { background: #2563eb; transform: translateY(-1px); }

.outline-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid var(--border-color);
  color: var(--text-main);
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.outline-btn:hover { background: #f8fafc; border-color: #cbd5e1; }

.success-btn {
  padding: 10px 20px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.loader {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
