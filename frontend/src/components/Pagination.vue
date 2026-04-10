<template>
  <div class="pagination-container" v-if="total > 0">
    <div class="pagination-info">
      Total {{ total }} items
    </div>
    <div class="pagination-controls">
      <select v-model="pageSize" @change="handleSizeChange" class="page-size-select">
        <option v-for="size in pageSizes" :key="size" :value="size">{{ size }} / page</option>
      </select>
      
      <button 
        class="page-btn" 
        :disabled="currentPage <= 1"
        @click="handleCurrentChange(currentPage - 1)">
        <i class="fas fa-chevron-left"></i>
      </button>

      <span class="page-current">{{ currentPage }}</span>

      <button 
        class="page-btn" 
        :disabled="currentPage * pageSize >= total"
        @click="handleCurrentChange(currentPage + 1)">
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: {
    required: true,
    type: Number
  },
  page: {
    type: Number,
    default: 1
  },
  limit: {
    type: Number,
    default: 10
  },
  pageSizes: {
    type: Array,
    default() {
      return [10, 20, 30, 50]
    }
  }
})

const emit = defineEmits(['update:page', 'update:limit', 'pagination'])

const currentPage = computed({
  get() {
    return props.page
  },
  set(val) {
    emit('update:page', val)
  }
})

const pageSize = computed({
  get() {
    return props.limit
  },
  set(val) {
    emit('update:limit', val)
  }
})

function handleSizeChange(event) {
  emit('pagination', { page: currentPage.value, limit: Number(event.target.value) })
}

function handleCurrentChange(val) {
  emit('update:page', val)
  emit('pagination', { page: val, limit: pageSize.value })
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin-top: 10px;
}

.pagination-info {
  font-size: 13px;
  color: var(--text-dim, #64748b);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-size-select {
  padding: 6px 10px;
  border: 1px solid var(--border-color, #e2e8f0);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-main, #334155);
  cursor: pointer;
  background-color: white;
}

.page-size-select:focus {
  outline: none;
  border-color: var(--primary-color, #3b82f6);
}

.page-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color, #e2e8f0);
  background: white;
  border-radius: 6px;
  color: var(--text-main, #334155);
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--primary-color, #3b82f6);
  color: var(--primary-color, #3b82f6);
}

.page-btn:disabled {
  background: #f1f5f9;
  color: #cbd5e1;
  cursor: not-allowed;
}

.page-current {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main, #334155);
  min-width: 24px;
  text-align: center;
}
</style>
