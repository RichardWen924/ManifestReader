<template>
  <div id="app-container">
    <router-view></router-view>
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700&display=swap');

:root {
  --primary-color: #3b82f6;
  --primary-gradient: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  --secondary-color: #6366f1;
  --bg-light: #f8fafc;
  --sidebar-bg: #ffffff;
  --card-bg: #ffffff;
  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(0, 0, 0, 0.05);
  --border-color: #e2e8f0;
  --text-main: #0f172a;
  --text-dim: #64748b;
  --text-white: #ffffff;
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Outfit', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background-color: var(--bg-light);
  color: var(--text-main);
  overflow-x: hidden;
  min-height: 100vh;
}

#app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Scrollbar styling */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: var(--bg-light);
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
