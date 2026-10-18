<!--
  Publish.vue —— 发布 / 编辑商品页     路由：/publish（新建）  /edit/:id（编辑）
  =====================================================================
  一个组件同时承担两种用途：路由里有 :id 参数即"编辑"，否则为"发布"，
  靠 computed isEdit 区分，标题、按钮文案、提交接口都随之切换。
  布局：单张表单卡片，最大宽 640px 居中
  ┌───────────────────────────────────────────────┐
  │  标题「发布商品 / 编辑商品」                    │
  │  标题     [ 单行输入，64 字上限，带字数统计 ]   │
  │  分类     [ 下拉选择，选项来自分类接口 ]        │
  │  价格     [ 数字输入，≥0，两位小数 ]           │
  │  描述     [ 多行文本域，4 行，1000 字上限 ]     │
  │  图片     [ 已选缩略图（右上角 × 删除）        │
  │             + 「添加」虚线方块，最多 9 张 ]     │
  │  [ 发布/保存修改 ]  [ 取消 ]                   │
  └───────────────────────────────────────────────┘
  图片上传说明：用隐藏的 <input type="file"> 承载系统选文件对话框，
  点「添加」方块时以 JS 触发它的 click；选完可多张依次上传。
  保存成功后统一跳转个人中心（/profile）查看结果。
-->
<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategories, createProduct, updateProduct, getProduct, uploadImage } from '@/api'

const route = useRoute()
const router = useRouter()

/** 是否为编辑模式：路由中存在 :id 参数即视为编辑 */
const isEdit = computed(() => !!route.params.id)
const formRef = ref()          // el-form 实例，用于提交前校验
const categories = ref([])     // 分类下拉选项
const images = ref([])         // 已上传图片地址数组
const uploading = ref(false)   // 上传中状态（显示"上传中…"）
const fileInput = ref()        // 隐藏的文件选择框，供 pickFiles 手动触发

// 表单数据：价格用 null 占位，便于 el-input-number 显示为空
const form = reactive({ title: '', categoryId: null, price: null, description: '' })

// 校验规则：标题、分类、价格必填；描述与图片选填
const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

/** 打开系统选文件对话框（原生 input 被隐藏，只能用 JS 触发） */
function pickFiles() {
  fileInput.value.click()
}

/**
 * 文件选择回调：逐个上传选中的图片，把返回的 url 追加到 images。
 * 上传完成后清空 input.value，否则再次选择同一文件不会触发 change。
 * @param {Event} e input 的 change 事件
 */
async function onFiles(e) {
  const files = Array.from(e.target.files || [])
  if (!files.length) return
  uploading.value = true
  try {
    for (const f of files) {
      const res = await uploadImage(f)
      images.value.push(res.url)
    }
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

/**
 * 删除指定位置的图片
 * @param {number} i images 数组下标
 */
function removeImage(i) {
  images.value.splice(i, 1)
}

/** 加载分类下拉选项（失败不阻塞表单，允许用户稍后重试） */
async function loadCategories() {
  try {
    categories.value = await getCategories()
  } catch { /* ignore */ }
}

/** 编辑模式：把已有商品数据回填到表单（新建模式直接返回） */
async function loadProduct() {
  if (!isEdit.value) return
  try {
    const p = await getProduct(route.params.id)
    form.title = p.title
    form.categoryId = p.categoryId
    form.price = p.price
    form.description = p.description
    images.value = [...(p.images || [])]
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

/**
 * 提交表单：校验 → 组装 payload → 按模式调用更新或新建接口。
 * 成功后跳个人中心，"取消"按钮则返回上一页。
 */
async function submit() {
  await formRef.value.validate()
  if (images.value.length === 0) {
    ElMessage.warning('请至少上传一张图片')
    return
  }
  const payload = {
    title: form.title,
    categoryId: form.categoryId,
    price: form.price,
    description: form.description,
    images: images.value
  }
  try {
    if (isEdit.value) {
      await updateProduct(route.params.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createProduct(payload)
      ElMessage.success('发布成功')
    }
    router.push('/profile')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

// 首屏：并行准备分类选项与（编辑时的）商品详情
onMounted(() => {
  loadCategories()
  loadProduct()
})
</script>

<template>
  <div class="page-container">
    <div class="card form-card">
      <!-- 标题随模式变化 -->
      <h2>{{ isEdit ? '编辑商品' : '发布商品' }}</h2>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="64" show-word-limit placeholder="例如：高等数学教材" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <!-- 数字输入：最小 0，保留两位小数，步长 1 -->
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="成色、使用情况、购买时间等" />
        </el-form-item>

        <el-form-item label="图片">
          <div class="imgs">
            <!-- 已上传图片：缩略图 + 右上角删除按钮 -->
            <div v-for="(img, i) in images" :key="i" class="img-item">
              <el-image :src="img" fit="cover" class="thumb" />
              <span class="del" @click="removeImage(i)">×</span>
            </div>
            <!-- 添加按钮：满 9 张后自动隐藏 -->
            <div v-if="images.length < 9" class="add" @click="pickFiles">
              <el-icon><Plus /></el-icon>
            </div>
          </div>
          <span v-if="uploading" class="text-muted">上传中…</span>
          <div class="text-muted">至少上传 1 张图片，第一张将作为封面</div>
          <!-- 真实的文件选择框，被隐藏，仅用 JS 触发；multiple 支持一次选多张 -->
          <input ref="fileInput" type="file" accept="image/*" multiple hidden @change="onFiles" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit">{{ isEdit ? '保存修改' : '发布' }}</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
/* 表单卡片限宽居中，避免长输入框在宽屏下过宽 */
.form-card {
  max-width: 640px;
  margin: 0 auto;
}
/* 图片区：缩略图与「添加」方块横向排列，自动换行 */
.imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.img-item {
  position: relative;
  width: 100px;
  height: 100px;
}
.thumb {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}
/* 删除按钮：绝对定位到缩略图右上角外沿 */
.del {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
}
/* 「添加图片」虚线方块，尺寸与缩略图一致 */
.add {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
}
.add:hover {
  border-color: var(--sh-primary);
  color: var(--sh-primary);
}
</style>