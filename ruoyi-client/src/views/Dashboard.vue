<template>
  <div class="dashboard-page">
    <nav class="sidebar">
      <div class="sidebar-header">
        <i class="fas fa-file-pdf"></i>
        <span>PDF System</span>
      </div>
      <ul class="nav-links">
        <li :class="{ active: $route.path === '/' }">
          <router-link to="/"><i class="fas fa-magic"></i> AI Analysis</router-link>
        </li>
        <li :class="{ active: $route.path === '/history' }">
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
            <div class="result-body">
              <div class="data-item">
                <span class="label">B/L NO.</span>
                <span class="value">{{ result.data.blNo || '-' }}</span>
              </div>
              <div class="data-item">
                <span class="label">Booking NO.</span>
                <span class="value">{{ result.data.bookingNo || '-' }}</span>
              </div>
               <div class="data-item">
                <span class="label">Vessel/Voyage</span>
                <span class="value">{{ result.data.vesselVoyage || '-' }}</span>
              </div>
              <div class="data-item">
                <span class="label">POL / POD</span>
                <span class="value">{{ result.data.portOfLoading }} / {{ result.data.portOfDischarge }}</span>
              </div>
              <div class="data-item">
                <span class="label">Created By</span>
                <span class="value">{{ currentUser }}</span>
              </div>
            </div>
            <div class="result-actions">
              <button @click="editResult(result)" class="outline-btn">Edit</button>
              <button @click="saveSingle(result, index)" class="primary-btn">Save</button>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/request'

const router = useRouter()
const currentUser = ref(localStorage.getItem('client_user') || 'Guest')
const isDragging = ref(false)
const fileInput = ref(null)
const files = ref([])
const isAnalyzing = ref(false)
const results = ref([])

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
      
      results.value.push({
        fileName: fileObj.name,
        filePath: filePath,
        data: analyzeRes.data.businessData,
        uuid: analyzeRes.data.uuid
      })
      
      fileObj.status = 'success'
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
  } catch (err) {
    router.push('/login')
  }
})
</script>

<style scoped>
.dashboard-page {
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

.sidebar-header i {
  font-size: 24px;
  color: #6366f1;
}

.sidebar-header span {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.nav-links {
  list-style: none;
  flex-grow: 1;
}

.nav-links li {
  margin-bottom: 8px;
}

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

.nav-links li.active a, .nav-links a:hover {
  background: rgba(99, 102, 241, 0.1);
  color: white;
}

.nav-links li.active i, .nav-links a:hover i {
  color: #6366f1;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #cbd5e1;
  margin-bottom: 16px;
  font-size: 14px;
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
  flex-grow: 1;
  padding: 40px;
  background: #0f172a;
  overflow-y: auto;
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
