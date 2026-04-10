<template>
  <div class="guide-page">
    <div class="background-blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="guide-container">
      <header class="guide-header animate-fade-in">
        <div class="logo">
          <i class="fas fa-ship"></i>
        </div>
        <h1>欢迎使用 Shipping DocFlow</h1>
        <p>只需 4 个简单步骤，即可掌握 AI 驱动的单证生成</p>
      </header>

      <div class="steps-grid">
        <div class="step-card animate-slide-up" style="animation-delay: 0.1s">
          <div class="step-num">01</div>
          <div class="step-icon">
            <i class="fas fa-file-upload"></i>
          </div>
          <h3>上传与分析</h3>
          <p>拖拽上传您的物流单证。我们的 AI 将立即扫描并提取从提单详情到集装箱号的所有信息。</p>
        </div>

        <div class="step-card animate-slide-up" style="animation-delay: 0.2s">
          <div class="step-num">02</div>
          <div class="step-icon">
            <i class="fas fa-tasks"></i>
          </div>
          <h3>审核与校验</h3>
          <p>在智能编辑器中核对提取的数据。在进行最后阶段之前，确保每一个细节都完美无误。</p>
        </div>

        <div class="step-card animate-slide-up" style="animation-delay: 0.3s">
          <div class="step-num">03</div>
          <div class="step-icon">
            <i class="fas fa-file-word"></i>
          </div>
          <h3>选择模版</h3>
          <p>从专业的航运模版中进行选择。一键将提取的数据匹配到所需的模版格式中。</p>
        </div>

        <div class="step-card animate-slide-up" style="animation-delay: 0.4s">
          <div class="step-num">04</div>
          <div class="step-icon">
            <i class="fas fa-file-export"></i>
          </div>
          <h3>导出与保存</h3>
          <p>下载最终的 DOCX 文档，并将处理记录保存至历史库，以便日后查询和追溯。</p>
        </div>
      </div>

      <footer class="guide-footer animate-fade-in">
        <router-link to="/" class="get-started-btn">
          立即开始使用 <i class="fas fa-arrow-right"></i>
        </router-link>
        <p class="hint">您可以随时从侧边栏重新访问此教程。</p>
      </footer>
    </div>
  </div>
</template>

<script setup>
// @author Richard
// No complex logic needed for static guide
</script>

<style scoped>
.guide-page {
  min-height: 100vh;
  background-color: #0f172a; /* Explicit dark background */
  color: white;
  padding: 80px 24px;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
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
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(99, 102, 241, 0.15));
  filter: blur(100px);
  border-radius: 50%;
  animation: move 20s infinite alternate;
}

.blob-1 { top: -200px; left: -100px; }
.blob-2 { bottom: -200px; right: -100px; animation-duration: 25s; }
.blob-3 { top: 40%; left: 50%; width: 400px; height: 400px; opacity: 0.5; }

@keyframes move {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(100px, 50px) scale(1.1); }
}

.guide-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  width: 100%;
  text-align: center;
}

.guide-header {
  margin-bottom: 60px;
}

.logo {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.4);
}

.logo i {
  font-size: 32px;
  color: white;
}

h1 {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 16px;
  color: #ffffff; /* Explicit white */
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.guide-header p {
  font-size: 18px;
  color: #94a3b8; /* Light slate for readable text on dark */
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 60px;
}

.step-card {
  background: rgba(255, 255, 255, 0.05); /* Slightly brighter background for card */
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px 32px;
  text-align: left;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.step-card:hover {
  transform: translateY(-10px);
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(59, 130, 246, 0.4);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.step-num {
  position: absolute;
  top: 20px;
  right: 24px;
  font-size: 40px;
  font-weight: 900;
  color: #3b82f6;
  opacity: 0.2;
}

.step-icon {
  width: 56px;
  height: 56px;
  background: rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: #60a5fa;
  font-size: 24px;
}

.step-card h3 {
  font-size: 22px; /* Slightly larger */
  font-weight: 700;
  margin-bottom: 12px;
  color: #ffffff;
}

.step-card p {
  color: #cbd5e1; /* Brighter grey for readability */
  line-height: 1.6;
  font-size: 15px;
}

.guide-footer {
  margin-top: 40px;
}

.get-started-btn {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  padding: 18px 48px;
  border-radius: 100px;
  font-size: 18px;
  font-weight: 700;
  text-decoration: none;
  transition: all 0.3s ease;
  box-shadow: 0 10px 25px rgba(59, 130, 246, 0.3);
}

.get-started-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 15px 35px rgba(59, 130, 246, 0.4);
}

.hint {
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-dim);
}

/* Animations */
.animate-fade-in {
  animation: fadeIn 1s ease-out forwards;
}

.animate-slide-up {
  opacity: 0;
  animation: slideUp 0.8s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
