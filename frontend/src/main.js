import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './assets/main.css'

// 创建根实例，入口组件为 App.vue（内部只有一个 <RouterView />）
const app = createApp(App)

// 全局注册 Element Plus 图标（模板里可直接用 <el-icon><Search /></el-icon>）
// 逐个注册后，模板中无需 import 即可使用任意图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
// locale: zhCn 让分页、日期、空数据等内置文案显示为中文
app.use(ElementPlus, { locale: zhCn })

// 挂载到 index.html 中的 <div id="app"></div>
app.mount('#app')