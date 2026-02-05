<template>
  <div class="history-page">
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
        <h1>My Records</h1>
        <p>View and manage your saved document declarations</p>
      </header>
      
      <section class="table-section card">
        <div class="table-filters">
              <input v-model="searchQuery" @input="fetchRecords" placeholder="Search by Booking No. or B/L No...">
        </div>
        
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Booking No.</th>
                <th>B/L No.</th>
                <th>Doc No.</th>
                <th>Vessel / Voyage</th>
                <th>Weight (KG)</th>
                <th>Volume (CBM)</th>
                <th>Package Info</th>
                <th>Created By</th>
                <th>Created At</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in records" :key="record.id">
                <td>{{ record.bookingNo }}</td>
                <td>{{ record.blNo }}</td>
                <td>{{ record.docNo }}</td>
                <td>{{ record.vesselVoyage }}</td>
                <td>{{ record.grossWeightKgs }}</td>
                <td>{{ record.measurementCbm }}</td>
                <td>{{ record.packageQuantity }} {{ record.packageUnit }}</td>
                <td>{{ record.createBy }}</td>
                <td class="dim">{{ record.createdAt }}</td>
                <td>
                  <div class="row-actions">
                    <button @click="editRecord(record)" class="icon-btn edit-btn" title="Edit">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button @click="exportPdf(record)" class="icon-btn export-btn" title="Export PDF">
                      <i class="fas fa-file-export"></i>
                    </button>
                    <button @click="handleDelete(record)" class="icon-btn delete-btn" title="Delete">
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="records.length === 0">
                <td colspan="11" class="empty-state">No records found.</td>
              </tr>
            </tbody>
          </table>
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
const records = ref([])
const searchQuery = ref('')

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

const fetchRecords = async () => {
  try {
    const res = await api.get('/client-api/list', {
      params: { 
        blNo: searchQuery.value,
        pageSize: 100 
      }
    })
    records.value = res.rows || []
  } catch (err) {
    console.error('Fetch failed:', err)
  }
}

const editRecord = (record) => {
  router.push(`/edit/${record.blNo}`)
}

const exportPdf = async (record) => {
  try {
    // 构造导出 DTO
    const dto = {
      businessData: record,
      uuid: '' // 历史记录通常不需要 UUID，直接使用记录数据
    }
    
    const response = await api.post('/client-api/export-pdf', dto, {
      responseType: 'blob'
    })
    
    // 下载文件
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `BL_${record.bookingNo}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    alert('Export failed: ' + err.message)
  }
}

const handleDelete = async (record) => {
  if (!confirm(`Are you sure you want to delete record ${record.bookingNo}?`)) {
    return
  }
  
  try {
    await api.post(`/client-api/remove/${record.id}`)
    alert('Record deleted successfully')
    fetchRecords()
  } catch (err) {
    alert('Delete failed: ' + err.message)
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.history-page {
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

.logo-icon {
  width: 40px;
  height: 40px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon i {
  color: #6366f1;
  font-size: 20px;
}

.logo-text h1 {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(to right, #f8fafc, #cbd5e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.logo-text span {
  font-size: 10px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
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

.table-filters {
  margin-bottom: 24px;
}

.search-box {
  position: relative;
  max-width: 400px;
}

.search-box i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
}

.search-box input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: white;
  font-family: inherit;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

th {
  padding: 16px;
  font-size: 14px;
  font-weight: 600;
  color: #94a3b8;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

td {
  padding: 16px;
  font-size: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.bold { font-weight: 600; color: #f8fafc; }
.dim { color: #64748b; }

.empty-state {
  text-align: center;
  padding: 40px;
  color: #64748b;
}

.row-actions {
  display: flex;
  gap: 12px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn:hover { background: rgba(99, 102, 241, 0.1); color: #6366f1; border-color: #6366f1; }
.export-btn:hover { background: rgba(16, 185, 129, 0.1); color: #10b981; border-color: #10b981; }
</style>
